package com.pfe.users.service;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pfe.users.DTO.MapillaryImageDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.HttpClientErrorException;

import java.util.ArrayList;
import java.util.List;

@Service
public class MapService {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${mapillary.access-token}")
    private String mapillaryAccessToken;

    public MapService(RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    public List<MapillaryImageDTO> fetchImages(String bbox, int limit) {
        String url = String.format(
                "https://graph.mapillary.com/images?access_token=%s&bbox=%s&fields=id,computed_geometry,thumb_256_url,is_pano&limit=%d",
                mapillaryAccessToken, bbox, limit
        );

        try {
            String response = restTemplate.getForObject(url, String.class);
            JsonNode root = objectMapper.readTree(response);
            JsonNode data = root.path("data");

            List<MapillaryImageDTO> images = new ArrayList<>();
            for (JsonNode item : data) {
                MapillaryImageDTO image = new MapillaryImageDTO();
                image.setId(item.path("id").asText());
                image.setThumbUrl(item.path("thumb_256_url").asText(""));
                JsonNode geometry = item.path("computed_geometry").path("coordinates");
                if (geometry.isArray() && geometry.size() == 2) {
                    image.setCoordinates(new double[]{geometry.get(0).asDouble(), geometry.get(1).asDouble()});
                } else {
                    // Fallback to bbox center
                    String[] bboxParts = bbox.split(",");
                    image.setCoordinates(new double[]{
                            Double.parseDouble(bboxParts[0]),
                            Double.parseDouble(bboxParts[1])
                    });
                }
                images.add(image);
            }
            return images;
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.TOO_MANY_REQUESTS) {
                throw new RuntimeException("Mapillary API rate limit exceeded");
            }
            throw new RuntimeException("Failed to fetch Mapillary images: " + e.getMessage());
        } catch (Exception e) {
            throw new RuntimeException("Error processing Mapillary response: " + e.getMessage());
        }
    }
    public MapillaryImageDTO fetchImageDetails(String imageId) {
        String url = String.format(
                "https://graph.mapillary.com/images?access_token=%s&image_ids=%s&fields=id,computed_geometry,thumb_1024_url,thumb_2048_url",
                mapillaryAccessToken, imageId
        );

        try {
            String response = restTemplate.getForObject(url, String.class);
            JsonNode root = objectMapper.readTree(response);
            JsonNode data = root.path("data");

            if (!data.isArray() || data.size() == 0) {
                throw new RuntimeException("No image found for ID: " + imageId);
            }

            JsonNode item = data.get(0);
            MapillaryImageDTO image = new MapillaryImageDTO();
            image.setId(item.path("id").asText());
            image.setThumbUrl(item.path("thumb_1024_url").asText(""));
            JsonNode geometry = item.path("computed_geometry").path("coordinates");
            if (geometry.isArray() && geometry.size() == 2) {
                image.setCoordinates(new double[]{geometry.get(0).asDouble(), geometry.get(1).asDouble()});
            } else {
                throw new RuntimeException("Invalid coordinates for image ID: " + imageId);
            }
            return image;
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.TOO_MANY_REQUESTS) {
                throw new RuntimeException("Mapillary API rate limit exceeded");
            }
            throw new RuntimeException("Failed to fetch Mapillary image: " + e.getMessage());
        } catch (Exception e) {
            throw new RuntimeException("Error processing Mapillary response: " + e.getMessage());
        }
    }
}

