package com.pfe.flight.Controller;

import com.pfe.flight.service.FlightFaker;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/flights")
public class FakerController {

    private final FlightFaker flightFaker;

    public FakerController(FlightFaker flightFaker) {
        this.flightFaker = flightFaker;
    }

    @GetMapping("/fake")
    public List<Map<String, Object>> getFlighterOffers(
            @RequestParam String origin,
            @RequestParam String destination,
            @RequestParam(required = false) String departureDate,
            @RequestParam(required = false) String returnDate,
            @RequestParam(defaultValue = "round-trip") String flightType,
            @RequestParam(required = false) String airlineCode) {
        return flightFaker.generateFakeFlightOffers(origin, destination, departureDate, returnDate, flightType, airlineCode);
    }
}