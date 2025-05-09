package com.pfe.users.controller;

import com.pfe.users.DTO.ClientUserDTO;
import com.pfe.users.service.ChatbotService;
import com.pfe.users.service.KeycloakService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class AuthController {

    private final KeycloakService keycloakService;
    private final ChatbotService chatbotService;

    public AuthController(KeycloakService keycloakService, ChatbotService chatbotService) {
        this.keycloakService = keycloakService;
        this.chatbotService = chatbotService;

    }
    @GetMapping("/clients")
    public ResponseEntity<List<ClientUserDTO>> getAllClients() {
        return keycloakService.getAllClients();
    }
    @GetMapping("/ask")
    public Map<String, Object> askAssistant(
            @RequestParam String message,
            @RequestParam String sessionId,
            @RequestParam(required = false) String userId // Dynamic userId parameter
    ) {
        return chatbotService.askAssistant(message, sessionId, userId);
    }
    @DeleteMapping("/{userId}")
    public ResponseEntity<Void> deleteUser(@PathVariable String userId) {
        return keycloakService.deleteUser(userId);
    }
}
