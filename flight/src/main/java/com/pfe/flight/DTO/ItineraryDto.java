package com.pfe.flight.DTO;


import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;

@Setter
@Getter
public class ItineraryDto {
    @NotBlank(message = "Duration must not be empty")
    private String duration;

    @NotNull(message = "Segments must not be null")
    private List<SegmentDto> segments;
}