package com.pfe.hotel.DTO;

import java.util.List;

public class BookingRequest {
    private String userId;          // New: User ID
    private String hotelName;       // Already there
    private String hotelAddress;    // New: Hotel address
    private String roomType;        // Already there
    private List<String> roomFeatures; // New: Room features
    private double roomPricePerNight;  // Already there
    private String checkInDate;        // Already there (String for JSON parsing)
    private String checkOutDate;       // Already there (String for JSON parsing)
    private String notes;              // Already there
    private double totalPrice;         // Already there

    // Getters and Setters
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

    public String getCheckInDate() { return checkInDate; }
    public void setCheckInDate(String checkInDate) { this.checkInDate = checkInDate; }

    public String getCheckOutDate() { return checkOutDate; }
    public void setCheckOutDate(String checkOutDate) { this.checkOutDate = checkOutDate; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public double getTotalPrice() { return totalPrice; }
    public void setTotalPrice(double totalPrice) { this.totalPrice = totalPrice; }
}