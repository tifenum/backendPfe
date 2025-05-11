package com.pfe.flight.Controller;

import com.pfe.flight.DTO.FlightBookingRequestDto;
import com.pfe.flight.DTO.SlimFlightBookingDto;
import com.pfe.flight.dao.entity.FlightBooking;
import com.pfe.flight.service.FlightService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/flights")
public class FlightController {

    private final FlightService flightService;

    public FlightController(FlightService flightService) {
        this.flightService = flightService;
    }

    @PostMapping("/book-flight")
    public ResponseEntity<FlightBooking> bookFlight(@RequestBody FlightBookingRequestDto request) {
        FlightBooking booking = flightService.bookFlight(request);
        return ResponseEntity.ok(booking);
    }



    @GetMapping("/bookings")
    public List<SlimFlightBookingDto> getBookings(@RequestParam String userId) {
        return flightService.getBookingsByUserId(userId);
    }

    @GetMapping("/all-bookings")
    public List<SlimFlightBookingDto> getPendingBookings() {
        return flightService.getPendingBookings();
    }

    @PutMapping("/bookings/{bookingId}/status")
    public ResponseEntity<SlimFlightBookingDto> updateBookingStatus(
            @PathVariable String bookingId,
            @RequestBody Map<String, String> request) {

        SlimFlightBookingDto updatedBooking = flightService.updateBookingStatus(bookingId, request);
        if (updatedBooking != null) {
            return ResponseEntity.ok(updatedBooking);
        } else {
            return ResponseEntity.notFound().build();
        }
    }
    @DeleteMapping("/bookings/{bookingId}")
    public ResponseEntity<Void> deleteBooking(@PathVariable String bookingId) {
        try {
            flightService.deleteBooking(bookingId);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
