package com.pfe.flight.DTO;


import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class FlightBookingResponseDto {

    private String id;
    private String userId;
    private Object flightDetails;
    private String bookingStatus;

    public FlightBookingResponseDto(String bookingId, String userId, String bookingStatus, Object flightDetails) {
        this.id = bookingId;
        this.userId = userId;
        this.bookingStatus = bookingStatus;
        this.flightDetails = flightDetails;
    }

    public FlightBookingResponseDto() {

    }
}