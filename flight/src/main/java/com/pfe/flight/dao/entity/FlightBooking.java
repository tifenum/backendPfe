package com.pfe.flight.dao.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Data
@Document(collection = "flight_bookings")
public class FlightBooking {
    @Id
    private String id;
    private String userId;
    private String bookingStatus;
    private FlightDetails flightDetails;

    @Data
    public static class FlightDetails {
        private boolean oneWay;
        private List<List<Seat>> seatMap;
        private String price;
        private List<Itinerary> itineraries;
        private String airlineCodes;
        private int flightId;
        private Seat selectedSeat;

    }

    @Data
    public static class Itinerary {
        private String duration;
        private List<Segment> segments;
    }

    @Data
    public static class Segment {
        private Airport departure;
        private Airport arrival;
        private String duration;
    }

    @Data
    public static class Airport {
        private String iataCode;
        private String terminal;
        private String at;
    }

    @Data
    public static class Seat {
        private String id;
        private boolean reserved;
        private String seatClass;
        private Double extraCost;
    }
}