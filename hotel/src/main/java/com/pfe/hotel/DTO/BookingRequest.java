package com.pfe.hotel.DTO;

import lombok.Getter;
import lombok.Setter;

import java.util.List;
@Setter
@Getter
public class BookingRequest {
    private String userId;
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
}