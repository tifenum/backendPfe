package com.pfe.flight.DTO;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Setter
@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class PriceDto {
    @NotBlank(message = "Total price must not be empty")
    private String total;

    @NotBlank(message = "Currency must not be empty")
    private String currency;

    @NotBlank(message = "Base price must not be empty")
    private String base;

    @NotBlank(message = "Grand total must not be empty")
    private String grandTotal;
}