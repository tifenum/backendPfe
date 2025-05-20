package com.pfe.cars.DTO;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
public class BookingRequest {
    private String userId;
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
}