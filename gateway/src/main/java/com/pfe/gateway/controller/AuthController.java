package com.pfe.gateway.controller;


import com.pfe.gateway.DTO.ResetPasswordRequest;
import com.pfe.gateway.DTO.TokenResponse;
import com.pfe.gateway.service.KeycloakService;
import com.pfe.gateway.user.User;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final KeycloakService keycloakService;

    public AuthController(KeycloakService keycloakService) {
        this.keycloakService = keycloakService;
    }

    @PostMapping("/signup")
    public Mono<ResponseEntity<String>> signUp(@RequestBody User user) {
        return keycloakService.registerUser(user);
    }

    @PostMapping("/login")
    public Mono<ResponseEntity<TokenResponse>> login(@RequestBody User loginRequest) {
        return keycloakService.loginUser(loginRequest.getEmail(), loginRequest.getPassword());
    }

    @PostMapping("/logout")
    public Mono<ResponseEntity<String>> logout() {
        return ReactiveSecurityContextHolder.getContext()
                .map(SecurityContext::getAuthentication)
                .flatMap(auth -> keycloakService.logoutUser((JwtAuthenticationToken) auth));
    }
    @PostMapping("/reset-password")
    public Mono<ResponseEntity<String>> resetPassword(@RequestBody ResetPasswordRequest request) {
        return keycloakService.resetPassword(request.getEmail());
    }
}
