package com.pfe.hotel.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDate;
import java.util.List;

@Document(collection = "hotelBookings")
public class Booking {

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

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getHotelName() { return hotelName; }
    public void setHotelName(String hotelName) { this.hotelName = hotelName; }

    public String getHotelAddress() { return hotelAddress; }
    public void setHotelAddress(String hotelAddress) { this.hotelAddress = hotelAddress; }

    public String getRoomType() { return roomType; }
    public void setRoomType(String roomType) { this.roomType = roomType; }

    public List<String> getRoomFeatures() { return roomFeatures; }
    public void setRoomFeatures(List<String> roomFeatures) { this.roomFeatures = roomFeatures; }

    public double getRoomPricePerNight() { return roomPricePerNight; }
    public void setRoomPricePerNight(double roomPricePerNight) { this.roomPricePerNight = roomPricePerNight; }

    public LocalDate getCheckInDate() { return checkInDate; }
    public void setCheckInDate(LocalDate checkInDate) { this.checkInDate = checkInDate; }

    public LocalDate getCheckOutDate() { return checkOutDate; }
    public void setCheckOutDate(LocalDate checkOutDate) { this.checkOutDate = checkOutDate; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public double getTotalPrice() { return totalPrice; }
    public void setTotalPrice(double totalPrice) { this.totalPrice = totalPrice; }
}