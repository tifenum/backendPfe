package com.pfe.flight.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.util.Map;
import java.util.List;

@Document(collection = "flight_bookings")
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class FlightBooking {

    @Id
    private String id;
    private String userId;
    private Object flightDetails;
    private String bookingStatus;

    @Override
    public String toString() {
        return "FlightBooking{" +
                "userId='" + userId + '\'' +
                ", flightDetails=" + flightDetails +
                ", bookingStatus='" + bookingStatus + '\'' +
                '}';
    }
}
