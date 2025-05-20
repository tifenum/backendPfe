package com.pfe.users.feignClient;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import java.util.List;
import java.util.Map;

@FeignClient(name = "cars", url = "${cars.service.url:http://localhost:8222/api/cars}")
public interface CarsServiceClient {
    @GetMapping("/fake")
    List<Map<String, Object>> getFakeCars(
            @RequestParam("pickupCountry") String pickupCountry,
            @RequestParam("pickupCity") String pickupCity
    );
}