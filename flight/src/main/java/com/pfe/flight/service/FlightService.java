package com.pfe.flight.service;

import com.amadeus.Amadeus;
import com.amadeus.Params;
import com.amadeus.exceptions.ResponseException;
import com.amadeus.resources.FlightOfferSearch;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class FlightService {

    private final Amadeus amadeus;
    private final ObjectMapper objectMapper;
    private final Gson gson;

    public FlightService(Amadeus amadeus, ObjectMapper objectMapper) {
        this.amadeus = amadeus;
        this.objectMapper = objectMapper;
        this.gson = new Gson();
    }

    public Mono<List<Map<String, Object>>> searchFlights(String origin, String destination, String departureDate, int adults) {
        return Mono.fromCallable(() -> {
            try {
                // Make the request to Amadeus API to get flight offers
                FlightOfferSearch[] offers = amadeus.shopping.flightOffersSearch.get(Params
                        .with("originLocationCode", origin)
                        .and("destinationLocationCode", destination)
                        .and("departureDate", departureDate)
                        .and("adults", adults)
                        .and("max", 3));

                // Convert each FlightOfferSearch to a Map for safe JSON serialization
                return Arrays.stream(offers)
                        .map(offer -> {
                            // First, convert the offer to a JSON string using Gson
                            String json = gson.toJson(offer);

                            // Use Gson to parse the response manually
                            JsonElement jsonElement = gson.fromJson(json, JsonElement.class);
                            // Handle the "response" field if necessary (ensure it's a JsonArray or JsonObject)
                            JsonElement responseElement = jsonElement.getAsJsonObject().get("response");

                            // Manually handle the issue where the data field might be an array of integers
                            if (responseElement != null && responseElement.isJsonArray()) {
                                // Optionally, process the array to ensure the size or structure fits the expected format
                                // For instance, you could take the first element if that's what is required
                                JsonElement dataElement = responseElement.getAsJsonArray().get(0);  // Adjust based on the structure
                                jsonElement.getAsJsonObject().add("response", dataElement);
                            }

                            // Convert the modified JSON back into a Map using Jackson
                            try {
                                return objectMapper.readValue(jsonElement.toString(), new TypeReference<Map<String, Object>>() {});
                            } catch (Exception e) {
                                throw new RuntimeException("Conversion failed", e);
                            }
                        })
                        .collect(Collectors.toList());
            } catch (ResponseException e) {
                throw new RuntimeException("Flight search failed: " + e.getDescription(), e);
            }
        });
    }
}
