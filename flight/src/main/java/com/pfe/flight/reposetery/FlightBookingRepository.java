package com.pfe.flight.reposetery;


import com.pfe.flight.model.FlightBooking;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

public interface FlightBookingRepository extends ReactiveMongoRepository<FlightBooking, String> {
}
