package com.pfe.cars.dao.impl;

import com.pfe.cars.dao.BookingDao;
import com.pfe.cars.dao.entity.Booking;
import com.pfe.cars.dao.repository.BookingRepository;
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
    @Override
    public List<Booking> findByReservationStatus(String status) {
        return bookingRepository.findByReservationStatus(status);
    }
    @Override
    public Booking findById(String id) {
        return bookingRepository.findById(id).orElse(null);
    }

    @Override
    public void deleteById(String id) {
        bookingRepository.deleteById(id);
    }
}