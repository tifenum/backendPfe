package com.pfe.flight.service;

import com.amadeus.Amadeus;
import com.amadeus.Params;
import com.amadeus.exceptions.ResponseException;
import com.amadeus.resources.FlightOfferSearch;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.pfe.flight.model.FlightBooking;
import com.pfe.flight.reposetery.FlightBookingRepository;
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
    private final FlightBookingRepository flightBookingRepository;

    public FlightService(Amadeus amadeus, ObjectMapper objectMapper, FlightBookingRepository flightBookingRepository) {
        this.amadeus = amadeus;
        this.objectMapper = objectMapper;
        this.flightBookingRepository = flightBookingRepository;
        this.gson = new Gson();
    }
    public Mono<FlightBooking> bookFlight(FlightBooking flightBooking) {
        // Logic to save the booking
        return flightBookingRepository.save(flightBooking);
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

                return Arrays.stream(offers)
                        .map(offer -> {
                            // First, convert the offer to a JSON string using Gson
                            String json = gson.toJson(offer);

                            // Use Gson to parse the response manually
                            JsonElement jsonElement = gson.fromJson(json, JsonElement.class);
                            JsonElement responseElement = jsonElement.getAsJsonObject().get("response");

                            // Manually handle the issue where the data field might be an array of integers
                            if (responseElement != null && responseElement.isJsonArray()) {
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
