package com.pfe.cars.dao;


import com.pfe.cars.dao.entity.Booking;
import java.util.List;

public interface BookingDao {
    Booking save(Booking booking);
    List<Booking> findByUserId(String userId);
    List<Booking> findByReservationStatus(String reservationStatus); // 👈 Add this
    Booking findById(String id); // Added
    void deleteById(String id); // Added for deletion

}