package com.pfe.flight.DTO;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;

@Getter @Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class SeatDto {
    @JsonProperty("isReserved")  // Add this annotation
    private boolean isReserved;

    @NotBlank
    private String id;

    @JsonProperty("class")
    @NotBlank
    private String seatClass;
}