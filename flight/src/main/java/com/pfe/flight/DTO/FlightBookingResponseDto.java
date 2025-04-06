package com.pfe.flight.DTO;


import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class FlightBookingResponseDto {

    private String id;
    private String userId;
    private FlightDetailsDto flightDetails;
    private String bookingStatus;

    public FlightBookingResponseDto() {
    }
}