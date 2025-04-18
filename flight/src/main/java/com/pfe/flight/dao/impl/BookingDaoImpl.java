package com.pfe.flight.dao.impl;

import com.pfe.flight.dao.BookingDao;
import com.pfe.flight.dao.entity.FlightBooking;
import com.pfe.flight.dao.repository.FlightBookingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public class BookingDaoImpl implements BookingDao {

    @Autowired
    private FlightBookingRepository FlightBookingRepository;

    @Override
    public Mono<FlightBooking> save(FlightBooking booking) {
        return FlightBookingRepository.save(booking);
    }
    @Override
    public Flux<FlightBooking> findByUserId(String userId) {
        return FlightBookingRepository.findByUserId(userId);
    }
    @Override
    public Flux<FlightBooking> findByBookingStatus(String bookingStatus) {
        return FlightBookingRepository.findByBookingStatus(bookingStatus);
    }
    @Override
    public Mono<FlightBooking> updateBookingStatus(String bookingId, String newStatus) {
        return FlightBookingRepository.findById(bookingId)
                .flatMap(booking -> {
                    booking.setBookingStatus(newStatus);
                    return FlightBookingRepository.save(booking);
                });
    }
}