package com.pfe.flight.DTO;

import lombok.Data;

@Data

public class SlimFlightBookingDto {
    private String id;
    private String userId;
    private String departureAirport;
    private String departureTime;
    private String arrivalAirport;
    private String tripType;
    private String totalPrice;
    private String bookingStatus;
    private String returnDate;
}