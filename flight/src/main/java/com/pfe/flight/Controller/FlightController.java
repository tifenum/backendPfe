package com.pfe.flight.Controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pfe.flight.DTO.FlightBookingRequestDto;
import com.pfe.flight.DTO.FlightBookingResponseDto;
import com.pfe.flight.dao.entity.FlightBooking;
import com.pfe.flight.service.FlightService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import javax.validation.Valid;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/flights")
public class FlightController {

    private final FlightService flightService;

    public FlightController(FlightService flightService) {
        this.flightService = flightService;
    }

    @PostMapping("/book-flight")
    public Mono<ResponseEntity<FlightBooking>> bookFlight(
            @RequestBody FlightBookingRequestDto request) {
        return flightService.bookFlight(request)
                .map(ResponseEntity::ok)
                .onErrorResume(e ->
                        Mono.just(ResponseEntity
                                .status(500)
                                .body(new FlightBooking()))
                );
    }
    @GetMapping("/search")
    public Mono<List<Map<String, Object>>> searchFlights(
            @RequestParam String origin,
            @RequestParam String destination,
            @RequestParam String departureDate,
            @RequestParam int adults) {
        return flightService.searchFlights(origin, destination, departureDate, adults);
    }
}
