package com.pfe.users.controller;

import com.pfe.users.DTO.ClientUserDTO;
import com.pfe.users.service.KeycloakService;
import com.pfe.users.user.User;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController
@RequestMapping("/users")
public class AuthController {

    private final KeycloakService keycloakService;

    public AuthController(KeycloakService keycloakService) {
        this.keycloakService = keycloakService;
    }
    @GetMapping("/clients")
    public Mono<ResponseEntity<List<ClientUserDTO>>> getAllClients() {
        return keycloakService.getAllClients();
    }
}
