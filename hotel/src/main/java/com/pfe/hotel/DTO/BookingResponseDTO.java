package com.pfe.hotel.DTO;

import lombok.Getter;
import lombok.Setter;

import java.util.List;
@Getter @Setter
public class BookingResponseDTO {
    private String id; // Added for frontend compatibility
    private String hotelName;
    private String hotelAddress;
    private String roomType;
    private List<String> roomFeatures;
    private int roomPricePerNight;
    private String checkInDate;
    private String checkOutDate;
    private String notes;
    private int totalPrice;
    private String reservationStatus;

    // Getters and setters...

    public BookingResponseDTO(String id,String hotelName, String hotelAddress, String roomType,
                              List<String> roomFeatures, int roomPricePerNight,
                              String checkInDate, String checkOutDate, String notes,
                              int totalPrice, String reservationStatus) {
        this.id = id;
        this.hotelName = hotelName;
        this.hotelAddress = hotelAddress;
        this.roomType = roomType;
        this.roomFeatures = roomFeatures;
        this.roomPricePerNight = roomPricePerNight;
        this.checkInDate = checkInDate;
        this.checkOutDate = checkOutDate;
        this.notes = notes;
        this.totalPrice = totalPrice;
        this.reservationStatus = reservationStatus;
    }
}
