package com.pfe.hotel.service;

import com.amadeus.Amadeus;
import com.amadeus.Params;
import com.amadeus.Response;
import com.amadeus.exceptions.ResponseException;
import com.amadeus.resources.Resource;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.pfe.hotel.config.AmadeusProperties;
import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AmadeusService {

    private final Amadeus amadeus;
    private final ObjectMapper objectMapper;
    private final Gson gson;

    public AmadeusService(ObjectMapper objectMapper, AmadeusProperties properties) {
        String apiKey = properties.getKey();
        String apiSecret = properties.getSecret();

        if (apiKey == null || apiSecret == null) {
            throw new IllegalArgumentException("Missing Amadeus API keys in configuration");
        } else {
            System.out.println("API Key: " + apiKey);
            System.out.println("API Secret: " + apiSecret);
        }

        this.amadeus = Amadeus.builder(apiKey, apiSecret).build();
        this.objectMapper = objectMapper;
        this.gson = new Gson();
    }

    public List<Map<String, Object>> searchHotels(String cityCode) {
        try {
            // Retrieve hotels using Amadeus API
            Resource[] hotels = amadeus.referenceData.locations.hotels.byCity.get(Params.with("cityCode", cityCode));

            // Convert each Resource into a JSON-friendly Map using Gson and Jackson
            return Arrays.stream(hotels)
                    .limit(15)
                    .map(resource -> {
                        // Convert the Resource into a JSON string using Gson
                        String json = gson.toJson(resource);
                        // Now convert the JSON string into a Map<String, Object> using Jackson
                        try {
                            return objectMapper.readValue(json, new TypeReference<Map<String, Object>>() {});
                        } catch (Exception e) {
                            throw new RuntimeException("Conversion failed", e);
                        }
                    })
                    .collect(Collectors.toList());
        } catch (ResponseException e) {
            throw new RuntimeException("Hotel search failed: " + e.getDescription(), e);
        }
    }

    public List<Map<String, Object>> searchCities(String countryCode, String keyword, int max) {
        try {
            Response response = amadeus.get("/v1/reference-data/locations/cities",
                    Params.with("countryCode", countryCode)
                            .and("keyword", keyword)
                            .and("max", max));

            String jsonResponse = response.getBody();
            System.out.println("Raw Response from Amadeus API: " + jsonResponse);

            // Convert JSON response into a Map
            Map<String, Object> responseMap = objectMapper.readValue(jsonResponse, new TypeReference<Map<String, Object>>() {});

            // Extract "data" array
            List<Map<String, Object>> cities = (List<Map<String, Object>>) responseMap.get("data");

            return cities;  // Return directly instead of unnecessary conversion to `Resource[]`
        } catch (ResponseException e) {
            throw new RuntimeException("City search failed: " + e.getDescription(), e);
        } catch (Exception e) {
            throw new RuntimeException("JSON Parsing failed", e);
        }
    }
    public List<Map<String, Object>> searchHotelsByGeocode(double latitude, double longitude, double radius) {
        try {
            Response response = amadeus.get("/v1/reference-data/locations/hotels/by-geocode",
                    Params.with("latitude", String.valueOf(latitude))
                            .and("longitude", String.valueOf(longitude))
                            .and("radius", String.valueOf(radius))
                            .and("radiusUnit", "KM")); // Always set unit to kilometers

            String jsonResponse = response.getBody();
            Map<String, Object> responseMap = objectMapper.readValue(jsonResponse,
                    new TypeReference<Map<String, Object>>() {});

            List<Map<String, Object>> hotels = (List<Map<String, Object>>) responseMap.get("data");

            return hotels != null ? hotels : List.of();

        } catch (ResponseException e) {
            throw new RuntimeException("Hotel search by geocode failed: " + e.getDescription(), e);
        } catch (Exception e) {
            throw new RuntimeException("JSON Parsing failed", e);
        }
    }
    public List<Map<String, Object>> searchHotelsByKeyword(String keyword) {
        try {
            Response response = amadeus.get("/v1/reference-data/locations/hotel",
                    Params.with("keyword", keyword)
                            .and("subType", "HOTEL_LEISURE"));

            String jsonResponse = response.getBody();
            Map<String, Object> responseMap = objectMapper.readValue(jsonResponse,
                    new TypeReference<Map<String, Object>>() {});

            List<Map<String, Object>> hotels = (List<Map<String, Object>>) responseMap.get("data");

            return hotels != null ? hotels : List.of();
        } catch (ResponseException e) {
            throw new RuntimeException("Hotel search by keyword failed: " + e.getDescription(), e);
        } catch (Exception e) {
            throw new RuntimeException("JSON Parsing failed", e);
        }
    }

}
