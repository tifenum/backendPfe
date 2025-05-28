package com.pfe.users.DTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MapillaryImageDTO {
    private String id;
    private double[] coordinates;
    private String thumbUrl;
    private String thumbUrlHighRes; // Optional for thumb_2048_url
}