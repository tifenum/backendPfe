package com.pfe.flight.dao.repository;


import com.pfe.flight.dao.entity.FlightBooking;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

public interface FlightBookingRepository extends ReactiveMongoRepository<FlightBooking, String> {
}
