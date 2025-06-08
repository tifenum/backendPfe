package com.pfe.users.service;

import com.pfe.users.DTO.ClientUserDTO;
import lombok.Getter;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import java.util.List;
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
    @Autowired
    private NewsletterEmailService emailService;

    public ResponseEntity<List<ClientUserDTO>> getAllClients() {
        Keycloak keycloak = null;
        try {
            String adminUsername = "admin";
            String adminPassword = "123456789";
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

            return ResponseEntity.ok(clients);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
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
    public ResponseEntity<Void> deleteUser(String userId) {
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

            keycloak.realm(realm).users().get(userId).remove();
            logger.info("User {} deleted successfully", userId);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            logger.error("Error deleting user {}: {}", userId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
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
    public ResponseEntity<ClientUserDTO> getUserById(String userId) {
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

            UserRepresentation user = keycloak.realm(realm).users().get(userId).toRepresentation();
            if (user == null) {
                return ResponseEntity.notFound().build();
            }

            ClientUserDTO client = new ClientUserDTO(
                    user.getId(),
                    user.getUsername(),
                    user.getEmail()
            );
            return ResponseEntity.ok(client);
        } catch (Exception e) {
            logger.error("Error fetching user {}: {}", userId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
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
    public ResponseEntity<String> subscribeToNewsletter(String name, String email) {
        try {
            if (email == null || email.isEmpty() || !email.contains("@")) {
                return ResponseEntity.badRequest().body("Invalid email address");
            }

            emailService.sendNewsletterSubscriptionEmail(email, name);
            logger.info("Newsletter subscription processed for email: {}", email);

            return ResponseEntity.ok("Successfully subscribed to newsletter");
        } catch (Exception e) {
            logger.error("Failed to subscribe to newsletter for email {}: {}", email, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to subscribe: " + e.getMessage());
        }
    }
    public ResponseEntity<String> submitContactRequest(String name, String email, String message) {
        try {
            emailService.sendContactConfirmationEmail(email, name);
            logger.info("Contact request processed for email: {}", email);
            // TODO: Store message or notify support team (e.g., save to database or send to support email)
            return ResponseEntity.ok("Contact request submitted successfully");
        } catch (Exception e) {
            logger.error("Failed to process contact request for email {}: {}", email, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to submit contact request: " + e.getMessage());
        }
    }
}
