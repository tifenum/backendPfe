package com.pfe.flight.service;

import com.amadeus.Amadeus;
import com.amadeus.Params;
import com.amadeus.exceptions.ResponseException;
import com.amadeus.resources.FlightOfferSearch;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.pfe.flight.DTO.FlightBookingRequestDto;
import com.pfe.flight.DTO.FlightBookingResponseDto;
import com.pfe.flight.DTO.FlightDetailsDto;
import com.pfe.flight.DTO.SlimFlightBookingDto;
import com.pfe.flight.dao.BookingDao;
import com.pfe.flight.dao.entity.FlightBooking;
import com.pfe.flight.mappers.FlightBookingMapper;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class FlightService {

    private final Amadeus amadeus;
    private final ObjectMapper objectMapper;
    private final Gson gson;
    private final BookingDao bookingDao;

    private final FlightBookingMapper flightBookingMapper;
    public FlightService(Amadeus amadeus, ObjectMapper objectMapper1, BookingDao bookingDao, FlightBookingMapper flightBookingMapper) {
        this.amadeus = amadeus;
        this.objectMapper = objectMapper1;
        this.bookingDao = bookingDao;
        this.flightBookingMapper = flightBookingMapper;
        this.gson = new Gson();
    }



    public Mono<FlightBooking> bookFlight(FlightBookingRequestDto requestDto) {
        try {
            FlightBooking flightBooking = flightBookingMapper.toEntity(requestDto);
            return bookingDao.save(flightBooking);
        } catch (Exception e) {
            return Mono.error(e);
        }
    }


    public Mono<SlimFlightBookingDto> updateBookingStatus(String bookingId, Map<String, String> request) {
        String newStatus = request.get("status");
        if (!"Accepted".equals(newStatus) && !"Refused".equals(newStatus)) {
            return Mono.error(new IllegalArgumentException("Status must be either 'Accepted' or 'Refused'."));
        }

        return bookingDao.updateBookingStatus(bookingId, newStatus)
                .map(flightBookingMapper::mapToSlimDto);
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
    public Flux<SlimFlightBookingDto> getBookingsByUserId(String userId) {
        return bookingDao.findByUserId(userId)
                .map(flightBookingMapper::mapToSlimDto);
    }

    public Flux<SlimFlightBookingDto> getPendingBookings() {
        return bookingDao.findByBookingStatus("Pending")
                .map(flightBookingMapper::mapToSlimDto);
    }

}
