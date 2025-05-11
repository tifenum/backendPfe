package com.pfe.users.feignClient;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import java.util.List;
import java.util.Map;

@FeignClient(name = "flight", url = "http://localhost:8222/api/flights")
public interface FlightServiceClient {
    @GetMapping("/fake")
    List<Map<String, Object>> getFakeFlightOffers(
            @RequestParam("origin") String origin,
            @RequestParam("destination") String destination,
            @RequestParam("departureDate") String departureDate,
            @RequestParam(value = "returnDate", required = false) String returnDate,
            @RequestParam(value = "flightType") String flightType // Added flightType
    );
}