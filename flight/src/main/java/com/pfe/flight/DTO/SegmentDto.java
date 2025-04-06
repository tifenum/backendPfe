package com.pfe.flight.DTO;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Setter
@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class SegmentDto {
    @NotBlank private String duration;
    @NotBlank private String number;
    private int numberOfStops;
    private boolean blacklistedInEU;
    @NotBlank private String carrierCode;

    @NotNull
    private Aircraft aircraft;
    @NotNull
    private Place departure;
    @NotNull
    private Place arrival;

    @Getter @Setter
    public static class Aircraft {
        @NotBlank private String code;
    }

    @Getter @Setter
    public static class Place {
        @NotBlank private String iataCode;
        @NotBlank private String at;
    }
}
