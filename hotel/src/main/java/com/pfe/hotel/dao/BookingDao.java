package com.pfe.hotel.dao;


import com.pfe.hotel.dao.entity.Booking;
import java.util.List;

public interface BookingDao {
    Booking save(Booking booking);
    List<Booking> findByUserId(String userId);
}