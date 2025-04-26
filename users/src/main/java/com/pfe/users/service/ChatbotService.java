package com.pfe.users.service;

import com.pfe.users.feignClient.FlightServiceClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
public class ChatbotService {
    @Value("${gemini.api-key}")
    private String apiKey;

    private static final String SYSTEM_PROMPT =
            "You are a travel assistant for a booking platform, helping users book flights or hotels. " +
                    "When the user requests a flight, gather details one at a time in this order: origin (string IATA code), destination (string IATA code), departureDate (ISO date string, YYYY-MM-DD), adults (integer), oneWay (boolean). " +
                    "Once all details are collected, reply with '[FLIGHT_RESULTS]' followed by 'Got all the info, searching for flights now!' and then a PARAMETERS block in this exact JSON format (no extra keys, correct types):\n" +
                    "```json\n" +
                    "[PARAMETERS: {\n" +
                    "  \"origin\": \"<string>\",\n" +
                    "  \"destination\": \"<string>\",\n" +
                    "  \"departureDate\": \"<YYYY-MM-DD>\",\n" +
                    "  \"adults\": <integer>,\n" +
                    "  \"oneWay\": <true|false>,\n" +
                    "  \"returnDate\": <\"YYYY-MM-DD\"|null>\n" +
                    "}]```\n" +
                    "Use null for returnDate if oneWay is true. "+
                    "For hotels, only respond if the user explicitly says 'hotel' or 'hotels.' Ask for the city, then suggest three fake hotels with '[HOTEL_RESULTS]' at the start, each on its own line in this format: " +
                    "'[HOTEL_RESULTS]\n{HOTEL_NAME}: {DESCRIPTION}. [BOOK_NOW:http://localhost:3000/hotel-details?lat={LAT}&lng={LAN}&hotelName={HOTEL_NAME}'. " +
                    "Invent {LAT}, {LNG}, and URL-encode {HOTEL_NAME}. " +
                    "Only include BOOK_NOW markers when all required info is provided. " +
                    "Never ask for info the user already gave. Never switch to hotels unless the user asks for them. " +
                    "Keep all responses short and conversational.";

    private final RestTemplate restTemplate = new RestTemplate();
    private final Map<String, List<Map<String, String>>> sessionHistory = new HashMap<>();

    @Autowired
    private FlightServiceClient flightServiceClient;

    public Map<String, Object> askAssistant(String userMessage, String sessionId) {
        String url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash-latest:generateContent";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("x-goog-api-key", apiKey);

        List<Map<String, String>> history = sessionHistory.computeIfAbsent(sessionId, k -> new ArrayList<>());
        history.add(Map.of("role", "user", "content", userMessage));

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

                    Map<String, String> parameters = extractParameters(botResponse);
                    if (parameters != null) {
                        List<Map<String, Object>> flightOffers = flightServiceClient.getFakeFlightOffers(
                                parameters.get("origin"),
                                parameters.get("destination"),
                                parameters.get("departureDate"),
                                parameters.get("oneWay").equals("true") ? null : parameters.get("returnDate"),
                                Integer.parseInt(parameters.get("adults"))
                        );

                        List<Map<String, Object>> formattedOffers = formatFlightOffers(flightOffers);
                        return Map.of("message", botResponse, "flightOffers", formattedOffers);
                    }
                    return Map.of("message", botResponse);
                }
            }
            return Map.of("message", "Sorry, I couldn’t generate a response. Try again.");
        } catch (Exception e) {
            e.printStackTrace();
            return Map.of("message", "Error with Gemini API: " + e.getMessage());
        }
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
        if (params.size() == 6) return params;
        return null;
    }

    private List<Map<String, Object>> formatFlightOffers(List<Map<String, Object>> flightOffers) {
        List<Map<String, Object>> formatted = new ArrayList<>();
        for (Map<String, Object> offer : flightOffers) {
            int id = (int) offer.get("id");
            String bookingLink = "http://localhost:3000/flight-details/" + id;
            offer.put("bookingLink", bookingLink);
            formatted.add(offer);
        }
        return formatted;
    }
}