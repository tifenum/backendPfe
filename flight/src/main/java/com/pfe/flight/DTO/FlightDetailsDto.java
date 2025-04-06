package com.pfe.flight.DTO;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;

@Setter
@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class FlightDetailsDto {
    @NotNull(message = "Number of bookable seats must not be null")
    private Integer numberOfBookableSeats;

    @NotNull(message = "Traveler pricings must not be null")
    private List<TravelerPricingDto> travelerPricings;

    @NotBlank(message = "Source must not be empty")
    private String source;

    @NotNull(message = "Price must not be null")
    private PriceDto price;

    @NotNull(message = "Itineraries must not be null")
    private List<ItineraryDto> itineraries;

    @NotBlank(message = "Last ticketing date must not be empty")
    private String lastTicketingDate;
    private Boolean upsellOffer;               // renamed
    private Boolean instantTicketingRequired;   // add
    private List<String> validatingAirlineCodes;// add
    private Boolean oneWay;                     // add
    private Boolean nonHomogeneous;             // add

}