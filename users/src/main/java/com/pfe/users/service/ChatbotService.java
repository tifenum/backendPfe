package com.pfe.users.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ChatbotService {
    @Value("${gemini.api-key}")
    private String apiKey;

    private static final String SYSTEM_PROMPT =
            "You are a travel assistant for a booking platform, helping users book flights or hotels. "
                    + "For flights, collect details one at a time in this order: origin city/airport, destination city/airport, departure date, number of adults, one-way or round-trip. "
                    + "Use the conversation history to track provided info and only ask for what’s missing. "
                    + "Once all details are collected, suggest exactly three fake flights, each on its own line in this format: "
                    + "'Flight {FLIGHT_ID} with {AIRLINE}, departs {TIME}. [BOOK_NOW:http://localhost:3000/signin?redirect=%2Fflight-details%3FflightId={FLIGHT_ID}]'. "
                    + "Invent a unique {FLIGHT_ID} (e.g., FL123), {AIRLINE}, and {TIME}. "
                    + "For hotels, only respond if the user explicitly says 'hotel' or 'hotels.' Ask for the city, then suggest three fake hotels, each on its own line in this format: "
                    + "'{HOTEL_NAME}: {DESCRIPTION}. [BOOK_NOW:http://localhost:3000/signin?redirect=%2Fhotel-details%3Flat={LAT}%26lng={LNG}%26hotelName={ENCODED_NAME}]'. "
                    + "Invent {LAT}, {LNG}, and URL-encode {ENCODED_NAME}. "
                    + "Only include BOOK_NOW markers when all required info is provided. "
                    + "Never ask for info the user already gave. Never switch to hotels unless the user asks for them. "
                    + "Keep responses short, direct, and conversational. If the user is frustrated or swears, say: 'Chill, mate, let’s get this sorted! What’s next?' "
                    + "Use the full conversation history to avoid repeating questions.";

    private final RestTemplate restTemplate = new RestTemplate();
    // Store conversation history per session (simplified for now)
    private final Map<String, List<Map<String, String>>> sessionHistory = new HashMap<>();

    public String askAssistant(String userMessage, String sessionId) {
        String url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash-latest:generateContent";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("x-goog-api-key", apiKey);

        // Get or initialize session history
        List<Map<String, String>> history = sessionHistory.computeIfAbsent(sessionId, k -> new ArrayList<>());

        // Add user message to history
        history.add(Map.of("role", "user", "content", userMessage));

        // Build request body with system prompt and full history
// Build request body with system prompt and full history
        Map<String, Object> requestBody = new HashMap<>();
        List<Map<String, Object>> contents = new ArrayList<>();

// Add system prompt as the first message with "assistant" role
        Map<String, Object> systemContent = new HashMap<>();
        systemContent.put("role", "assistant");  // Changed from "system" to "assistant"
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
            ResponseEntity<Map> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    new HttpEntity<>(requestBody, headers),
                    Map.class
            );

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                Map<String, Object> responseBody = response.getBody();
                List<Map<String, Object>> candidates = (List<Map<String, Object>>) responseBody.get("candidates");

                if (candidates != null && !candidates.isEmpty()) {
                    Map<String, Object> firstCandidate = candidates.get(0);
                    Map<String, Object> contentResponse = (Map<String, Object>) firstCandidate.get("content");
                    List<Map<String, String>> responseParts = (List<Map<String, String>>) contentResponse.get("parts");

                    if (!responseParts.isEmpty()) {
                        String botResponse = responseParts.get(0).get("text");
                        // Add bot response to history
                        history.add(Map.of("role", "assistant", "content", botResponse));
                        // Limit history to avoid bloat (e.g., last 20 messages)
                        if (history.size() > 20) {
                            history.subList(0, history.size() - 20).clear();
                        }
                        return botResponse;
                    }
                }
            }
            return "Sorry, I couldn't generate a response. Try again.";
        } catch (Exception e) {
            e.printStackTrace();
            return "Error with Gemini API: " + e.getMessage();
        }
    }
}