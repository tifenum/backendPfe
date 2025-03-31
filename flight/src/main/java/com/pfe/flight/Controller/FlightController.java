package com.pfe.flight.Controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pfe.flight.model.FlightBooking;
import com.pfe.flight.service.FlightService;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

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
    public Mono<FlightBooking> bookFlight(@RequestBody String rawRequestBody) throws JsonProcessingException {
        System.out.println("Received raw request body: " + rawRequestBody);

        ObjectMapper objectMapper = new ObjectMapper();
        FlightBooking flightBooking = objectMapper.readValue(rawRequestBody, FlightBooking.class);

        System.out.println("Parsed FlightBooking object: " + flightBooking);

        return flightService.bookFlight(flightBooking);
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
