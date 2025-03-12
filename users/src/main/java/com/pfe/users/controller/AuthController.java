package com.pfe.users.controller;

import com.pfe.users.security.JwtUtil;
import com.pfe.users.service.UserService;
import com.pfe.users.user.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
@CrossOrigin
@RestController
@RequestMapping("/users")
public class AuthController {

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private PasswordEncoder passwordEncoder;
    @PostMapping("/signup")
    public Mono<ResponseEntity<String>> signUp(@RequestBody User user) {
        return userService.registerUser(user)
                .map(registeredUser -> ResponseEntity.ok("User registered successfully"))
                .onErrorReturn(ResponseEntity.status(400).body("Email already in use"));
    }

    @PostMapping("/login")
    public Mono<ResponseEntity<String>> login(@RequestBody User loginRequest) {
        return userService.findByEmail(loginRequest.getEmail()) // Find user by email
                .filter(user -> passwordEncoder.matches(loginRequest.getPassword(), user.getPassword()))
                .map(user -> {
                    Map<String, Object> additionalClaims = new HashMap<>();
                    additionalClaims.put("username", user.getUsername());
                    additionalClaims.put("email", user.getEmail());
                    String token = jwtUtil.generateToken(user.getEmail(), additionalClaims); // Use email as subject
                    return ResponseEntity.ok(token);
                })
                .defaultIfEmpty(ResponseEntity.status(401).body("Invalid credentials"));
    }


    @GetMapping("/google")
    public Mono<ResponseEntity<Void>> googleLoginRedirect() {
        return Mono.just(ResponseEntity.status(HttpStatus.FOUND)
                .location(URI.create("/oauth2/authorization/google"))
                .build());
    }

    @GetMapping("/google/callback")
    public Mono<ResponseEntity<Map<String, Object>>> googleCallback(OAuth2AuthenticationToken authentication) {
        if (authentication == null || authentication.getPrincipal() == null) {
            return Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "User not authenticated")));
        }

        OAuth2User user = authentication.getPrincipal();
        Map<String, Object> userInfo = Map.of(
                "id", user.getAttributes().get("sub"),
                "username", user.getAttributes().get("name"),
                "email", user.getAttributes().get("email")
        );

        return Mono.just(ResponseEntity.ok(userInfo));
    }
}
