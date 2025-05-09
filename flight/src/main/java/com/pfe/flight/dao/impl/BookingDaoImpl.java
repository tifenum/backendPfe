package com.pfe.flight.dao.impl;

import com.pfe.flight.dao.BookingDao;
import com.pfe.flight.dao.entity.FlightBooking;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
import com.pfe.flight.dao.repository.flightBookingRepository;
@Repository
public class BookingDaoImpl implements BookingDao {

    private final flightBookingRepository FlightBookingRepository; // Use constructor injection

    @Autowired
    public BookingDaoImpl(flightBookingRepository FlightBookingRepository) {
        this.FlightBookingRepository = FlightBookingRepository; // Inject repository through constructor
    }
    @Override
    public FlightBooking findById(String id) {
        return FlightBookingRepository.findById(id).orElse(null);
    }

    @Override
    public FlightBooking save(FlightBooking booking) {
        return FlightBookingRepository.save(booking);
    }
    @Override
    public List<FlightBooking> findByUserId(String userId) {
        return FlightBookingRepository.findByUserId(userId);
    }
    @Override
    public List<FlightBooking> findByBookingStatus(String bookingStatus) {
        return FlightBookingRepository.findByBookingStatus(bookingStatus);
    }
    @Override
    public Optional<FlightBooking> updateBookingStatus(String bookingId, String newStatus) {
        return FlightBookingRepository.findById(bookingId)
                .map(booking -> {
                    booking.setBookingStatus(newStatus);
                    return FlightBookingRepository.save(booking); // returns FlightBooking
                });
    }
    @Override
    public void deleteById(String bookingId) {
        FlightBookingRepository.deleteById(bookingId);
    }
}