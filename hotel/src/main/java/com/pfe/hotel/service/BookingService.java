package com.pfe.hotel.service;

import com.pfe.hotel.DTO.BookingResponseDTO;
import com.pfe.hotel.DTO.ClientUserDTO;
import com.pfe.hotel.dao.BookingDao;
import com.pfe.hotel.dao.entity.Booking;
import com.pfe.hotel.DTO.BookingRequest;
import com.pfe.hotel.feignClient.UserServiceFeignClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class BookingService {

    @Autowired
    private BookingDao bookingDao;
    @Autowired
    private UserServiceFeignClient userServiceFeignClient;

    @Autowired
    private EmailService emailService;
    public Booking createBooking(Booking booking) {
        return bookingDao.save(booking);
    }

    public Booking createBookingFromRequest(BookingRequest bookingRequest) {
        String checkInDate = bookingRequest.getCheckInDate();
        String checkOutDate = bookingRequest.getCheckOutDate();

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
            return List.of();
        }

        return reservations.stream()
                .map(reservation -> new BookingResponseDTO(
                        reservation.getId(),
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


    public BookingResponseDTO updateReservationStatus(String reservationId, String newStatus) {
        if (!"Accepted".equals(newStatus) && !"Refused".equals(newStatus)) {
            throw new IllegalArgumentException("Invalid status: " + newStatus);
        }

        Booking reservation = bookingDao.findById(reservationId);
        if (reservation == null) {
            return null;
        }

        reservation.setReservationStatus(newStatus);
        Booking updatedBooking = bookingDao.save(reservation);

        // Fetch user email using Feign client
        ResponseEntity<ClientUserDTO> userResponse = userServiceFeignClient.getUserById(reservation.getUserId());
        if (userResponse.getStatusCode().is2xxSuccessful() && userResponse.getBody() != null) {
            ClientUserDTO user = userResponse.getBody();
            // Send email notification
            emailService.sendBookingStatusEmail(
                    user.getEmail(),
                    updatedBooking.getHotelName(),
                    newStatus,
                    reservationId
            );
        }

        return new BookingResponseDTO(
                updatedBooking.getId(),
                updatedBooking.getHotelName(),
                updatedBooking.getHotelAddress(),
                updatedBooking.getRoomType(),
                updatedBooking.getRoomFeatures(),
                updatedBooking.getRoomPricePerNight(),
                updatedBooking.getCheckInDate(),
                updatedBooking.getCheckOutDate(),
                updatedBooking.getNotes(),
                updatedBooking.getTotalPrice(),
                updatedBooking.getReservationStatus()
        );
    }
    public List<BookingResponseDTO> getPendingReservations() {
        List<Booking> reservations = bookingDao.findByReservationStatus("Pending");

        return reservations.stream()
                .map(res -> new BookingResponseDTO(
                        res.getId(),
                        res.getHotelName(),
                        res.getHotelAddress(),
                        res.getRoomType(),
                        res.getRoomFeatures(),
                        res.getRoomPricePerNight(),
                        res.getCheckInDate(),
                        res.getCheckOutDate(),
                        res.getNotes(),
                        res.getTotalPrice(),
                        res.getReservationStatus()
                ))
                .collect(Collectors.toList());
    }
    public void deleteBooking(String reservationId) {
        Booking reservation = bookingDao.findById(reservationId);
        if (reservation == null) {
            throw new IllegalArgumentException("Reservation not found: " + reservationId);
        }
        bookingDao.deleteById(reservationId);
    }
}