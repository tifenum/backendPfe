package com.pfe.flight.mappers;

import com.pfe.flight.DTO.FlightBookingRequestDto;
import com.pfe.flight.DTO.SlimFlightBookingDto;
import com.pfe.flight.dao.entity.FlightBooking;
import org.springframework.stereotype.Component;

@Component
public class FlightBookingMapper {

    public FlightBooking toEntity(FlightBookingRequestDto dto) {
        FlightBooking entity = new FlightBooking();
        entity.setUserId(dto.getUserId());
        entity.setBookingStatus(dto.getBookingStatus());

        if (dto.getFlightDetails() != null) {
            entity.setFlightDetails(mapFlightDetails(dto.getFlightDetails()));
        }

        return entity;
    }

    private FlightBooking.FlightDetails mapFlightDetails(FlightBookingRequestDto.FlightDetails dtoDetails) {
        FlightBooking.FlightDetails details = new FlightBooking.FlightDetails();
        details.setOneWay(dtoDetails.isOneWay());
        details.setPrice(dtoDetails.getPrice());
        details.setAirlineCodes(dtoDetails.getAirlineCodes());
        details.setFlightId(dtoDetails.getId());

        // Map the seat map
        details.setSeatMap(dtoDetails.getSeatMap().stream()
                .map(seatList -> seatList.stream()
                        .map(this::mapSeat)
                        .toList())
                .toList());

        // Map itineraries
        details.setItineraries(dtoDetails.getItineraries().stream()
                .map(this::mapItinerary)
                .toList());

        // Map the selected seat if present
        if (dtoDetails.getSelectedSeat() != null) {
            details.setSelectedSeat(mapSeat(dtoDetails.getSelectedSeat()));
        }

        return details;
    }

    private FlightBooking.Seat mapSeat(FlightBookingRequestDto.SeatDTO dto) {
        FlightBooking.Seat seat = new FlightBooking.Seat();
        seat.setId(dto.getId());
        seat.setReserved(dto.isReserved());
        seat.setSeatClass(dto.getSeatClass());
        // Map the extraCost if provided (will be null for non-selected seats)
        seat.setExtraCost(dto.getExtraCost());
        return seat;
    }

    private FlightBooking.Itinerary mapItinerary(FlightBookingRequestDto.ItineraryDTO dto) {
        FlightBooking.Itinerary itinerary = new FlightBooking.Itinerary();
        itinerary.setDuration(dto.getDuration());
        itinerary.setSegments(dto.getSegments().stream()
                .map(this::mapSegment)
                .toList());
        return itinerary;
    }

    private FlightBooking.Segment mapSegment(FlightBookingRequestDto.SegmentDTO dto) {
        FlightBooking.Segment segment = new FlightBooking.Segment();
        segment.setDuration(dto.getDuration());
        segment.setDeparture(mapAirport(dto.getDeparture()));
        segment.setArrival(mapAirport(dto.getArrival()));
        return segment;
    }

    private FlightBooking.Airport mapAirport(FlightBookingRequestDto.AirportDTO dto) {
        FlightBooking.Airport airport = new FlightBooking.Airport();
        airport.setIataCode(dto.getIataCode());
        airport.setTerminal(dto.getTerminal());
        airport.setAt(dto.getAt());
        return airport;
    }

    public SlimFlightBookingDto mapToSlimDto(FlightBooking flightBooking) {
        SlimFlightBookingDto slimDto = new SlimFlightBookingDto();
        slimDto.setId(flightBooking.getId());
        slimDto.setUserId(flightBooking.getUserId());
        slimDto.setBookingStatus(flightBooking.getBookingStatus());

        if (flightBooking.getFlightDetails() != null) {
            FlightBooking.FlightDetails details = flightBooking.getFlightDetails();

            // Map trip type: One Way or Round Trip
            slimDto.setTripType(details.isOneWay() ? "One Way" : "Round Trip");

            // Map total price (assuming the price is a string, as per the entity)
            slimDto.setTotalPrice(details.getPrice());

            // Map departure airport and departure time from the first itinerary's first segment, if available
            if (details.getItineraries() != null && !details.getItineraries().isEmpty()) {
                FlightBooking.Itinerary firstItinerary = details.getItineraries().get(0);
                if (firstItinerary.getSegments() != null && !firstItinerary.getSegments().isEmpty()) {
                    FlightBooking.Segment firstSegment = firstItinerary.getSegments().get(0);
                    if (firstSegment.getDeparture() != null) {
                        slimDto.setDepartureAirport(firstSegment.getDeparture().getIataCode());
                        slimDto.setDepartureTime(firstSegment.getDeparture().getAt());
                    }
                }

                // Map arrival airport from the last itinerary's last segment, if available
                FlightBooking.Itinerary lastItinerary = details.getItineraries().get(details.getItineraries().size() - 1);
                if (lastItinerary.getSegments() != null && !lastItinerary.getSegments().isEmpty()) {
                    FlightBooking.Segment lastSegment = lastItinerary.getSegments().get(lastItinerary.getSegments().size() - 1);
                    if (lastSegment.getArrival() != null) {
                        slimDto.setArrivalAirport(lastSegment.getArrival().getIataCode());
                    }
                }
            }
        }

        return slimDto;
    }
}
