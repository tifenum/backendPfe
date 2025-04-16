package com.pfe.flight.Controller;
import com.pfe.flight.DTO.FlightBookingRequestDto;
import com.pfe.flight.DTO.SlimFlightBookingDto;
import com.pfe.flight.dao.entity.FlightBooking;
import com.pfe.flight.service.FlightService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

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
    public Mono<ResponseEntity<FlightBooking>> bookFlight(@RequestBody FlightBookingRequestDto request) {
        return flightService.bookFlight(request)
                .map(ResponseEntity::ok);
    }

    @GetMapping("/search")
    public Mono<List<Map<String, Object>>> searchFlights(
            @RequestParam String origin,
            @RequestParam String destination,
            @RequestParam String departureDate,
            @RequestParam int adults) {
        return flightService.searchFlights(origin, destination, departureDate, adults);
    }
    @GetMapping("/bookings")
    public Mono<List<SlimFlightBookingDto>> getBookings(@RequestParam String userId) {
        return flightService.getBookingsByUserId(userId)
                .collectList();
    }
    @GetMapping("/all-bookings")
    public Mono<List<SlimFlightBookingDto>> getPendingBookings() {
        return flightService.getPendingBookings().collectList();
    }

    @PutMapping("/bookings/{bookingId}/status")
    public Mono<ResponseEntity<SlimFlightBookingDto>> updateBookingStatus(
            @PathVariable String bookingId,
            @RequestBody Map<String, String> request) {

        return flightService.updateBookingStatus(bookingId, request)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

}
