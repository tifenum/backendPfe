package com.pfe.flight.DTO;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ArrivalDto {
    private String terminal;
    private String iataCode;
    private String at;
}