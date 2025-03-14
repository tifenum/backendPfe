package com.pfe.flight.Controller;

import com.pfe.flight.service.AmadeusService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/flights")
public class FlightController {

    @Autowired
    private AmadeusService AmadeusService;

    @GetMapping("/search")
    public Mono<ResponseEntity<String>> searchFlights(
            @RequestParam String origin,
            @RequestParam String destination,
            @RequestParam String departureDate,
            @RequestParam int adults) {

        // Step 1: Get Amadeus Access Token
        return AmadeusService.getAccessToken()
                .flatMap(amadeusToken -> {
                    // Step 2: Prepare query parameters for Amadeus API
                    Map<String, String> queryParams = new HashMap<>();
                    queryParams.put("originLocationCode", origin);
                    queryParams.put("destinationLocationCode", destination);
                    queryParams.put("departureDate", departureDate);
                    queryParams.put("adults", String.valueOf(adults));

                    // Step 3: Call Amadeus API
                    return AmadeusService.searchFlights(amadeusToken, queryParams)
                            .map(ResponseEntity::ok);
                })
                .onErrorResume(e -> Mono.just(ResponseEntity.badRequest().body("Error: " + e.getMessage())));
    }
}