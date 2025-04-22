package com.pfe.flight.dao;

import com.pfe.flight.dao.entity.FlightBooking;

import java.util.List;
import java.util.Optional;

public interface BookingDao {
    FlightBooking save(FlightBooking booking);
    List<FlightBooking> findByUserId(String userId);
    List<FlightBooking> findByBookingStatus(String bookingStatus);
    Optional<FlightBooking> updateBookingStatus(String bookingId, String newStatus);
}