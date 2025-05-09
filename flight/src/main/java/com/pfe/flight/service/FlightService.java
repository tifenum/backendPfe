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

    public FlightService(Amadeus amadeus, BookingDao bookingDao, FlightBookingMapper flightBookingMapper) {
        this.amadeus = amadeus;
        this.bookingDao = bookingDao;
        this.flightBookingMapper = flightBookingMapper;
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

    public List<Map<String, Object>> searchFlights(String origin, String destination, String departureDate, int adults) {
        try {
            FlightOfferSearch[] offers = amadeus.shopping.flightOffersSearch.get(Params
                    .with("originLocationCode", origin)
                    .and("destinationLocationCode", destination)
                    .and("departureDate", departureDate)
                    .and("adults", adults)
                    .and("max", 3));

            return Arrays.stream(offers)
                    .map(offer -> {
                        String json = gson.toJson(offer);
                        Map<String, Object> map = gson.fromJson(json, new TypeToken<Map<String, Object>>(){}.getType());
                        if (map.containsKey("response") && map.get("response") instanceof List) {
                            List<?> responseList = (List<?>) map.get("response");
                            if (!responseList.isEmpty()) {
                                map.put("response", responseList.get(0));
                            }
                        }
                        return map;
                    })
                    .collect(Collectors.toList());
        } catch (ResponseException e) {
            throw new RuntimeException("Flight search failed: " + e.getDescription(), e);
        }
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