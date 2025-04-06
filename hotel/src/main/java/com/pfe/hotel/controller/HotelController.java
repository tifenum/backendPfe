package com.pfe.hotel.controller;

import com.pfe.hotel.DTO.BookingRequest;
import com.pfe.hotel.DTO.BookingResponseDTO;
import com.pfe.hotel.service.AmadeusService;
import com.pfe.hotel.service.BookingService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.pfe.hotel.service.HotelFakerService;
import com.pfe.hotel.dao.repository.BookingRepository;
import com.pfe.hotel.dao.entity.Booking;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
@RestController
@RequestMapping("/api/hotels")
public class HotelController {

    private final AmadeusService amadeusService;
    private final HotelFakerService hotelFakerService;
    private final BookingService bookingService;
    public HotelController(AmadeusService amadeusService, HotelFakerService hotelFakerService, BookingService bookingService) {
        this.amadeusService = amadeusService;
        this.hotelFakerService = hotelFakerService;
        this.bookingService = bookingService;
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
            @RequestParam double latitude,
            @RequestParam double longitude,
            @RequestParam String hotelName) {

        Map<String, String> locationData = getLocationFromCoordinates(latitude, longitude);
        String countryName = locationData.get("country");
        String stateName = locationData.get("state");

        return hotelFakerService.generateFakeHotel(hotelName, countryName, stateName, latitude, longitude);
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
    private Map<String, String> getLocationFromCoordinates(double latitude, double longitude) {
        String url = String.format(Locale.US, "https://nominatim.openstreetmap.org/reverse?format=json&lat=%f&lon=%f", latitude, longitude);
        RestTemplate restTemplate = new RestTemplate();

        try {
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            if (!response.getStatusCode().is2xxSuccessful()) {
                throw new RuntimeException("Nominatim API returned " + response.getStatusCode() + ": " + response.getBody());
            }

            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(response.getBody());
            String country = root.path("address").path("country").asText("Unknown Country");
            String state = root.path("address").path("state").asText();

            if (state.isEmpty()) {
                state = root.path("address").path("region").asText("Unknown State");
            }

            return Map.of("country", country, "state", state);
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch location data, bro! " + e.getMessage(), e);
        }
    }
    @PostMapping("/book")
    public ResponseEntity<Booking> createBooking(@RequestBody BookingRequest bookingRequest) {
        return ResponseEntity.ok(bookingService.createBookingFromRequest(bookingRequest));
    }
    @GetMapping("/reservations")
    public ResponseEntity<List<BookingResponseDTO>> getReservations(@RequestParam String userId) {
        List<BookingResponseDTO> bookingResponseDTOList = bookingService.getReservationsByUserId(userId);

        if (bookingResponseDTOList.isEmpty()) {
            return ResponseEntity.noContent().build(); // No reservations found
        }

        return ResponseEntity.ok(bookingResponseDTOList); // Return the DTO list in the response
    }

}
