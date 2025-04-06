package com.pfe.flight.dao;

import com.pfe.flight.dao.entity.FlightBooking;
import reactor.core.publisher.Mono;

public interface BookingDao {
    Mono<FlightBooking> save(FlightBooking booking);
}