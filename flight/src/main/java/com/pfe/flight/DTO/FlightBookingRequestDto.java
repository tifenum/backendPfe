package com.pfe.flight.DTO;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class FlightBookingRequestDto {
    private String userId;
    private String bookingStatus;
    private FlightDetails flightDetails;

    @Data
    public static class FlightDetails {
        private boolean oneWay;
        private String tripType; // "One Way" or "Round Trip"
        private String returnDate; // ISO date (e.g., "2025-05-20")
        private List<List<SeatDTO>> seatMap;
        private String price;
        private List<ItineraryDTO> itineraries;
        @JsonProperty("AirlineCodes")
        private String airlineCodes;
        private int id;
        private SeatDTO selectedSeat;
    }

    @Data
    public static class ItineraryDTO {
        private String duration;
        private List<SegmentDTO> segments;
    }

    @Data
    public static class SegmentDTO {
        private AirportDTO departure;
        private AirportDTO arrival;
        private String duration;
    }

    @Data
    public static class AirportDTO {
        private String iataCode;
        private String terminal;
        private String at;
    }

    @Data
    public static class SeatDTO {
        private String id;
        @JsonProperty("isReserved")
        private boolean reserved;
        @JsonProperty("class")
        private String seatClass;
        private Double extraCost;
    }
}
