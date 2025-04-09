package com.pfe.users.service;

import com.pfe.users.DTO.ClientUserDTO;
import com.pfe.users.user.User;
import com.pfe.users.DTO.TokenResponse;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;
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
        var payload = Map.of(
                "username", user.getEmail(),
                "email",    user.getEmail(),
                "enabled",  true,
                "credentials", new Object[]{
                        Map.of("type","password",
                                "value",user.getPassword(),
                                "temporary",false)
                }
        );

        return webClient.post()
                .uri(keycloakServerUrl + "/admin/realms/" + realm + "/users")
                .header("Authorization","Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(payload))
                .exchangeToMono(response -> {
                    if (response.statusCode().equals(HttpStatus.CREATED)) {
                        // 1) Grab the userId
                        String location = response.headers()
                                .asHttpHeaders()
                                .getFirst("Location");
                        String userId = location.substring(location.lastIndexOf('/') + 1);
                        // 2) Assign the realm role
                        return assignRoleToUser(userId, "client");
                    } else if (response.statusCode().equals(HttpStatus.CONFLICT)) {
                        // user already exists
                        return Mono.error(new IllegalStateException("User already exists"));
                    } else {
                        // some other error
                        return response.createException()
                                .flatMap(Mono::error);
                    }
                });
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
    private Mono<String> findUserIdByEmail(String email) {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path(keycloakServerUrl + "/admin/realms/" + realm + "/users")
                        .queryParam("email", email)
                        .build())
                .header("Authorization", "Bearer " + adminToken)
                .retrieve()
                .bodyToMono(User[].class)
                .map(users -> users.length > 0 ? users[0].getId() : null);
    }

    private Mono<Void> assignRoleToUser(String userId, String roleName) {
        return webClient.get()
                .uri(keycloakServerUrl + "/admin/realms/" + realm + "/roles/" + roleName)
                .header("Authorization", "Bearer " + adminToken)
                .retrieve()
                .bodyToMono(Map.class)
                .flatMap(role -> webClient.post()
                        .uri(keycloakServerUrl + "/admin/realms/" + realm + "/users/" + userId + "/role-mappings/realm")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(List.of(role))
                        .retrieve()
                        .bodyToMono(Void.class));
    }
    public Mono<List<ClientUserDTO>> getAllClients() {
        // Step 1: Get all users
        return webClient.get()
                .uri(keycloakServerUrl + "/admin/realms/" + realm + "/users")
                .header("Authorization", "Bearer " + adminToken)
                .retrieve()
                .bodyToFlux(Map.class)
                .filterWhen(user -> {
                    String userId = (String) user.get("id");
                    // Step 2: Check if user has 'client' role
                    return webClient.get()
                            .uri(keycloakServerUrl + "/admin/realms/" + realm + "/users/" + userId + "/role-mappings/realm")
                            .header("Authorization", "Bearer " + adminToken)
                            .retrieve()
                            .bodyToFlux(Map.class)
                            .any(role -> "client".equals(role.get("name")));
                })
                .map(user -> new ClientUserDTO(
                        (String) user.get("id"),
                        (String) user.get("username"),
                        (String) user.get("email")
                ))
                .collectList();
    }

}
