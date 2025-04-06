package com.pfe.flight.mappers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pfe.flight.DTO.SlimFlightBookingDto;
import com.pfe.flight.dao.entity.FlightBooking;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Component
public class FlightBookingMapper {
    private final ObjectMapper objectMapper;

    public FlightBookingMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @SuppressWarnings("unchecked")
    public SlimFlightBookingDto mapToSlimDto(FlightBooking booking) {
        SlimFlightBookingDto dto = new SlimFlightBookingDto();
        dto.setUserId(booking.getUserId());
        dto.setBookingStatus(booking.getBookingStatus()); // Set booking status

        // 1) parse flightDetails into a Map
        Object rawDetails = booking.getFlightDetails();
        Map<String,Object> flightDetailsMap;
        if (rawDetails instanceof String) {
            try {
                flightDetailsMap = objectMapper.readValue((String) rawDetails,
                        new TypeReference<Map<String,Object>>() {});
            } catch (JsonProcessingException e) {
                throw new RuntimeException("Failed to parse flightDetails JSON", e);
            }
        } else if (rawDetails instanceof Map) {
            flightDetailsMap = (Map<String,Object>) rawDetails;
        } else {
            flightDetailsMap = Collections.emptyMap();
        }

        // 2) extract price
        Map<String,Object> price = (Map<String,Object>) flightDetailsMap.get("price");
        if (price != null) {
            dto.setTotalPrice((String) price.get("grandTotal"));
        }

        // 3) extract itineraries
        List<Map<String,Object>> itineraries =
                (List<Map<String,Object>>) flightDetailsMap.get("itineraries");
        if (itineraries != null && !itineraries.isEmpty()) {
            // determine trip type
            dto.setTripType(itineraries.size() > 1 ? "ROUND_TRIP" : "ONE_WAY");

            // first itinerary: departure info
            Map<String,Object> firstItin = itineraries.get(0);
            List<Map<String,Object>> firstSegs =
                    (List<Map<String,Object>>) firstItin.get("segments");
            if (firstSegs != null && !firstSegs.isEmpty()) {
                Map<String,Object> dep = (Map<String,Object>) firstSegs.get(0).get("departure");
                if (dep != null) {
                    dto.setDepartureAirport((String) dep.get("iataCode"));
                    // directly use string with space instead of 'T'
                    String at = (String) dep.get("at");
                    dto.setDepartureTime(at);
                }
            }

            // last itinerary: arrival info
            Map<String,Object> lastItin = itineraries.get(itineraries.size() - 1);
            List<Map<String,Object>> lastSegs =
                    (List<Map<String,Object>>) lastItin.get("segments");
            if (lastSegs != null && !lastSegs.isEmpty()) {
                Map<String,Object> arr = (Map<String,Object>) lastSegs.get(lastSegs.size() - 1).get("arrival");
                if (arr != null) {
                    dto.setArrivalAirport((String) arr.get("iataCode"));
                }
            }
        }

        return dto;
    }
}
