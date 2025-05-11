package com.pfe.flight.service;

import com.amadeus.Amadeus;
import com.amadeus.Params;
import com.amadeus.exceptions.ResponseException;
import com.amadeus.resources.FlightOfferSearch;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.pfe.flight.DTO.FlightBookingRequestDto;
import com.pfe.flight.DTO.SlimFlightBookingDto;
import com.pfe.flight.dao.BookingDao;
import com.pfe.flight.dao.entity.FlightBooking;
import com.pfe.flight.mappers.FlightBookingMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class FlightService {

    private final Amadeus amadeus;
    private final Gson gson;
    private final BookingDao bookingDao;
    private final FlightBookingMapper flightBookingMapper;
    private final FlightFaker flightFaker;

    public FlightService(Amadeus amadeus, BookingDao bookingDao, FlightBookingMapper flightBookingMapper, FlightFaker flightFaker) {
        this.amadeus = amadeus;
        this.bookingDao = bookingDao;
        this.flightBookingMapper = flightBookingMapper;
        this.flightFaker = flightFaker;
        this.gson = new Gson();
    }

    public FlightBooking bookFlight(FlightBookingRequestDto requestDto) {
        FlightBooking flightBooking = flightBookingMapper.toEntity(requestDto);
        return bookingDao.save(flightBooking);
    }

    public SlimFlightBookingDto updateBookingStatus(String bookingId, Map<String, String> request) {
        String newStatus = request.get("status");
        if (!"Accepted".equals(newStatus) && !"Refused".equals(newStatus)) {
            throw new IllegalArgumentException("Status must be either 'Accepted' or 'Refused'.");
        }

        Optional<FlightBooking> updated = bookingDao.updateBookingStatus(bookingId, newStatus);
        return updated.map(flightBookingMapper::mapToSlimDto)
                .orElse(null);
    }

    public List<Map<String, Object>> searchFlights(String origin, String destination, String departureDate, String returnDate, String flightType) {
        // Default departureDate to today + 7 days if not provided
        String effectiveDepartureDate = departureDate != null && !departureDate.isEmpty()
                ? departureDate
                : LocalDate.now().plusDays(7).format(DateTimeFormatter.ISO_LOCAL_DATE);

        // Use FlightFaker for fake data (remove Amadeus for now, as per your setup)
        return flightFaker.generateFakeFlightOffers(origin, destination, effectiveDepartureDate, returnDate, flightType, "");
    }

    public List<SlimFlightBookingDto> getBookingsByUserId(String userId) {
        return bookingDao.findByUserId(userId)
                .stream()
                .map(flightBookingMapper::mapToSlimDto)
                .collect(Collectors.toList());
    }

    public List<SlimFlightBookingDto> getPendingBookings() {
        return bookingDao.findByBookingStatus("Pending")
                .stream()
                .map(flightBookingMapper::mapToSlimDto)
                .collect(Collectors.toList());
    }

    public void deleteBooking(String bookingId) {
        Optional<FlightBooking> booking = Optional.ofNullable(bookingDao.findById(bookingId));
        if (booking.isEmpty()) {
            throw new IllegalArgumentException("Booking not found: " + bookingId);
        }
        bookingDao.deleteById(bookingId);
    }
}
