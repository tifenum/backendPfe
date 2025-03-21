package com.pfe.users.controller;

import com.pfe.users.service.KeycloakService;
import com.pfe.users.user.User;
import com.pfe.users.DTO.TokenResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.net.URI;

@RestController
@RequestMapping("/users")
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    @Autowired
    private KeycloakService keycloakService;

    // Direct user registration without admin API
    @PostMapping("/signup")
    public Mono<ResponseEntity<String>> signUp(@RequestBody User user) {
        logger.info("Registering user with email: {}", user.getEmail());
        return keycloakService.registerUser(user)
                .map(v -> ResponseEntity.ok("User registered successfully"))
                .onErrorResume(e ->
                        Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                .body("Error during registration: " + e.getMessage()))
                );
    }

    // Direct login using Resource Owner Password Credentials grant
    @PostMapping("/login")
    public Mono<ResponseEntity<TokenResponse>> login(@RequestBody User loginRequest) {
        logger.info("Received login request for email: {}", loginRequest.getEmail());
        return keycloakService.loginUser(loginRequest.getEmail(), loginRequest.getPassword())
                .map(token -> ResponseEntity.ok(token))
                .defaultIfEmpty(ResponseEntity.status(HttpStatus.UNAUTHORIZED).build());
    }
    @PostMapping("/logout")
    public Mono<ResponseEntity<String>> logout(@RequestHeader("Authorization") String authorizationHeader) {
        String accessToken = authorizationHeader.replace("Bearer ", ""); // Extract the token from Authorization header

        logger.info("Logging out user with access token: {}", accessToken);

        return keycloakService.logoutUser(accessToken)
                .map(v -> ResponseEntity.ok("Logged out successfully"))
                .onErrorResume(e ->
                        Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                .body("Error during logout: " + e.getMessage()))
                );
    }
    @GetMapping("/google")
    public Mono<Void> initiateGoogleLogin(ServerWebExchange exchange) {
        // Build the Keycloak authorization URL with the Google IdP hint.
        String keycloakAuthUrl = keycloakService.getKeycloakServerUrl() + "/realms/" + keycloakService.getRealm() +
                "/protocol/openid-connect/auth" +
                "?client_id=" + keycloakService.getClientId() +
                "&response_type=code" +
                "&scope=openid%20email%20profile" +
                "&redirect_uri=" + "http://localhost:8090/users/oauth2/callback/google" +
                "&kc_idp_hint=google";

        exchange.getResponse().setStatusCode(HttpStatus.SEE_OTHER);
        exchange.getResponse().getHeaders().setLocation(URI.create(keycloakAuthUrl));
        return exchange.getResponse().setComplete();
    }

    @GetMapping("/oauth2/callback/google")
    public Mono<ResponseEntity<TokenResponse>> googleLoginCallback(@RequestParam("code") String code) {
        logger.info("Received Google login callback with code: {}", code);
        return keycloakService.exchangeGoogleCodeForToken(code)
                .map(tokenResponse -> {
                    logger.info("Google login successful, token: {}", tokenResponse);
                    return ResponseEntity.ok(tokenResponse);
                })
                .onErrorResume(e -> {
                    logger.error("Error during Google login", e);
                    return Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST)
                            .body(new TokenResponse("Error during Google login: " + e.getMessage())));
                });
    }


}

