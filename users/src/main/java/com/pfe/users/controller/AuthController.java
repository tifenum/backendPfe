package com.pfe.users.controller;

import com.pfe.users.DTO.ClientUserDTO;
import com.pfe.users.DTO.ContactRequestDTO;
import com.pfe.users.DTO.MapillaryImageDTO;
import com.pfe.users.service.ChatbotService;
import com.pfe.users.service.KeycloakService;
import com.pfe.users.service.MapService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class AuthController {

    private final KeycloakService keycloakService;
    private final ChatbotService chatbotService;
    private final MapService mapService;
    public AuthController(KeycloakService keycloakService, ChatbotService chatbotService, MapService mapService) {
        this.keycloakService = keycloakService;
        this.chatbotService = chatbotService;
        this.mapService = mapService;
    }
    @GetMapping("/clients")
    public ResponseEntity<List<ClientUserDTO>> getAllClients() {
        return keycloakService.getAllClients();
    }
    @GetMapping("/ask")
    public Map<String, Object> askAssistant(
            @RequestParam String message,
            @RequestParam String sessionId,
            @RequestParam(required = false) String userId,
            @RequestParam(defaultValue = "0") int characterId // New parameter for historical character
    ) {
        return chatbotService.askAssistant(message, sessionId, userId, characterId);
    }
    @DeleteMapping("/{userId}")
    public ResponseEntity<Void> deleteUser(@PathVariable String userId) {
        return keycloakService.deleteUser(userId);
    }
    @GetMapping("/{userId}")
    public ResponseEntity<ClientUserDTO> getUserById(@PathVariable String userId) {
        return keycloakService.getUserById(userId);
    }
    @GetMapping("/map/images")
    public ResponseEntity<List<MapillaryImageDTO>> getMapillaryImages(
            @RequestParam String bbox,
            @RequestParam(defaultValue = "2") int limit
    ) {
        try {
            List<MapillaryImageDTO> images = mapService.fetchImages(bbox, limit);
            return ResponseEntity.ok(images);
        } catch (RuntimeException e) {
            return ResponseEntity.status(500).body(null);
        }
    }
    @GetMapping("/map/image/{imageId}")
    public ResponseEntity<MapillaryImageDTO> getMapillaryImageDetails(@PathVariable String imageId) {
        try {
            MapillaryImageDTO image = mapService.fetchImageDetails(imageId);
            return ResponseEntity.ok(image);
        } catch (RuntimeException e) {
            return ResponseEntity.status(500).body(null);
        }
    }

    @PostMapping("/newsletter/subscribe")
    public ResponseEntity<String> subscribeToNewsletter(
            @RequestBody Map<String, String> request
    ) {
        String name = request.get("name");
        String email = request.get("email");
        return keycloakService.subscribeToNewsletter(name, email);
    }
    @PostMapping("/contact/submit")
    public ResponseEntity<String> submitContactRequest(
            @Valid @RequestBody ContactRequestDTO request
    ) {
        return keycloakService.submitContactRequest(request.getName(), request.getEmail(), request.getMessage());
    }
}
