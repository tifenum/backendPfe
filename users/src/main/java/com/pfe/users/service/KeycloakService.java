package com.pfe.users.service;

import com.pfe.users.user.User;
import com.pfe.users.DTO.TokenResponse;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Map;

@Service
public class KeycloakService {

    private final WebClient webClient;
    @Getter
    @Value("${keycloak.server-url}")
    private String keycloakServerUrl;

    @Getter
    @Value("${keycloak.realm}")
    private String realm;

    @Getter
    @Value("${keycloak.client-id}")
    private String clientId;

    @Value("${keycloak.client-secret}")
    private String clientSecret;

    @Value("${keycloak.admin-token}")
    private String adminToken;

    public KeycloakService(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.build();
    }

    public Mono<Void> registerUser(User user) {
        var keycloakUserPayload = Map.of(
                "username", user.getEmail(),
                "email", user.getEmail(),
                "enabled", true,
                "credentials", new Object[] {
                        Map.of("type", "password", "value", user.getPassword(), "temporary", false)
                }
        );

        return webClient.post()
                .uri(keycloakServerUrl + "/admin/realms/" + realm + "/users")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(keycloakUserPayload))
                .retrieve()
                .bodyToMono(Void.class);
    }

    public Mono<TokenResponse> loginUser(String username, String password) {
        return webClient.post()
                .uri(keycloakServerUrl + "/realms/" + realm + "/protocol/openid-connect/token")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters
                        .fromFormData("grant_type", "password")
                        .with("client_id", clientId)
                        .with("client_secret", clientSecret)
                        .with("username", username)
                        .with("password", password))
                .retrieve()
                .bodyToMono(TokenResponse.class);
    }
    public Mono<Void> logoutUser(String accessToken) {
        return webClient.post()
                .uri(keycloakServerUrl + "/realms/" + realm + "/protocol/openid-connect/logout")
                .header("Authorization", "Bearer " + accessToken) // Send the current access token to invalidate
                .retrieve()
                .bodyToMono(Void.class);
    }
    public Mono<TokenResponse> exchangeGoogleCodeForToken(String code) {
        return webClient.post()
                .uri(keycloakServerUrl + "/realms/" + realm + "/protocol/openid-connect/token")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters
                        .fromFormData("grant_type", "authorization_code")
                        .with("client_id", clientId)
                        .with("client_secret", clientSecret)
                        .with("code", code)
                        .with("redirect_uri", "http://localhost:8090/users/oauth2/callback/google"))
                .retrieve()
                .bodyToMono(TokenResponse.class);
    }


}
