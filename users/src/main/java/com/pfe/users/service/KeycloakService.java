package com.pfe.users.service;

import com.pfe.users.DTO.ClientUserDTO;
import com.pfe.users.DTO.TokenResponse;
import com.pfe.users.user.User;
import lombok.Getter;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class KeycloakService {

    private static final Logger logger = LoggerFactory.getLogger(KeycloakService.class);
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


    public Mono<ResponseEntity<List<ClientUserDTO>>> getAllClients() {
        Keycloak keycloak = null;
        try {
            String adminUsername = "admin";
            String adminPassword = "admin";
            keycloak = KeycloakBuilder.builder()
                    .serverUrl(keycloakServerUrl)
                    .realm("master")
                    .clientId("admin-cli")
                    .username(adminUsername)
                    .password(adminPassword)
                    .grantType("password")
                    .build();

            List<UserRepresentation> users = keycloak.realm(realm).users().list();

            Keycloak finalKeycloak = keycloak;
            List<ClientUserDTO> clients = users.stream()
                    .filter(user -> {
                        List<RoleRepresentation> realmRoles = finalKeycloak.realm(realm)
                                .users()
                                .get(user.getId())
                                .roles()
                                .realmLevel()
                                .listEffective();
                        boolean hasClientRole = realmRoles.stream()
                                .anyMatch(role -> "client".equals(role.getName()));
                        logger.debug("User {} has client role: {}", user.getId(), hasClientRole);
                        return hasClientRole;
                    })
                    .map(user -> new ClientUserDTO(
                            user.getId(),
                            user.getUsername(),
                            user.getEmail()
                    ))
                    .collect(Collectors.toList());

            return Mono.just(ResponseEntity.ok(clients));
        } catch (Exception e) {
            return Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null));
        } finally {
            if (keycloak != null) {
                try {
                    keycloak.close();
                } catch (Exception e) {
                    logger.warn("Failed to close Keycloak admin client: {}", e.getMessage());
                }
            }
        }
    }
}