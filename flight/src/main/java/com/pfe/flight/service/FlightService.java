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
        return flightBookingRepository.save(flightBooking);
    }
    public Mono<List<Map<String, Object>>> searchFlights(String origin, String destination, String departureDate, int adults) {
        return Mono.fromCallable(() -> {
            try {
                FlightOfferSearch[] offers = amadeus.shopping.flightOffersSearch.get(Params
                        .with("originLocationCode", origin)
                        .and("destinationLocationCode", destination)
                        .and("departureDate", departureDate)
                        .and("adults", adults)
                        .and("max", 3));

                return Arrays.stream(offers)
                        .map(offer -> {
                            String json = gson.toJson(offer);

                            JsonElement jsonElement = gson.fromJson(json, JsonElement.class);
                            JsonElement responseElement = jsonElement.getAsJsonObject().get("response");

                            if (responseElement != null && responseElement.isJsonArray()) {
                                JsonElement dataElement = responseElement.getAsJsonArray().get(0);
                                jsonElement.getAsJsonObject().add("response", dataElement);
                            }

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
