package com.pfe.cars.controller;
import com.pfe.cars.DTO.BookingRequest;
import com.pfe.cars.DTO.BookingResponseDTO;
import com.pfe.cars.service.AmadeusService;
import com.pfe.cars.service.CarBookingService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.pfe.cars.service.CarFakerService;
import com.pfe.cars.dao.entity.Booking;
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
@RequestMapping("/api/cars")
public class CarsController {

    private final AmadeusService amadeusService;
    private final CarFakerService carFakerService;
    private final CarBookingService bookingService;
    public CarsController(AmadeusService amadeusService, CarFakerService carFakerService, CarBookingService bookingService) {
        this.amadeusService = amadeusService;
        this.carFakerService = carFakerService;
        this.bookingService = bookingService;
    }
    @GetMapping("/cities")
    public List<Map<String, Object>> searchCities(@RequestParam String countryCode, @RequestParam String keyword, @RequestParam(defaultValue = "10") int max) {
        return amadeusService.searchCities(countryCode, keyword, max);
    }
    @GetMapping("/fake")
    public ResponseEntity<?> searchCars(
            @RequestParam String pickupCountry,
            @RequestParam String pickupCity,
            @RequestParam(required = false) String carType,
            @RequestParam(required = false) String passengers,
            @RequestParam(required = false) String transmission) {

        if (pickupCountry == null || pickupCountry.trim().isEmpty()) {
            return ResponseEntity.badRequest().body("pickupCountry is required");
        }
        if (pickupCity == null || pickupCity.trim().isEmpty()) {
            return ResponseEntity.badRequest().body("pickupCity is required");
        }

        CarFakerService.Car car = carFakerService.generateFakeCar(
                pickupCountry, pickupCity, carType, passengers, transmission);
        return ResponseEntity.ok(car);
    }

    @PostMapping("/book")
    public ResponseEntity<Booking> createBooking(@RequestBody BookingRequest bookingRequest) {
        return ResponseEntity.ok(bookingService.createBookingFromRequest(bookingRequest));
    }
    @GetMapping("/reservations")
    public ResponseEntity<List<BookingResponseDTO>> getReservations(@RequestParam String userId) {
        List<BookingResponseDTO> bookingResponseDTOList = bookingService.getReservationsByUserId(userId);

        if (bookingResponseDTOList.isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(bookingResponseDTOList);
    }
    @GetMapping("/all-reservations")
    public ResponseEntity<List<BookingResponseDTO>> getAllPendingReservations() {
        List<BookingResponseDTO> pendingReservations = bookingService.getPendingReservations();

        if (pendingReservations.isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(pendingReservations);
    }
    @PutMapping("/reservations/{reservationId}/status")
    public ResponseEntity<BookingResponseDTO> updateReservationStatus(
            @PathVariable String reservationId,
            @RequestBody Map<String, String> request) {
        String newStatus = request.get("status");
        try {
            BookingResponseDTO updatedDto = bookingService.updateReservationStatus(reservationId, newStatus);
            if (updatedDto == null) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(updatedDto);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(null);
        }
    }
    @DeleteMapping("/reservations/{reservationId}")
    public ResponseEntity<Void> deleteReservation(@PathVariable String reservationId) {
        try {
            bookingService.deleteBooking(reservationId);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
