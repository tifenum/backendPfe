package com.pfe.hotel.service;

import com.pfe.hotel.DTO.BookingResponseDTO;
import com.pfe.hotel.dao.BookingDao;
import com.pfe.hotel.dao.entity.Booking;
import com.pfe.hotel.DTO.BookingRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class BookingService {

    @Autowired
    private BookingDao bookingDao;

    public Booking createBooking(Booking booking) {
        return bookingDao.save(booking);
    }

    public Booking createBookingFromRequest(BookingRequest bookingRequest) {
        LocalDate checkInDate = LocalDate.parse(bookingRequest.getCheckInDate());
        LocalDate checkOutDate = LocalDate.parse(bookingRequest.getCheckOutDate());

        Booking booking = new Booking(
                bookingRequest.getUserId(),
                bookingRequest.getHotelName(),
                bookingRequest.getHotelAddress(),
                bookingRequest.getRoomType(),
                bookingRequest.getRoomFeatures(),
                bookingRequest.getRoomPricePerNight(),
                checkInDate,
                checkOutDate,
                bookingRequest.getNotes(),
                bookingRequest.getTotalPrice()
        );

        return createBooking(booking);
    }
    public List<BookingResponseDTO> getReservationsByUserId(String userId) {
        List<Booking> reservations = bookingDao.findByUserId(userId);

        if (reservations.isEmpty()) {
            return List.of(); // Return an empty list if no reservations found
        }

        return reservations.stream()
                .map(reservation -> new BookingResponseDTO(
                        reservation.getHotelName(),
                        reservation.getHotelAddress(),
                        reservation.getRoomType(),
                        reservation.getRoomFeatures(),
                        reservation.getRoomPricePerNight(),
                        reservation.getCheckInDate(),
                        reservation.getCheckOutDate(),
                        reservation.getNotes(),
                        reservation.getTotalPrice(),
                        reservation.getReservationStatus()
                ))
                .collect(Collectors.toList());
    }
}