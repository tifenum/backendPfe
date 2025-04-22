package com.pfe.flight.dao.repository;


import com.pfe.flight.dao.entity.FlightBooking;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;

public interface flightBookingRepository extends MongoRepository<FlightBooking, String> {
    List<FlightBooking> findByUserId(String userId);
    List<FlightBooking> findByBookingStatus(String bookingStatus);
}
