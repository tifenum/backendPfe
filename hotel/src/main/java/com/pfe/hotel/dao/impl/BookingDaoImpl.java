package com.pfe.hotel.dao.impl;

import com.pfe.hotel.dao.BookingDao;
import com.pfe.hotel.dao.entity.Booking;
import com.pfe.hotel.dao.repository.BookingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class BookingDaoImpl implements BookingDao {

    @Autowired
    private BookingRepository bookingRepository;

    @Override
    public Booking save(Booking booking) {
        return bookingRepository.save(booking);
    }

    @Override
    public List<Booking> findByUserId(String userId) {
        return bookingRepository.findByUserId(userId);
    }
}