package com.pfe.cars.DTO;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class BookingResponseDTO {
    private String id;
    private String userId;
    private String carProvider;
    private String pickupCountry;
    private String pickupCity;
    private String carType;
    private List<String> carFeatures;
    private int pricePerDay;
    private String pickupDate;
    private String pickupTime;
    private String dropoffDate;
    private String dropoffTime;
    private String notes;
    private int totalPrice;
    private String reservationStatus;

    public BookingResponseDTO(String id, String userId, String carProvider, String pickupCountry,
                                 String pickupCity, String carType, List<String> carFeatures,
                                 int pricePerDay, String pickupDate, String pickupTime,
                                 String dropoffDate, String dropoffTime, String notes,
                                 int totalPrice, String reservationStatus) {
        this.id = id;
        this.userId = userId;
        this.carProvider = carProvider;
        this.pickupCountry = pickupCountry;
        this.pickupCity = pickupCity;
        this.carType = carType;
        this.carFeatures = carFeatures;
        this.pricePerDay = pricePerDay;
        this.pickupDate = pickupDate;
        this.pickupTime = pickupTime;
        this.dropoffDate = dropoffDate;
        this.dropoffTime = dropoffTime;
        this.notes = notes;
        this.totalPrice = totalPrice;
        this.reservationStatus = reservationStatus;
    }
}