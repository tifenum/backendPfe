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
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Indexes;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class ChatbotService {
    private static final Logger logger = LoggerFactory.getLogger(ChatbotService.class);

    @Value("${gemini.api-key}")
    private String apiKey;

    private String mongoUri = "mongodb+srv://hbib:Azerty%409911@cluster0.br7eade.mongodb.net/?retryWrites=true&w=majority&appName=Cluster0";

    private static final String SYSTEM_PROMPT =
            "You are a travel assistant for a booking platform, helping users book flights, hotels, or cars. The current date is May 21, 2025. " +
                    "When the user requests a flight, gather details one at a time in this order: origin city (string, convert to IATA code), destination city (string, convert to IATA code), trip type (oneWay or roundTrip), departureDate (ISO date string, YYYY-MM-DD), returnDate (ISO date string, YYYY-MM-DD, only if roundTrip, must be after departureDate). " +
                    "For cities, verify if the provided city has an airport. If it does, confirm the airport name and IATA code (e.g., 'Paris, got it, I’ll use Charles de Gaulle (CDG)'). If the city has multiple airports, choose the main one. If the city has no airport, respond with 'Sorry, [city] doesn’t have an airport. Please provide a nearby city with an airport.' and wait for a new city. " +
                    "Convert city names to IATA codes (e.g., 'New York' to 'JFK', 'London' to 'LHR') using your knowledge. " +
                    "For dates, accept flexible inputs (e.g., 'next week', '3 February', 'tomorrow') and interpret them relative to May 21, 2025. Convert all dates to 'YYYY-MM-DD' format for the JSON output. If a date is ambiguous, invalid, or in the past, ask for clarification (e.g., 'Could you clarify the date? Please provide a future date like 'next week' or '3 February'.'). Ensure returnDate is after departureDate for roundTrip. " +
                    "Once all flight details are collected, reply with 'Got all the info, searching for flights now!' followed by a single PARAMETERS block in this exact JSON format (no extra keys, correct types):\n" +
                    "[PARAMETERS: {\n" +
                    "  \"origin\": \"<IATA code>\",\n" +
                    "  \"destination\": \"<IATA code>\",\n" +
                    "  \"departureDate\": \"<YYYY-MM-DD>\",\n" +
                    "  \"oneWay\": <true|false>,\n" +
                    "  \"returnDate\": <\"YYYY-MM-DD\"|null>\n" +
                    "}]\n" +
                    "Use null for returnDate if oneWay is true. " +
                    "For hotels, only respond if the user explicitly says 'hotel' or 'hotels.' Ask for the city, then suggest three fake hotels with a JSON array of three hotel objects in this format:\n" +
                    "[\n" +
                    "  {\"hotelName\": \"<string>\", \"description\": \"<string>\", \"lat\": <number>, \"lng\": <number>},\n" +
                    "  {\"hotelName\": \"<string>\", \"description\": \"<string>\", \"lat\": <number>, \"lng\": <number>},\n" +
                    "  {\"hotelName\": \"<string>\", \"description\": \"<string>\", \"lat\": <number>, \"lng\": <number>}\n" +
                    "]\n" +
                    "Invent realistic lat and lng coordinates for the city (e.g., for Paris, use values around lat: 48.85, lng: 2.35). Ensure hotelName and description are unique and relevant to the city. " +
                    "For cars, only respond if the user explicitly says 'car,' 'cars,' 'rent a car,' or 'car rental.' Gather details one at a time in this order: pickupCountry (string), pickupCity (string). Validate that pickupCountry if exist or the spelling is correct. If invalid, respond with 'Sorry, [pickupCountry] is not a valid country name. Please provide a valid country name (e.g., United States, France).' and wait for a new name. For pickupCity, verify it’s a plausible city in the given country. If it seems invalid, ask for clarification (e.g., 'Is [pickupCity] a city in [pickupCountry]? Please confirm or provide another city.'). Once all car details are collected, reply with 'Got all the info, searching for cars now!' followed by a single PARAMETERS block in this exact JSON format (no extra keys, correct types):\n" +
                    "[PARAMETERS: {\n" +
                    "  \"pickupCountry\": \"<string>\",\n" +
                    "  \"pickupCity\": \"<string>\",\n" +
                    "}]\n" +
                    "When referring to bookings, use custom IDs (e.g., H1 for hotels, F1 for flights, C1 for cars) instead of internal IDs. " +
                    "If the user mentions a custom ID (e.g., 'show H1'), interpret it as referring to the corresponding booking and respond with the booking details using the same custom ID. " +
                    "Never ask for info the user already gave. Never switch to flights, hotels, or cars unless the user asks for them. " +
                    "Keep all responses short and conversational.";

    private final RestTemplate restTemplate = new RestTemplate();
    private final Map<String, List<Map<String, String>>> sessionHistory = new HashMap<>();
    private final MongoCollection<Document> hotelCollection;
    private final MongoCollection<Document> flightCollection;
    private final MongoCollection<Document> carCollection;
    private final ObjectMapper objectMapper = new ObjectMapper();

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
        String lowerMessage = userMessage.toLowerCase();
        List<Map<String, String>> history = sessionHistory.computeIfAbsent(sessionId, k -> new ArrayList<>());
        history.add(Map.of("role", "user", "content", userMessage));

        // Handle booking requests
        if (lowerMessage.contains("bookings") || lowerMessage.contains("fetch") ||
                lowerMessage.contains("display") || lowerMessage.contains("show") ||
                lowerMessage.contains("list") || lowerMessage.contains("reservations") ||
                lowerMessage.contains("rentals")) {
            if (userId == null || userId.isEmpty()) {
                String errorMessage = "You must be logged in first to fetch your bookings.";
                history.add(Map.of("role", "assistant", "content", errorMessage));
                return Map.of("message", errorMessage);
            }

            try {
                // Check for single booking display (e.g., "show C5", "give me only the C5 car")
                String customId = extractCustomId(lowerMessage);
                if (customId != null && (lowerMessage.contains("show") || lowerMessage.contains("display") ||
                        lowerMessage.contains("give") || lowerMessage.contains("only"))) {
                    pruneSessionHistory(history, customId);
                    Map<String, Object> singleBookingResponse = getSingleBookingContext(userId, customId, lowerMessage);
                    String botResponse = (String) singleBookingResponse.get("context");
                    List<Map<String, Object>> bookings = (List<Map<String, Object>>) singleBookingResponse.getOrDefault("bookings", new ArrayList<>());
                    history.add(Map.of("role", "assistant", "content", botResponse));
                    if (bookings.isEmpty()) {
                        return Map.of("message", botResponse);
                    }
                    return Map.of("message", botResponse, "bookings", bookings, "idMappings", singleBookingResponse.get("idMappings"));
                }

                Map<String, Object> bookingResponse = getBookingContext(userId, lowerMessage);
                String botResponse = (String) bookingResponse.get("context");
                List<Map<String, Object>> bookings = (List<Map<String, Object>>) bookingResponse.getOrDefault("bookings", new ArrayList<>());
                botResponse = botResponse.isEmpty() ? "No bookings found!" : botResponse;
                logger.debug("Booking context for user {}: {}", userId, botResponse);
                history.add(Map.of("role", "assistant", "content", botResponse));
                if (history.size() > 20) {
                    history.subList(0, history.size() - 20).clear();
                }
                if (bookings.isEmpty()) {
                    return Map.of("message", botResponse);
                }
                return Map.of("message", botResponse, "bookings", bookings, "idMappings", bookingResponse.get("idMappings"));
            } catch (Exception e) {
                logger.error("Error fetching booking context for user {}: {}", userId, e.getMessage(), e);
                String errorMessage = "Error retrieving bookings. Please try again.";
                history.add(Map.of("role", "assistant", "content", errorMessage));
                return Map.of("message", errorMessage);
            }
        }

        // Handle deletion requests
        if (lowerMessage.contains("cancel") || lowerMessage.contains("delete")) {
            String botResponse = "Sorry, deleting bookings is not supported. You can view your bookings with 'display all bookings'.";
            history.add(Map.of("role", "assistant", "content", botResponse));
            return Map.of("message", botResponse);
        }

        // Handle non-booking requests (flights, hotels, cars) via Gemini API
        String url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash-latest:generateContent";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("x-goog-api-key", apiKey);

        Map<String, Object> requestBody = new HashMap<>();
        List<Map<String, Object>> contents = new ArrayList<>();

        Map<String, Object> systemContent = new HashMap<>();
        systemContent.put("role", "assistant");
        systemContent.put("parts", List.of(Map.of("text", SYSTEM_PROMPT)));
        contents.add(systemContent);

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

                    history.add(Map.of("role", "assistant", "content", botResponse));
                    if (history.size() > 20) {
                        history.subList(0, history.size() - 20).clear();
                    }

                    String cleanBotMessage = botResponse
                            .replaceAll("\\[PARAMETERS:[\\s\\S]*?\\]", "")
                            .replaceAll("\\[\\s*\\{[\\s\\S]*?\\}\\s*\\]", "")
                            .replaceAll("\\[.*_RESULTS\\]", "")
                            .trim();
                    if (cleanBotMessage.isEmpty()) {
                        cleanBotMessage = "Here are some great options for you!";
                    }

                    // Check for parameters first (flights or cars)
                    Map<String, String> parameters = extractParameters(botResponse);
                    if (parameters != null) {
                        if (parameters.containsKey("origin") && parameters.containsKey("destination")) {
                            String flightType = parameters.get("oneWay").equals("true") ? "one-way" : "round-trip";
                            List<Map<String, Object>> flightOffers = flightServiceClient.getFakeFlightOffers(
                                    parameters.get("origin"),
                                    parameters.get("destination"),
                                    parameters.get("departureDate"),
                                    parameters.get("oneWay").equals("true") ? null : parameters.get("returnDate"),
                                    flightType
                            );
                            return Map.of("message", cleanBotMessage, "flightOffers", formatFlightOffers(flightOffers));
                        } else if (parameters.containsKey("pickupCountry") && parameters.containsKey("pickupCity")) {
                            List<Map<String, Object>> carOffers = carsServiceClient.getFakeCars(
                                    parameters.get("pickupCountry"),
                                    parameters.get("pickupCity")
                            );
                            return Map.of("message", cleanBotMessage, "carOffers", formatCarOffers(carOffers));
                        }
                    }

                    // Only check for hotel offers if no parameters (i.e., not a flight or car response)
                    List<Map<String, Object>> hotelOffers = extractHotelOffers(botResponse);
                    if (!hotelOffers.isEmpty()) {
                        return Map.of("message", cleanBotMessage, "hotelOffers", formatHotelOffers(hotelOffers));
                    }

                    return Map.of("message", cleanBotMessage);
                }
            }
            logger.warn("No valid response from Gemini API for session {}", sessionId);
            return Map.of("message", "Sorry, I couldn’t generate a response. Try again.");
        } catch (Exception e) {
            logger.error("Error with Gemini API for session {}: {}", sessionId, e.getMessage(), e);
            return Map.of("message", "Error with Gemini API: " + e.getMessage());
        }
    }

    private void pruneSessionHistory(List<Map<String, String>> history, String customId) {
        List<Map<String, String>> newHistory = new ArrayList<>();
        for (Map<String, String> msg : history) {
            if (msg.get("role").equals("assistant") && msg.get("content").equals(SYSTEM_PROMPT)) {
                newHistory.add(msg);
            } else if (msg.get("role").equals("user") && msg.get("content").toLowerCase().contains(customId.toLowerCase())) {
                newHistory.add(msg);
            }
        }
        history.clear();
        history.addAll(newHistory);
    }

    private Map<String, Object> getBookingContext(String userId, String lowerMessage) {
        List<Document> hotelDocs = new ArrayList<>();
        List<Document> flightDocs = new ArrayList<>();
        List<Document> carDocs = new ArrayList<>();
        Map<String, String> idMappings = new HashMap<>();

        String statusFilter = null;
        Double maxPrice = null;
        String dateFilterField = null;
        String dateFilterValue = null;
        String dateFilterType = null;
        String locationFilter = null;
        String iataFilter = null;
        List<String> bookingTypes = new ArrayList<>();

        // Status filter
        if (lowerMessage.contains("accepted")) {
            statusFilter = "Accepted";
        } else if (lowerMessage.contains("refused")) {
            statusFilter = "Refused";
        } else if (lowerMessage.contains("pending")) {
            statusFilter = "Pending";
        }

        // Price filter
        Pattern pricePattern = Pattern.compile("(under|cheaper than|less than)\\s+(\\d+)\\s*(dollar|dollars)?");
        Matcher priceMatcher = pricePattern.matcher(lowerMessage);
        if (priceMatcher.find()) {
            try {
                maxPrice = Double.parseDouble(priceMatcher.group(2));
            } catch (NumberFormatException e) {
                logger.warn("Invalid price format in message: {}", lowerMessage);
            }
        }

        // Date filter
        Pattern datePattern = Pattern.compile("(after|before)\\s+([\\d-]{10}|next month|tomorrow|next week)");
        Matcher dateMatcher = datePattern.matcher(lowerMessage);
        if (dateMatcher.find()) {
            dateFilterType = dateMatcher.group(1);
            String dateInput = dateMatcher.group(2);
            try {
                LocalDate currentDate = LocalDate.of(2025, 5, 21);
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

                if (dateInput.equals("next month")) {
                    dateFilterValue = currentDate.plusMonths(1).withDayOfMonth(1).format(formatter);
                } else if (dateInput.equals("tomorrow")) {
                    dateFilterValue = currentDate.plusDays(1).format(formatter);
                } else if (dateInput.equals("next week")) {
                    dateFilterValue = currentDate.plusWeeks(1).format(formatter);
                } else {
                    LocalDate parsedDate = LocalDate.parse(dateInput, formatter);
                    if (parsedDate.isBefore(currentDate) && dateFilterType.equals("after")) {
                        logger.warn("Date {} is in the past for 'after' filter", dateInput);
                    } else {
                        dateFilterValue = dateInput;
                    }
                }
            } catch (DateTimeParseException e) {
                logger.warn("Invalid date format in message: {}", dateInput);
            }
        }

        // Location filter (city, country, or IATA code)
        Pattern locationPattern = Pattern.compile("\\b(in|at|from|to)\\s+([a-zA-Z\\s]+|[A-Z]{3})\\b");
        Matcher locationMatcher = locationPattern.matcher(lowerMessage);
        if (locationMatcher.find()) {
            String location = locationMatcher.group(2).trim();
            // Check if it's a 3-letter IATA code
            if (location.matches("[A-Z]{3}")) {
                iataFilter = location;
                logger.debug("Detected IATA code filter: {}", iataFilter);
            } else {
                locationFilter = location.replaceAll("\\s+", " ").trim();
                logger.debug("Detected location filter: {}", locationFilter);
            }
        }

        // Detect booking types
        boolean fetchHotels = lowerMessage.contains("hotel") || lowerMessage.contains("hotels");
        boolean fetchFlights = lowerMessage.contains("flight") || lowerMessage.contains("flights");
        boolean fetchCars = lowerMessage.contains("car") || lowerMessage.contains("cars") || lowerMessage.contains("rental") || lowerMessage.contains("rentals");

        // Check for combinations using "and" or ","
        Pattern comboPattern = Pattern.compile("\\b(hotels?|flights?|cars?|rentals?)\\s*(and|,)\\s*(hotels?|flights?|cars?|rentals?)\\b");
        Matcher comboMatcher = comboPattern.matcher(lowerMessage);
        if (comboMatcher.find()) {
            String type1 = comboMatcher.group(1);
            String type2 = comboMatcher.group(3);
            if (type1.matches("hotels?")) bookingTypes.add("hotel");
            else if (type1.matches("flights?")) bookingTypes.add("flight");
            else if (type1.matches("cars?|rentals?")) bookingTypes.add("car");
            if (type2.matches("hotels?")) bookingTypes.add("hotel");
            else if (type2.matches("flights?")) bookingTypes.add("flight");
            else if (type2.matches("cars?|rentals?")) bookingTypes.add("car");
            // Remove duplicates
            bookingTypes = bookingTypes.stream().distinct().toList();
            logger.debug("Detected booking type combination: {}", bookingTypes);
        } else if (fetchHotels || fetchFlights || fetchCars) {
            // Single type or implicit combination
            if (fetchHotels) bookingTypes.add("hotel");
            if (fetchFlights) bookingTypes.add("flight");
            if (fetchCars) bookingTypes.add("car");
            bookingTypes = bookingTypes.stream().distinct().toList();
        } else {
            // Default to all types if no specific type or combination is mentioned
            bookingTypes = List.of("hotel", "flight", "car");
        }

        try {
            Document baseQuery = new Document("userId", userId);
            Document flightQuery = new Document("userId", userId);

            // Apply status filter
            if (statusFilter != null) {
                baseQuery.append("reservationStatus", statusFilter);
                flightQuery.append("bookingStatus", statusFilter);
            }

            // Apply price filter
            if (maxPrice != null) {
                baseQuery.append("totalPrice", new Document("$lt", maxPrice));
                flightQuery.append("flightDetails.price", new Document("$lt", maxPrice));
            }

            // Apply date filter
            if (dateFilterValue != null) {
                String mongoOperator = dateFilterType.equals("after") ? "$gt" : "$lt";
                if (bookingTypes.contains("hotel") && !bookingTypes.contains("flight") && !bookingTypes.contains("car")) {
                    dateFilterField = "checkInDate";
                } else if (bookingTypes.contains("flight") && !bookingTypes.contains("hotel") && !bookingTypes.contains("car")) {
                    dateFilterField = "flightDetails.itineraries.segments.departure.at";
                } else if (bookingTypes.contains("car") && !bookingTypes.contains("hotel") && !bookingTypes.contains("flight")) {
                    dateFilterField = "pickupDate";
                } else {
                    baseQuery.append("$or", Arrays.asList(
                            new Document("checkInDate", new Document(mongoOperator, dateFilterValue)),
                            new Document("pickupDate", new Document(mongoOperator, dateFilterValue))
                    ));
                    flightQuery.append("flightDetails.itineraries.segments.departure.at", new Document(mongoOperator, dateFilterValue));
                }
                if (dateFilterField != null) {
                    baseQuery.append(dateFilterField, new Document(mongoOperator, dateFilterValue));
                    if (dateFilterField.equals("flightDetails.itineraries.segments.departure.at")) {
                        flightQuery.append(dateFilterField, new Document(mongoOperator, dateFilterValue));
                    }
                }
            }

            // Apply location filter
            if (locationFilter != null) {
                baseQuery.append("$or", Arrays.asList(
                        new Document("hotelAddress", new Document("$regex", locationFilter).append("$options", "i")),
                        new Document("pickupCity", new Document("$regex", locationFilter).append("$options", "i")),
                        new Document("pickupCountry", new Document("$regex", locationFilter).append("$options", "i"))
                ));
            }
            if (iataFilter != null) {
                flightQuery.append("$or", Arrays.asList(
                        new Document("flightDetails.itineraries.segments.departure.iataCode", iataFilter),
                        new Document("flightDetails.itineraries.segments.arrival.iataCode", iataFilter)
                ));
            }

            // Fetch bookings based on requested types
            if (bookingTypes.contains("hotel")) {
                hotelDocs = hotelCollection.find(baseQuery).limit(10).into(new ArrayList<>());
                logger.debug("Fetched {} hotel bookings for user {} with status {}, price < {}, date {} {}, location {}",
                        hotelDocs.size(), userId, statusFilter != null ? statusFilter : "all",
                        maxPrice != null ? maxPrice : "none",
                        dateFilterType != null ? dateFilterType : "none", dateFilterValue != null ? dateFilterValue : "none",
                        locationFilter != null ? locationFilter : "none");
            }
            if (bookingTypes.contains("flight")) {
                flightDocs = flightCollection.find(flightQuery).limit(10).into(new ArrayList<>());
                logger.debug("Fetched {} flight bookings for user {} with status {}, price < {}, date {} {}, iata {}",
                        flightDocs.size(), userId, statusFilter != null ? statusFilter : "all",
                        maxPrice != null ? maxPrice : "none",
                        dateFilterType != null ? dateFilterType : "none", dateFilterValue != null ? dateFilterValue : "none",
                        iataFilter != null ? iataFilter : "none");
            }
            if (bookingTypes.contains("car")) {
                carDocs = carCollection.find(baseQuery).limit(10).into(new ArrayList<>());
                logger.debug("Fetched {} car bookings for user {} with status {}, price < {}, date {} {}, location {}",
                        carDocs.size(), userId, statusFilter != null ? statusFilter : "all",
                        maxPrice != null ? maxPrice : "none",
                        dateFilterType != null ? dateFilterType : "none", dateFilterValue != null ? dateFilterValue : "none",
                        locationFilter != null ? locationFilter : "none");
            }

            StringBuilder context = new StringBuilder();
            // Generate context header based on booking types
            String typeHeader = bookingTypes.size() == 1 ? bookingTypes.get(0).substring(0, 1).toUpperCase() + bookingTypes.get(0).substring(1) :
                    bookingTypes.size() == 2 ? String.join(" and ", bookingTypes.stream()
                            .map(type -> type.substring(0, 1).toUpperCase() + type.substring(1))
                            .toList()) :
                            "Hotel, Flight, and Car";
            String statusText = statusFilter != null ? statusFilter + " " : "";

            if (!hotelDocs.isEmpty() && bookingTypes.contains("hotel")) {
                context.append(formatHotelBookings(hotelDocs, statusFilter, idMappings));
            }
            if (!flightDocs.isEmpty() && bookingTypes.contains("flight")) {
                if (context.length() > 0) context.append("\n");
                context.append(formatFlightBookings(flightDocs, statusFilter, idMappings));
            }
            if (!carDocs.isEmpty() && bookingTypes.contains("car")) {
                if (context.length() > 0) context.append("\n");
                context.append(formatCarBookings(carDocs, statusFilter, idMappings));
            }
            if (context.length() == 0) {
                String priceText = maxPrice != null ? "under $" + maxPrice + " " : "";
                String dateText = dateFilterValue != null ? dateFilterType + " " + dateFilterValue + " " : "";
                String locationText = locationFilter != null ? "in " + locationFilter + " " : iataFilter != null ? "for " + iataFilter + " " : "";
                return Map.of("context", String.format("No %s%s%s%s%s bookings found!", statusText, priceText, dateText, locationText, typeHeader.toLowerCase()));
            }
            return Map.of("context", String.format("Your %s%s Bookings:\n", statusText, typeHeader) + context.toString(),
                    "bookings", parseBookingsToJson(context.toString()), "idMappings", idMappings);
        } catch (Exception e) {
            logger.error("Error in getBookingContext for user {}: {}", userId, e.getMessage(), e);
            return Map.of("context", "Error retrieving bookings. Please try again.");
        }
    }

    private Map<String, Object> getSingleBookingContext(String userId, String customId, String lowerMessage) {
        Map<String, String> idMappings = new HashMap<>();
        String prefix = customId.substring(0, 1);
        int index = Integer.parseInt(customId.substring(1)) - 1;
        MongoCollection<Document> collection;
        String type;

        try {
            if (prefix.equals("H")) {
                collection = hotelCollection;
                type = "hotel";
            } else if (prefix.equals("F")) {
                collection = flightCollection;
                type = "flight";
            } else if (prefix.equals("C")) {
                collection = carCollection;
                type = "car";
            } else {
                return Map.of("context", "Invalid custom ID format. Use H1, F1, C1, etc.");
            }

            List<Document> docs = collection.find(new Document("userId", userId)).limit(10).into(new ArrayList<>());
            if (index < 0 || index >= docs.size()) {
                return Map.of("context", String.format("No %s booking found with ID %s.", type, customId));
            }

            Document doc = docs.get(index);
            List<Document> singleDocList = List.of(doc);
            String context;
            idMappings.put(customId, doc.getObjectId("_id").toHexString());
            if (type.equals("hotel")) {
                context = formatHotelBookings(singleDocList, null, idMappings);
            } else if (type.equals("flight")) {
                context = formatFlightBookings(singleDocList, null, idMappings);
            } else {
                context = formatCarBookings(singleDocList, null, idMappings);
            }

            List<Map<String, Object>> bookings = parseSingleBookingToJson(context, customId, type);
            if (bookings.isEmpty()) {
                return Map.of("context", String.format("Error parsing booking %s. Please try again.", customId));
            }
            return Map.of("context", context, "bookings", bookings, "idMappings", idMappings);
        } catch (Exception e) {
            logger.error("Error fetching single booking {} for user {}: {}", customId, userId, e.getMessage(), e);
            return Map.of("context", "Error retrieving booking. Please try again.");
        }
    }

    private List<Map<String, Object>> parseSingleBookingToJson(String content, String customId, String type) {
        List<Map<String, Object>> bookings = new ArrayList<>();
        String[] lines = content.split("\n");

        for (String line : lines) {
            if (line.startsWith("- ID: " + customId)) {
                Map<String, Object> booking = new HashMap<>();
                String[] details = line.substring(2).split(" \\| ");
                booking.put("customId", customId);
                booking.put("type", type);

                try {
                    if (type.equals("flight")) {
                        Map<String, String> detailsMap = new HashMap<>();
                        String[] route = details[1].split(" to ");
                        detailsMap.put("origin", route[0].trim());
                        detailsMap.put("destination", route[1].trim());
                        detailsMap.put("departure", details[2].split(": ")[1].split("T")[0]);
                        detailsMap.put("status", details[3].split(": ")[1]);
                        detailsMap.put("price", details[4].replace("$", ""));
                        booking.put("details", detailsMap);
                    } else if (type.equals("hotel")) {
                        Map<String, String> detailsMap = new HashMap<>();
                        detailsMap.put("name", details[1]);
                        detailsMap.put("address", details[2]);
                        detailsMap.put("checkIn", details[3].split(": ")[1]);
                        detailsMap.put("checkOut", details[4].split(": ")[1]);
                        detailsMap.put("status", details[5].split(": ")[1]);
                        detailsMap.put("price", details[6].replace("$", ""));
                        booking.put("details", detailsMap);
                    } else if (type.equals("car")) {
                        Map<String, String> detailsMap = new HashMap<>();
                        detailsMap.put("carType", details[1]);
                        detailsMap.put("location", details[2]);
                        detailsMap.put("pickupDate", details[3].split(": ")[1]);
                        detailsMap.put("dropoffDate", details[4].split(": ")[1]);
                        detailsMap.put("status", details[5].split(": ")[1]);
                        detailsMap.put("price", details[6].replace("$", ""));
                        booking.put("details", detailsMap);
                    }
                    bookings.add(booking);
                } catch (Exception e) {
                    logger.error("Error parsing booking for customId {}: {}", customId, e.getMessage(), e);
                    return new ArrayList<>();
                }
                break;
            }
        }
        if (bookings.isEmpty()) {
            logger.warn("No booking parsed for customId {} in content: {}", customId, content);
        }
        return bookings;
    }

    private String formatHotelBookings(List<Document> docs, String statusFilter, Map<String, String> idMappings) {
        if (docs == null || docs.isEmpty()) {
            String statusText = statusFilter != null ? statusFilter.toLowerCase() + " " : "";
            return String.format("No %shotel bookings found!\n", statusText);
        }
        List<String> lines = new ArrayList<>();
        int counter = 1;
        for (Document b : docs) {
            try {
                logger.debug("Formatting hotel booking: {}", b.toJson());
                String mongoId = b.getObjectId("_id").toHexString();
                String customId = "H" + counter++;
                idMappings.put(customId, mongoId);
                String name = b.getString("hotelName") != null ? UriUtils.decode(b.getString("hotelName"), StandardCharsets.UTF_8) : "Unknown";
                String addr = b.getString("hotelAddress") != null ? UriUtils.decode(b.getString("hotelAddress"), StandardCharsets.UTF_8) : "Unknown";
                String ci = b.getString("checkInDate") != null ? b.getString("checkInDate") : "?";
                String co = b.getString("checkOutDate") != null ? b.getString("checkOutDate") : "?";
                String status = b.getString("reservationStatus") != null ? b.getString("reservationStatus") : "?";
                Object price = b.get("totalPrice") != null ? b.get("totalPrice") : "?";
                lines.add(String.format("- ID: %s | %s | %s | Check-in: %s | Check-out: %s | Status: %s | $%s", customId, name, addr, ci, co, status, price));
            } catch (Exception e) {
                logger.error("Error formatting hotel booking: {}", e.getMessage(), e);
                lines.add("- Error formatting hotel booking");
            }
        }
        String statusText = statusFilter != null ? statusFilter + " " : "";
        return String.format("Your %sHotel Bookings:\n", statusText) + String.join("\n", lines) + "\n";
    }

    private String formatFlightBookings(List<Document> docs, String statusFilter, Map<String, String> idMappings) {
        if (docs == null || docs.isEmpty()) {
            String statusText = statusFilter != null ? statusFilter.toLowerCase() + " " : "";
            return String.format("No %sflight bookings found!\n", statusText);
        }
        List<String> lines = new ArrayList<>();
        int counter = 1;
        for (Document f : docs) {
            try {
                logger.debug("Formatting flight booking: {}", f.toJson());
                String mongoId = f.getObjectId("_id").toHexString();
                String customId = "F" + counter++;
                idMappings.put(customId, mongoId);
                Document flightDetails = f.get("flightDetails", Document.class);
                if (flightDetails == null) {
                    lines.add(String.format("- ID: %s | Flight booking missing details", customId));
                    continue;
                }

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
                lines.add(String.format("- ID: %s | %s to %s | Departure: %s | Status: %s | $%s", customId, dep, arr, date, status, price));
            } catch (Exception e) {
                logger.error("Error formatting flight booking: {}", e.getMessage(), e);
                lines.add("- Error formatting flight booking");
            }
        }
        String statusText = statusFilter != null ? statusFilter + " " : "";
        return String.format("Your %sFlight Bookings:\n", statusText) + String.join("\n", lines) + "\n";
    }

    private String formatCarBookings(List<Document> docs, String statusFilter, Map<String, String> idMappings) {
        if (docs == null || docs.isEmpty()) {
            String statusText = statusFilter != null ? statusFilter.toLowerCase() + " " : "";
            return String.format("No %scar bookings found!\n", statusText);
        }
        List<String> lines = new ArrayList<>();
        int counter = 1;
        for (Document c : docs) {
            try {
                logger.debug("Formatting car booking: {}", c.toJson());
                String mongoId = c.getObjectId("_id").toHexString();
                String customId = "C" + counter++;
                idMappings.put(customId, mongoId);
                String carType = c.getString("carType") != null ? c.getString("carType") : "Unknown";
                String location = c.getString("pickupCity") != null && c.getString("pickupCountry") != null
                        ? c.getString("pickupCity") + ", " + c.getString("pickupCountry") : "Unknown";
                String pickupDate = c.getString("pickupDate") != null ? c.getString("pickupDate") : "?";
                String dropoffDate = c.getString("dropoffDate") != null ? c.getString("dropoffDate") : "?";
                String status = c.getString("reservationStatus") != null ? c.getString("reservationStatus") : "?";
                Object price = c.get("totalPrice") != null ? c.get("totalPrice") : "?";
                lines.add(String.format("- ID: %s | %s | %s | Pickup: %s | Drop-off: %s | Status: %s | $%s", customId, carType, location, pickupDate, dropoffDate, status, price));
            } catch (Exception e) {
                logger.error("Error formatting car booking: {}", e.getMessage(), e);
                lines.add("- Error formatting car booking");
            }
        }
        String statusText = statusFilter != null ? statusFilter + " " : "";
        return String.format("Your %sCar Bookings:\n", statusText) + String.join("\n", lines) + "\n";
    }

    private String extractCustomId(String message) {
        String regex = "(H|F|C)\\d+";
        java.util.regex.Matcher matcher = java.util.regex.Pattern.compile(regex).matcher(message);
        if (matcher.find()) {
            return matcher.group();
        }
        return null;
    }

    private List<Map<String, Object>> parseBookingsToJson(String content) {
        List<Map<String, Object>> bookings = new ArrayList<>();
        String[] lines = content.split("\n");
        String currentType = null;
        int counter = 1;

        for (String line : lines) {
            if (line.contains("Flight Bookings:")) {
                currentType = "flight";
                counter = 1;
            } else if (line.contains("Hotel Bookings:")) {
                currentType = "hotel";
                counter = 1;
            } else if (line.contains("Car Bookings:")) {
                currentType = "car";
                counter = 1;
            } else if (line.startsWith("- ID: ") && currentType != null) {
                Map<String, Object> booking = new HashMap<>();
                String[] details = line.substring(2).split(" \\| ");
                String customId = details[0].split(": ")[1];
                booking.put("customId", customId);
                booking.put("type", currentType);

                if (currentType.equals("flight")) {
                    Map<String, String> detailsMap = new HashMap<>();
                    String[] route = details[1].split(" to ");
                    detailsMap.put("origin", route[0].trim());
                    detailsMap.put("destination", route[1].trim());
                    detailsMap.put("departure", details[2].split(": ")[1].split("T")[0]);
                    detailsMap.put("status", details[3].split(": ")[1]);
                    detailsMap.put("price", details[4].replace("$", ""));
                    booking.put("details", detailsMap);
                } else if (currentType.equals("hotel")) {
                    Map<String, String> detailsMap = new HashMap<>();
                    detailsMap.put("name", details[1]);
                    detailsMap.put("address", details[2]);
                    detailsMap.put("checkIn", details[3].split(": ")[1]);
                    detailsMap.put("checkOut", details[4].split(": ")[1]);
                    detailsMap.put("status", details[5].split(": ")[1]);
                    detailsMap.put("price", details[6].replace("$", ""));
                    booking.put("details", detailsMap);
                } else if (currentType.equals("car")) {
                    Map<String, String> detailsMap = new HashMap<>();
                    detailsMap.put("carType", details[1]);
                    detailsMap.put("location", details[2]);
                    detailsMap.put("pickupDate", details[3].split(": ")[1]);
                    detailsMap.put("dropoffDate", details[4].split(": ")[1]);
                    detailsMap.put("status", details[5].split(": ")[1]);
                    detailsMap.put("price", details[6].replace("$", ""));
                    booking.put("details", detailsMap);
                }
                bookings.add(booking);
            }
        }
        return bookings;
    }

    private List<Map<String, Object>> extractHotelOffers(String botResponse) {
        try {
            // Look for a JSON array that doesn’t contain PARAMETERS
            int jsonStart = botResponse.indexOf("[");
            int jsonEnd = botResponse.lastIndexOf("]");
            if (jsonStart == -1 || jsonEnd == -1 || jsonEnd <= jsonStart || botResponse.contains("[PARAMETERS:")) {
                return new ArrayList<>();
            }

            String jsonStr = botResponse.substring(jsonStart, jsonEnd + 1);
            List<Map<String, Object>> hotelOffers = objectMapper.readValue(jsonStr, List.class);
            // Verify each object has the required hotel fields
            if (hotelOffers.stream().allMatch(offer ->
                    offer.containsKey("hotelName") && offer.get("hotelName") instanceof String &&
                            offer.containsKey("description") && offer.get("description") instanceof String &&
                            offer.containsKey("lat") && offer.get("lat") instanceof Number &&
                            offer.containsKey("lng") && offer.get("lng") instanceof Number)) {
                return hotelOffers;
            }
            return new ArrayList<>();
        } catch (Exception e) {
            logger.error("Error parsing hotel offers: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    private List<Map<String, Object>> formatHotelOffers(List<Map<String, Object>> hotelOffers) {
        return hotelOffers.stream()
                .filter(offer -> offer.containsKey("hotelName") && offer.containsKey("description") &&
                        offer.containsKey("lat") && offer.containsKey("lng"))
                .map(offer -> {
                    Map<String, Object> formatted = new HashMap<>();
                    formatted.put("hotelName", offer.get("hotelName"));
                    formatted.put("description", offer.get("description"));
                    formatted.put("lat", offer.get("lat"));
                    formatted.put("lng", offer.get("lng"));
                    return formatted;
                })
                .toList();
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
        if (params.size() >= 2) return params;
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
            formattedOffer.put("pickupCountry", offer.get("pickupCountry"));
            formattedOffer.put("pickupCity", offer.get("pickupCity"));
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