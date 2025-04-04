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
    public List<Map<String, Object>> getFlightOffers(
            @RequestParam String origin,
            @RequestParam String destination,
            @RequestParam String departureDate,
            @RequestParam String returnDate,
            @RequestParam(defaultValue = "1") int adults) {

        return flightFaker.generateFakeFlightOffers(origin, destination, departureDate, returnDate, adults);
    }
}
