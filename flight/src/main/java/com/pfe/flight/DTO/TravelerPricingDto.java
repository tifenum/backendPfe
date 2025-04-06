package com.pfe.flight.DTO;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;

@Setter
@Getter
@JsonIgnoreProperties(ignoreUnknown = true) // Add this annotation
public class TravelerPricingDto {
    @NotNull(message = "Seat map must not be null")
    private List<SeatDto> seatMap;

    @NotBlank(message = "Traveler type must not be empty")
    private String travelerType;

    @NotNull(message = "Price must not be null")
    private PriceDto price;
}