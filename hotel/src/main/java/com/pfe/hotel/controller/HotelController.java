package com.pfe.hotel.controller;

import com.pfe.hotel.service.AmadeusService;
import org.springframework.web.bind.annotation.*;
import com.pfe.hotel.service.HotelFakerService;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/hotels")
public class HotelController {

    private final AmadeusService amadeusService;
    private final HotelFakerService hotelFakerService;
    public HotelController(AmadeusService amadeusService, HotelFakerService hotelFakerService) {
        this.amadeusService = amadeusService;
        this.hotelFakerService = hotelFakerService;
    }

    @GetMapping("/search")
    public List<Map<String, Object>> searchHotels(@RequestParam String cityCode) {
        return amadeusService.searchHotels(cityCode);
    }
    @GetMapping("/cities")
    public List<Map<String, Object>> searchCities(@RequestParam String countryCode, @RequestParam String keyword, @RequestParam(defaultValue = "10") int max) {
        return amadeusService.searchCities(countryCode, keyword, max);
    }
    @GetMapping("/fake")
    public Object generateFakeHotel(
            @RequestParam String countryName,
            @RequestParam String stateName,
            @RequestParam double latitude,
            @RequestParam double longitude) {
        return hotelFakerService.generateFakeHotel(countryName, stateName, latitude, longitude);
    }
    @GetMapping("/by-geocode")
    public List<Map<String, Object>> searchHotelsByGeocode(
            @RequestParam double latitude,
            @RequestParam double longitude,
            @RequestParam double radius) {
        return amadeusService.searchHotelsByGeocode(latitude, longitude, radius);
    }
    @GetMapping("/by-keyword")
    public List<Map<String, Object>> searchHotelsByKeyword(@RequestParam String keyword) {
        return amadeusService.searchHotelsByKeyword(keyword);
    }

}
