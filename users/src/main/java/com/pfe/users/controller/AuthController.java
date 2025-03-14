package com.pfe.users.controller;

import com.pfe.users.security.JwtUtil;
import com.pfe.users.service.UserService;
import com.pfe.users.user.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import java.util.UUID;

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
        Logger logger = LoggerFactory.getLogger(AuthController.class);
        logger.info("Received login request for email: {}", loginRequest.getEmail());

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
    public Mono<ResponseEntity<Object>> googleCallback(OAuth2AuthenticationToken authentication) {
        return googleCallbackLogic(authentication)
                .flatMap(token -> {
                    String frontendUrl = "http://localhost:3000?token=" + token;
                    return Mono.just(
                            ResponseEntity.status(HttpStatus.SEE_OTHER)
                                    .location(URI.create(frontendUrl))
                                    .build()
                    );
                })
                .onErrorResume(e -> {
                    // Log the error
                    System.err.println("Error in googleCallback: " + e.getMessage());
                    // Redirect to frontend with error
                    String frontendErrorUrl = "http://localhost:3000/login?error=oauth_failed";
                    return Mono.just(
                            ResponseEntity.status(HttpStatus.SEE_OTHER)
                                    .location(URI.create(frontendErrorUrl))
                                    .build()
                    );
                });
    }

    private Mono<String> googleCallbackLogic(OAuth2AuthenticationToken authentication) {
        return Mono.just(authentication)
                .flatMap(auth -> {
                    OAuth2User oauth2User = auth.getPrincipal();
                    String email = oauth2User.getAttribute("email");
                    String name = oauth2User.getAttribute("name");

                    if (email == null || name == null) {
                        return Mono.error(new RuntimeException("Email or name not provided by Google"));
                    }

                    return userService.findByEmail(email)
                            .switchIfEmpty(userService.registerUser(createNewUser(email, name)))
                            .map(user -> jwtUtil.generateToken(user.getEmail(), Map.of(
                                    "username", user.getUsername(),
                                    "email", user.getEmail()
                            )));
                });
    }

    private User createNewUser(String email, String name) {
        User newUser = new User();
        newUser.setEmail(email);
        newUser.setUsername(name);
        newUser.setPassword(passwordEncoder.encode(UUID.randomUUID().toString()));
        return newUser;
    }
}
