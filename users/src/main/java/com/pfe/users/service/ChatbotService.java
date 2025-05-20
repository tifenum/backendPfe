package com.pfe.users.service;

import com.pfe.users.feignClient.FlightServiceClient;
import com.pfe.users.feignClient.CarsServiceClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriUtils;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Indexes;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Service
public class ChatbotService {
    private static final Logger logger = LoggerFactory.getLogger(ChatbotService.class);

    @Value("${gemini.api-key}")
    private String apiKey;

    private String mongoUri = "mongodb+srv://hbib:Azerty%409911@cluster0.br7eade.mongodb.net/?retryWrites=true&w=majority&appName=Cluster0";

    private static final String SYSTEM_PROMPT =
            "You are a travel assistant for a booking platform, helping users book flights, hotels, or cars. The current date is May 20, 2025. " +
                    "When the user requests a flight, gather details one at a time in this order: origin city (string, convert to IATA code), destination city (string, convert to IATA code), trip type (oneWay or roundTrip), departureDate (ISO date string, YYYY-MM-DD), returnDate (ISO date string, YYYY-MM-DD, only if roundTrip, must be after departureDate). " +
                    "For cities, verify if the provided city has an airport. If it does, confirm the airport name and IATA code (e.g., 'Paris, got it, I’ll use Charles de Gaulle (CDG)'). If the city has multiple airports, choose the main one. If the city has no airport, respond with 'Sorry, [city] doesn’t have an airport. Please provide a nearby city with an airport.' and wait for a new city. " +
                    "Convert city names to IATA codes (e.g., 'New York' to 'JFK', 'London' to 'LHR') using your knowledge. " +
                    "For dates, accept flexible inputs (e.g., 'next week', '3 February', 'tomorrow') and interpret them relative to May 20, 2025. Convert all dates to 'YYYY-MM-DD' format for the JSON output. If a date is ambiguous, invalid, or in the past, ask for clarification (e.g., 'Could you clarify the date? Please provide a future date like 'next week' or '3 February'.'). Ensure returnDate is after departureDate for roundTrip. " +
                    "Once all flight details are collected, reply with '[FLIGHT_RESULTS]' followed by 'Got all the info, searching for flights now!' and then a single PARAMETERS block in this exact JSON format (no extra keys, correct types):\n" +
                    "[PARAMETERS: {\n" +
                    "  \"origin\": \"<IATA code>\",\n" +
                    "  \"destination\": \"<IATA code>\",\n" +
                    "  \"departureDate\": \"<YYYY-MM-DD>\",\n" +
                    "  \"oneWay\": <true|false>,\n" +
                    "  \"returnDate\": <\"YYYY-MM-DD\"|null>\n" +
                    "}]\n" +
                    "Use null for returnDate if oneWay is true. " +
                    "For hotels, only respond if the user explicitly says 'hotel' or 'hotels.' Ask for the city, then suggest three fake hotels with '[HOTEL_RESULTS]' at the start, each on its own line in this format: " +
                    "'[HOTEL_RESULTS]\n{HOTEL_NAME}: {DESCRIPTION}. [BOOK_NOW:/hotel-details?lat={LAT}&lng={LNG}&hotelName={HOTEL_NAME}]'. " +
                    "Invent {LAT}, {LNG}, and URL-encode {HOTEL_NAME}. " +
                    "For cars, only respond if the user explicitly says 'car,' 'cars,' 'rent a car,' or 'car rental.' Gather details one at a time in this order: pickupCountry (string), pickupCity (string). Validate that pickupCountry if exist or the spelling is correct. If invalid, respond with 'Sorry, [pickupCountry] is not a valid country name. Please provide a valid country name (e.g., United States, France).' and wait for a new name. For pickupCity, verify it’s a plausible city in the given country. If it seems invalid, ask for clarification (e.g., 'Is [pickupCity] a city in [pickupCountry]? Please confirm or provide another city.'). Once all car details are collected, reply with '[CAR_RESULTS]' followed by 'Got all the info, searching for cars now!' and then a single PARAMETERS block in this exact JSON format (no extra keys, correct types):\n" +
                    "[PARAMETERS: {\n" +
                    "  \"pickupCountry\": \"<string>\",\n" +
                    "  \"pickupCity\": \"<string>\",\n" +
                    "}]\n" +
                    "Only include BOOK_NOW markers when all required info is provided. " +
                    "Never ask for info the user already gave. Never switch to flights, hotels, or cars unless the user asks for them. " +
                    "If the user asks to see their bookings, respond with: 'Let me check your bookings for you!' and the system will append the details. " +
                    "Keep all responses short and conversational.";

    private final RestTemplate restTemplate = new RestTemplate();
    private final Map<String, List<Map<String, String>>> sessionHistory = new HashMap<>();
    private final MongoCollection<Document> hotelCollection;
    private final MongoCollection<Document> flightCollection;
    private final MongoCollection<Document> carCollection;

    @Autowired
    private FlightServiceClient flightServiceClient;

    @Autowired
    private CarsServiceClient carsServiceClient;

    public ChatbotService() {
        try {
            MongoClient mongoClient = MongoClients.create(mongoUri);
            MongoDatabase database = mongoClient.getDatabase("PFE");
            database.runCommand(new Document("ping", 1));
            logger.info("MongoDB connected successfully");
            this.hotelCollection = database.getCollection("hotelBookings");
            this.flightCollection = database.getCollection("flight_bookings");
            this.carCollection = database.getCollection("carBookings");
            this.hotelCollection.createIndex(Indexes.ascending("userId"));
            this.flightCollection.createIndex(Indexes.ascending("userId"));
            this.carCollection.createIndex(Indexes.ascending("userId"));
        } catch (Exception e) {
            logger.error("MongoDB connection failed: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to connect to MongoDB", e);
        }
    }

    public Map<String, Object> askAssistant(String userMessage, String sessionId, String userId) {
        String url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash-latest:generateContent";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("x-goog-api-key", apiKey);

        List<Map<String, String>> history = sessionHistory.computeIfAbsent(sessionId, k -> new ArrayList<>());
        history.add(Map.of("role", "user", "content", userMessage));

        // Check for bookings
        String bookingContext = "";
        if (userMessage.toLowerCase().contains("bookings") || userMessage.toLowerCase().contains("show") || userMessage.toLowerCase().contains("list")) {
            if (userId == null || userId.isEmpty()) {
                bookingContext = "You must be logged in first to fetch your bookings.";
            } else {
                try {
                    bookingContext = getBookingContext(userId, userMessage.toLowerCase());
                    logger.debug("Booking context for user {}: {}", userId, bookingContext);
                } catch (Exception e) {
                    logger.error("Error fetching booking context for user {}: {}", userId, e.getMessage(), e);
                    bookingContext = "Error retrieving bookings. Please try again.";
                }
            }
        }

        Map<String, Object> requestBody = new HashMap<>();
        List<Map<String, Object>> contents = new ArrayList<>();

        // Add system prompt
        Map<String, Object> systemContent = new HashMap<>();
        systemContent.put("role", "assistant");
        systemContent.put("parts", List.of(Map.of("text", SYSTEM_PROMPT)));
        contents.add(systemContent);

        // Add conversation history
        for (Map<String, String> msg : history) {
            Map<String, Object> content = new HashMap<>();
            content.put("role", msg.get("role"));
            content.put("parts", List.of(Map.of("text", msg.get("content"))));
            contents.add(content);
        }

        requestBody.put("contents", contents);

        try {
            logger.debug("Sending request to Gemini API for session {}", sessionId);
            ResponseEntity<Map> response = restTemplate.exchange(
                    url, HttpMethod.POST, new HttpEntity<>(requestBody, headers), Map.class
            );

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                Map<String, Object> responseBody = response.getBody();
                List<Map<String, Object>> candidates = (List<Map<String, Object>>) responseBody.get("candidates");

                if (candidates != null && !candidates.isEmpty()) {
                    Map<String, Object> contentResponse = (Map<String, Object>) candidates.get(0).get("content");
                    List<Map<String, String>> responseParts = (List<Map<String, String>>) contentResponse.get("parts");
                    String botResponse = responseParts.get(0).get("text");

                    // Append booking context only if it's a booking query
                    if (!bookingContext.isEmpty()) {
                        botResponse = "Let me check your bookings for you!\n" + bookingContext;
                    }

                    history.add(Map.of("role", "assistant", "content", botResponse));
                    if (history.size() > 20) {
                        history.subList(0, history.size() - 20).clear();
                    }

                    Map<String, String> parameters = extractParameters(botResponse);
                    if (parameters != null) {
                        if (botResponse.contains("[FLIGHT_RESULTS]")) {
                            String flightType = parameters.get("oneWay").equals("true") ? "one-way" : "round-trip";
                            List<Map<String, Object>> flightOffers = flightServiceClient.getFakeFlightOffers(
                                    parameters.get("origin"),
                                    parameters.get("destination"),
                                    parameters.get("departureDate"),
                                    parameters.get("oneWay").equals("true") ? null : parameters.get("returnDate"),
                                    flightType
                            );
                            List<Map<String, Object>> formattedOffers = formatFlightOffers(flightOffers);
                            return Map.of("message", botResponse, "flightOffers", formattedOffers);
                        } else if (botResponse.contains("[CAR_RESULTS]")) {
                            List<Map<String, Object>> carOffers = carsServiceClient.getFakeCars(
                                    parameters.get("pickupCountry"),
                                    parameters.get("pickupCity")
                            );
                            List<Map<String, Object>> formattedOffers = formatCarOffers(carOffers);
                            return Map.of("message", botResponse, "carOffers", formattedOffers);
                        }
                    }
                    return Map.of("message", botResponse);
                }
            }
            logger.warn("No valid response from Gemini API for session {}", sessionId);
            return Map.of("message", "Sorry, I couldn’t generate a response. Try again.");
        } catch (Exception e) {
            logger.error("Error with Gemini API for session {}: {}", sessionId, e.getMessage(), e);
            return Map.of("message", "Error with Gemini API: " + e.getMessage());
        }
    }

    private String getBookingContext(String userId, String lowerMessage) {
        List<Document> hotelDocs = new ArrayList<>();
        List<Document> flightDocs = new ArrayList<>();
        List<Document> carDocs = new ArrayList<>();

        try {
            // Fetch bookings based on user intent
            if (lowerMessage.contains("hotel")) {
                hotelDocs = hotelCollection.find(new Document("userId", userId)).limit(10).into(new ArrayList<>());
                logger.debug("Fetched {} hotel bookings for user {}", hotelDocs.size(), userId);
            } else if (lowerMessage.contains("flight")) {
                flightDocs = flightCollection.find(new Document("userId", userId)).limit(10).into(new ArrayList<>());
                logger.debug("Fetched {} flight bookings for user {}", flightDocs.size(), userId);
            } else if (lowerMessage.contains("car") || lowerMessage.contains("rental")) {
                carDocs = carCollection.find(new Document("userId", userId)).limit(10).into(new ArrayList<>());
                logger.debug("Fetched {} car bookings for user {}", carDocs.size(), userId);
            } else {
                hotelDocs = hotelCollection.find(new Document("userId", userId)).limit(10).into(new ArrayList<>());
                flightDocs = flightCollection.find(new Document("userId", userId)).limit(10).into(new ArrayList<>());
                carDocs = carCollection.find(new Document("userId", userId)).limit(10).into(new ArrayList<>());
                logger.debug("Fetched {} hotel, {} flight, and {} car bookings for user {}", hotelDocs.size(), flightDocs.size(), carDocs.size(), userId);
            }

            StringBuilder context = new StringBuilder();
            if (!hotelDocs.isEmpty()) {
                context.append(formatHotelBookings(hotelDocs));
            }
            if (!flightDocs.isEmpty()) {
                if (context.length() > 0) context.append("\n");
                context.append(formatFlightBookings(flightDocs));
            }
            if (!carDocs.isEmpty()) {
                if (context.length() > 0) context.append("\n");
                context.append(formatCarBookings(carDocs));
            }
            if (context.length() == 0) {
                return "No bookings found!";
            }
            return context.toString();
        } catch (Exception e) {
            logger.error("Error in getBookingContext for user {}: {}", userId, e.getMessage(), e);
            throw new RuntimeException("Failed to fetch bookings", e);
        }
    }

    private String formatHotelBookings(List<Document> docs) {
        if (docs == null || docs.isEmpty()) {
            return "No hotel bookings found!\n";
        }
        List<String> lines = new ArrayList<>();
        for (Document b : docs) {
            try {
                logger.debug("Formatting hotel booking: {}", b.toJson());
                String name = b.getString("hotelName") != null ? UriUtils.decode(b.getString("hotelName"), StandardCharsets.UTF_8) : "Unknown";
                String addr = b.getString("hotelAddress") != null ? UriUtils.decode(b.getString("hotelAddress"), StandardCharsets.UTF_8) : "Unknown";
                String ci = b.getString("checkInDate") != null ? b.getString("checkInDate") : "?";
                String co = b.getString("checkOutDate") != null ? b.getString("checkOutDate") : "?";
                String status = b.getString("reservationStatus") != null ? b.getString("reservationStatus") : "?";
                Object price = b.get("totalPrice") != null ? b.get("totalPrice") : "?";
                lines.add(String.format("- %s | %s | Check-in: %s | Check-out: %s | Status: %s | $%s", name, addr, ci, co, status, price));
            } catch (Exception e) {
                logger.error("Error formatting hotel booking: {}", e.getMessage(), e);
                lines.add("- Error formatting hotel booking");
            }
        }
        return "Your Hotel Bookings:\n" + String.join("\n", lines) + "\n";
    }

    private String formatFlightBookings(List<Document> docs) {
        if (docs == null || docs.isEmpty()) {
            return "No flight bookings found!\n";
        }
        List<String> lines = new ArrayList<>();
        for (Document f : docs) {
            try {
                logger.debug("Formatting flight booking: {}", f.toJson());
                Document flightDetails = f.get("flightDetails", Document.class);
                if (flightDetails == null) {
                    lines.add("- Flight booking missing details");
                    continue;
                }

                Object flightIdObj = flightDetails.get("flightId");
                String flightId = flightIdObj != null ? flightIdObj.toString() : "Unknown";

                String dep = "?";
                String arr = "?";
                String date = "?";

                Object itinerariesObj = flightDetails.get("itineraries");
                if (itinerariesObj instanceof List) {
                    List<?> itinerariesRaw = (List<?>) itinerariesObj;
                    if (!itinerariesRaw.isEmpty()) {
                        Object firstItinerary = itinerariesRaw.get(0);
                        if (firstItinerary instanceof Document) {
                            Document itinerary = (Document) firstItinerary;
                            Object segmentsObj = itinerary.get("segments");
                            if (segmentsObj instanceof List) {
                                List<?> segmentsRaw = (List<?>) segmentsObj;
                                if (!segmentsRaw.isEmpty()) {
                                    Object firstSegment = segmentsRaw.get(0);
                                    if (firstSegment instanceof Document) {
                                        Document segment = (Document) firstSegment;
                                        Document departure = segment.get("departure", Document.class);
                                        Document arrival = segment.get("arrival", Document.class);
                                        if (departure != null) {
                                            dep = departure.getString("iataCode") != null ? departure.getString("iataCode") : "?";
                                            date = departure.getString("at") != null ? departure.getString("at") : "?";
                                        }
                                        if (arrival != null) {
                                            arr = arrival.getString("iataCode") != null ? arrival.getString("iataCode") : "?";
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                String status = f.getString("bookingStatus") != null ? f.getString("bookingStatus") : "?";
                Object price = flightDetails.get("price") != null ? flightDetails.get("price") : "?";
                lines.add(String.format("- Flight ID: %s | %s to %s | Departure: %s | Status: %s | $%s", flightId, dep, arr, date, status, price));
            } catch (Exception e) {
                logger.error("Error formatting flight booking: {}", e.getMessage(), e);
                lines.add("- Error formatting flight booking");
            }
        }
        return "Your Flight Bookings:\n" + String.join("\n", lines) + "\n";
    }

    private String formatCarBookings(List<Document> docs) {
        if (docs == null || docs.isEmpty()) {
            return "No car bookings found!\n";
        }
        List<String> lines = new ArrayList<>();
        for (Document c : docs) {
            try {
                logger.debug("Formatting car booking: {}", c.toJson());
                String carType = c.getString("carType") != null ? c.getString("carType") : "Unknown";
                String location = c.getString("pickupCity") != null && c.getString("pickupCountry") != null
                        ? c.getString("pickupCity") + ", " + c.getString("pickupCountry") : "Unknown";
                String pickupDate = c.getString("pickupDate") != null ? c.getString("pickupDate") : "?";
                String dropoffDate = c.getString("dropoffDate") != null ? c.getString("dropoffDate") : "?";
                String status = c.getString("reservationStatus") != null ? c.getString("reservationStatus") : "?";
                Object price = c.get("totalPrice") != null ? c.get("totalPrice") : "?";
                lines.add(String.format("- %s | %s | Pickup: %s | Drop-off: %s | Status: %s | $%s", carType, location, pickupDate, dropoffDate, status, price));
            } catch (Exception e) {
                logger.error("Error formatting car booking: {}", e.getMessage(), e);
                lines.add("- Error formatting car booking");
            }
        }
        return "Your Car Bookings:\n" + String.join("\n", lines) + "\n";
    }

    private Map<String, String> extractParameters(String botResponse) {
        int start = botResponse.indexOf("[PARAMETERS:");
        if (start == -1) return null;

        int end = botResponse.indexOf("]", start);
        if (end == -1) return null;

        String jsonStr = botResponse.substring(start + 12, end).trim();
        Map<String, String> params = new HashMap<>();
        String[] lines = jsonStr.split("\n");
        for (String line : lines) {
            line = line.trim();
            if (line.isEmpty() || line.equals("{") || line.equals("}")) continue;
            String[] keyValue = line.split(":\\s+", 2);
            if (keyValue.length == 2) {
                String key = keyValue[0].replace("\"", "").trim();
                String value = keyValue[1].replace("\"", "").replace(",", "").trim();
                params.put(key, value);
            }
        }
        if (params.size() >= 2) return params; // At least 2 for flights or cars
        return null;
    }

    private List<Map<String, Object>> formatFlightOffers(List<Map<String, Object>> flightOffers) {
        List<Map<String, Object>> formatted = new ArrayList<>();
        for (Map<String, Object> offer : flightOffers) {
            int id = (int) offer.get("id");
            String bookingLink = "/flight-details/" + id;
            offer.put("bookingLink", bookingLink);
            formatted.add(offer);
        }
        return formatted;
    }

    private List<Map<String, Object>> formatCarOffers(List<Map<String, Object>> carOffers) {
        List<Map<String, Object>> formatted = new ArrayList<>();
        for (Map<String, Object> offer : carOffers) {
            Map<String, Object> formattedOffer = new HashMap<>();
            formattedOffer.put("pickupCountry", offer.get("pickupCountry")); // Full country name, e.g., "France"
            formattedOffer.put("pickupCity", offer.get("pickupCity")); // e.g., "Paris"
            List<Map<String, Object>> carTypes = (List<Map<String, Object>>) offer.get("carTypes");
            formattedOffer.put("carTypes", carTypes);
            String bookingLink = "/car-details?pickupCountry=" + URLEncoder.encode((String) offer.get("pickupCountry"), StandardCharsets.UTF_8) +
                    "&pickupCity=" + URLEncoder.encode((String) offer.get("pickupCity"), StandardCharsets.UTF_8) +
                    "&carType=" + (carTypes.isEmpty() ? "Economy" : carTypes.get(0).get("type"));
            formattedOffer.put("bookingLink", bookingLink);
            formatted.add(formattedOffer);
        }
        return formatted;
    }
}