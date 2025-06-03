package com.pfe.cars.service;

import com.pfe.cars.DTO.BookingRequest;
import com.pfe.cars.DTO.BookingResponseDTO;
import com.pfe.cars.dao.BookingDao;
import com.pfe.cars.dao.entity.Booking;
import com.pfe.cars.DTO.ClientUserDTO;
import com.pfe.cars.feignClient.UserServiceFeignClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CarBookingService {

    @Autowired
    private BookingDao carBookingDao;

    @Autowired
    private UserServiceFeignClient userServiceFeignClient;

    @Autowired
    private EmailService emailService;

    public Booking createBooking(Booking carBooking) {
        return carBookingDao.save(carBooking);
    }

    public Booking createBookingFromRequest(BookingRequest bookingRequest) {
        Booking carBooking = new Booking(
                bookingRequest.getUserId(),
                bookingRequest.getPickupCountry(),
                bookingRequest.getPickupCity(),
                bookingRequest.getCarType(),
                bookingRequest.getCarFeatures(),
                bookingRequest.getPickupDate(),
                bookingRequest.getPickupTime(),
                bookingRequest.getDropoffDate(),
                bookingRequest.getDropoffTime(),
                bookingRequest.getNotes(),
                bookingRequest.getTotalPrice(),
                bookingRequest.getPricePerDay()
        );
        return createBooking(carBooking);
    }

    public List<BookingResponseDTO> getReservationsByUserId(String userId) {
        List<Booking> reservations = carBookingDao.findByUserId(userId);

        if (reservations.isEmpty()) {
            return List.of();
        }

        return reservations.stream()
                .map(reservation -> new BookingResponseDTO(
                        reservation.getId(),
                        reservation.getUserId(),
                        reservation.getPickupCountry(),
                        reservation.getPickupCity(),
                        reservation.getCarType(),
                        reservation.getCarFeatures(),
                        reservation.getPricePerDay(),
                        reservation.getPickupDate(),
                        reservation.getPickupTime(),
                        reservation.getDropoffDate(),
                        reservation.getDropoffTime(),
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

        Booking reservation = carBookingDao.findById(reservationId);
        if (reservation == null) {
            return null;
        }

        reservation.setReservationStatus(newStatus);
        Booking updatedBooking = carBookingDao.save(reservation);

        // Fetch user email using Feign client
        ResponseEntity<ClientUserDTO> userResponse = userServiceFeignClient.getUserById(reservation.getUserId());
        if (userResponse.getStatusCode().is2xxSuccessful() && userResponse.getBody() != null) {
            ClientUserDTO user = userResponse.getBody();
            // Send email notification
            emailService.sendCarBookingStatusEmail(
                    "boukadidahbib@gmail.com", // Replace with user.getEmail() in production
                    updatedBooking.getCarProvider(),
                    newStatus,
                    reservationId
            );
        }

        return new BookingResponseDTO(
                updatedBooking.getId(),
                updatedBooking.getUserId(),
                updatedBooking.getPickupCountry(),
                updatedBooking.getPickupCity(),
                updatedBooking.getCarType(),
                updatedBooking.getCarFeatures(),
                updatedBooking.getPricePerDay(),
                updatedBooking.getPickupDate(),
                updatedBooking.getPickupTime(),
                updatedBooking.getDropoffDate(),
                updatedBooking.getDropoffTime(),
                updatedBooking.getNotes(),
                updatedBooking.getTotalPrice(),
                updatedBooking.getReservationStatus()
        );
    }

    public List<BookingResponseDTO> getPendingReservations() {
        List<Booking> reservations = carBookingDao.findByReservationStatus("Pending");

        return reservations.stream()
                .map(res -> new BookingResponseDTO(
                        res.getId(),
                        res.getUserId(),
                        res.getPickupCountry(),
                        res.getPickupCity(),
                        res.getCarType(),
                        res.getCarFeatures(),
                        res.getPricePerDay(),
                        res.getPickupDate(),
                        res.getPickupTime(),
                        res.getDropoffDate(),
                        res.getDropoffTime(),
                        res.getNotes(),
                        res.getTotalPrice(),
                        res.getReservationStatus()
                ))
                .collect(Collectors.toList());
    }

    public void deleteBooking(String reservationId) {
        Booking reservation = carBookingDao.findById(reservationId);
        if (reservation == null) {
            throw new IllegalArgumentException("Reservation not found: " + reservationId);
        }
        carBookingDao.deleteById(reservationId);
    }
}