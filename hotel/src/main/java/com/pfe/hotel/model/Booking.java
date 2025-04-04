package com.pfe.hotel.model;

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

    // Getters and Setters
    @Id
    private String id;
    private String userId;          // New: To track who’s booking
    private String hotelName;       // Already there
    private String hotelAddress;    // New: Hotel’s full address
    private String roomType;        // Already there
    private List<String> roomFeatures; // New: Features like "Ocean View", "Hot Tub"
    private double roomPricePerNight;  // Already there
    private LocalDate checkInDate;     // Already there
    private LocalDate checkOutDate;    // Already there
    private String notes;              // Already there
    private double totalPrice;         // Already there

    // Default constructor
    public Booking() {}

    // Updated constructor with all fields
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