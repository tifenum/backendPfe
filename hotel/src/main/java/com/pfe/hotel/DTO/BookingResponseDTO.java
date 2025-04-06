package com.pfe.hotel.DTO;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;
@Getter @Setter
public class BookingResponseDTO {
    private String hotelName;
    private String hotelAddress;
    private String roomType;
    private List<String> roomFeatures;
    private double roomPricePerNight;
    private LocalDate checkInDate;
    private LocalDate checkOutDate;
    private String notes;
    private double totalPrice;

    // Getters and setters...

    public BookingResponseDTO(String hotelName, String hotelAddress, String roomType,
                              List<String> roomFeatures, double roomPricePerNight,
                              LocalDate checkInDate, LocalDate checkOutDate, String notes,
                              double totalPrice) {
        this.hotelName = hotelName;
        this.hotelAddress = hotelAddress;
        this.roomType = roomType;
        this.roomFeatures = roomFeatures;
        this.roomPricePerNight = roomPricePerNight;
        this.checkInDate = checkInDate;
        this.checkOutDate = checkOutDate;
        this.notes = notes;
        this.totalPrice = totalPrice;
    }
}
