package com.pfe.cars.dao.repository;

import com.pfe.cars.dao.entity.Booking;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface BookingRepository extends MongoRepository<Booking, String> {
    List<Booking> findByUserId(String userId);
    List<Booking> findByReservationStatus(String status); // 👈 Add this

}