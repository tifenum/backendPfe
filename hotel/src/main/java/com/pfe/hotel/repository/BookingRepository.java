package com.pfe.hotel.repository;

import com.pfe.hotel.model.Booking;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface BookingRepository extends MongoRepository<Booking, String> {
}