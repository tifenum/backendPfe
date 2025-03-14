package com.pfe.Controller;

import com.pfe.service.AmadeusService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/flights")
public class FlightController {

    @Autowired
    private AmadeusService amadeusService;

    @GetMapping("/search")
    public Mono<ResponseEntity<String>> searchFlights(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String token,
            @RequestParam String origin,
            @RequestParam String destination,
            @RequestParam String departureDate,
            @RequestParam int adults) {

        // Step 1: Validate JWT token (if needed)
        // You can reuse your existing JWT validation logic here.

        // Step 2: Get Amadeus Access Token
        return amadeusService.getAccessToken()
                .flatMap(accessToken -> {
                    // Step 3: Prepare query parameters for Amadeus API
                    Map<String, String> queryParams = new HashMap<>();
                    queryParams.put("originLocationCode", origin);
                    queryParams.put("destinationLocationCode", destination);
                    queryParams.put("departureDate", departureDate);
                    queryParams.put("adults", String.valueOf(adults));

                    // Step 4: Call Amadeus API
                    return amadeusService.searchFlights(accessToken, queryParams)
                            .map(ResponseEntity::ok);
                })
                .onErrorResume(e -> Mono.just(ResponseEntity.badRequest().body("Error: " + e.getMessage())));
    }
}