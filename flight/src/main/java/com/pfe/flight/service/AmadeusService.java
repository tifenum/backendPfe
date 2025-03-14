package com.pfe.flight.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

@Service
public class AmadeusService {

    @Value("${amadeus.api.key}")
    private String apiKey;

    @Value("${amadeus.api.secret}")
    private String apiSecret;

    private final WebClient webClient;

    public AmadeusService(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.baseUrl("https://test.api.amadeus.com").build();
    }

    public Mono<String> getAccessToken() {
        // Step 1: Prepare the request body for Amadeus Authentication API
        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("grant_type", "client_credentials");
        requestBody.put("client_id", apiKey);
        requestBody.put("client_secret", apiSecret);

        // Step 2: Call Amadeus Authentication API
        return webClient.post()
                .uri("/v1/security/oauth2/token")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(String.class);
    }

    public Mono<String> searchFlights(String amadeusToken, Map<String, String> queryParams) {
        // Step 3: Call Amadeus Flight Offers API
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/v2/shopping/flight-offers")
                        .queryParam("originLocationCode", queryParams.get("originLocationCode"))
                        .queryParam("destinationLocationCode", queryParams.get("destinationLocationCode"))
                        .queryParam("departureDate", queryParams.get("departureDate"))
                        .queryParam("adults", queryParams.get("adults"))
                        .build())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + amadeusToken)
                .retrieve()
                .bodyToMono(String.class);
    }
}