package com.pfe.hotel.dao.entity;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDate;
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
    private double roomPricePerNight;
    private LocalDate checkInDate;
    private LocalDate checkOutDate;
    private String notes;
    private double totalPrice;

    public Booking() {}

    public Booking(String userId, String hotelName, String hotelAddress, String roomType,
                   List<String> roomFeatures, double roomPricePerNight, LocalDate checkInDate,
                   LocalDate checkOutDate, String notes, double totalPrice) {
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
    }

}