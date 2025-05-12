package com.pfe.hotel.dao.entity;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.util.List;

@Setter
@Getter
@Document(collection = "hotelBookings")
public class Booking {

    @Id
    private String id;
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
    public Booking() {}

    public Booking(String userId, String hotelName, String hotelAddress, String roomType,
                   List<String> roomFeatures, int roomPricePerNight, String checkInDate,
                   String checkOutDate, String notes, int totalPrice) {
        this.userId = userId;
        this.hotelName = hotelName;
        this.hotelAddress = hotelAddress;
        this.roomType = roomType;
        this.roomFeatures = roomFeatures;
        this.roomPricePerNight = roomPricePerNight;
        this.checkInDate = checkInDate;
        this.checkOutDate = checkOutDate;
        this.notes = notes;
        this.totalPrice = totalPrice;
        this.reservationStatus = "Pending"; // Default status
    }

}