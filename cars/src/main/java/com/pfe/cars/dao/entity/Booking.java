package com.pfe.cars.dao.entity;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.util.List;

@Setter
@Getter
@Document(collection = "carBookings")
public class Booking {

    @Id
    private String id;
    private String userId;
    private String carProvider;
    private String pickupCountry;
    private String pickupCity;
    private String carType;
    private List<String> carFeatures;
    private String pickupDate;
    private String pickupTime;
    private String dropoffDate;
    private String dropoffTime;
    private String notes;
    private int pricePerDay;
    private int totalPrice;
    private String reservationStatus;

    public Booking() {}

    public Booking(String userId, String carProvider, String pickupCountry, String pickupCity,
                   String carType, List<String> carFeatures, String pickupDate,
                   String pickupTime, String dropoffDate, String dropoffTime, String notes,int pricePerDay,
                   int totalPrice) {
        this.userId = userId;
        this.carProvider = carProvider;
        this.pickupCountry = pickupCountry;
        this.pickupCity = pickupCity;
        this.carType = carType;
        this.carFeatures = carFeatures;
        this.pickupDate = pickupDate;
        this.pickupTime = pickupTime;
        this.dropoffDate = dropoffDate;
        this.dropoffTime = dropoffTime;
        this.notes = notes;
        this.totalPrice = totalPrice;
        this.reservationStatus = "Pending"; // Default status
        this.pricePerDay = pricePerDay;
    }

}