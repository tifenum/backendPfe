package com.pfe.gateway.service;

import com.pfe.gateway.DTO.TokenResponse;
import com.pfe.gateway.user.User;
import lombok.Getter;
import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.AccessTokenResponse;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.ws.rs.core.Response;
import java.util.Collections;
import java.util.List;

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

    private final String adminUsername="admin";

    private final String adminPassword= "admin";

    /**
     * Registers a new user in Keycloak using the Admin client.
     */
    public Mono<ResponseEntity<String>> registerUser(User user) {
        try {
            // Build an admin client instance
            Keycloak keycloakAdmin = KeycloakBuilder.builder()
                    .serverUrl(keycloakServerUrl)
                    .realm("master")
                    .username(adminUsername)
                    .password(adminPassword)
                    .clientId("admin-cli")
                    .grantType(OAuth2Constants.PASSWORD)
                    .build();

            UserRepresentation userRep = new UserRepresentation();
            userRep.setUsername(user.getEmail());
            userRep.setEmail(user.getEmail());
            userRep.setEnabled(true);
            userRep.setEmailVerified(false);
            userRep.setRequiredActions(Collections.singletonList("VERIFY_EMAIL"));

            // Set user credentials
            CredentialRepresentation credential = new CredentialRepresentation();
            credential.setType(CredentialRepresentation.PASSWORD);
            credential.setValue(user.getPassword());
            credential.setTemporary(false);
            userRep.setCredentials(Collections.singletonList(credential));

            // Create the user in the target realm
            Response response = keycloakAdmin.realm(realm).users().create(userRep);
            if (response.getStatus() == 201) {
                String userId = org.keycloak.admin.client.CreatedResponseUtil.getCreatedId(response);
                RoleRepresentation clientRole = keycloakAdmin.realm(realm)
                        .roles().get("client").toRepresentation();
                keycloakAdmin.realm(realm).users().get(userId).roles().realmLevel()
                        .add(Collections.singletonList(clientRole));

                // Explicitly send verification email
                keycloakAdmin.realm(realm).users().get(userId).executeActionsEmail(
                        clientId, // Client ID (e.g., "spring-boot-client")
                        null,     // Redirect URI (optional, set to login page if needed)
                        Collections.singletonList("VERIFY_EMAIL")
                );

                return Mono.just(ResponseEntity.ok("User registered successfully. Please check your email to verify."));
            } else if (response.getStatus() == 409) {
                return Mono.just(ResponseEntity
                        .status(HttpStatus.BAD_REQUEST)
                        .body("Error during registration: User already exists"));
            } else {
                return Mono.just(ResponseEntity
                        .status(response.getStatus())
                        .body("Error during registration: " + response.getStatusInfo().toString()));
            }
        } catch (Exception e) {
            logger.error("Error during user registration: {}", e.getMessage(), e);
            return Mono.just(ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Unexpected error during registration: " + e.getMessage()));
        }
    }    /**
     * Logs in a user by building a Keycloak instance using user credentials and retrieves an access token.
     */

    public Mono<ResponseEntity<TokenResponse>> loginUser(String username, String password) {
        logger.debug("Attempting to obtain token for user={} from realm={}", username, realm);
        try {
            Keycloak keycloak = KeycloakBuilder.builder()
                    .serverUrl(keycloakServerUrl)
                    .realm(realm)
                    .clientId(clientId)
                    .clientSecret(clientSecret)
                    .username(username)
                    .password(password)
                    .grantType(OAuth2Constants.PASSWORD)
                    .build();

            logger.debug("Calling Keycloak token endpoint at {}/realms/{}/protocol/openid-connect/token with clientId={}",
                    keycloakServerUrl, realm, clientId);

            AccessTokenResponse tokenResponse = keycloak.tokenManager().getAccessToken();
            TokenResponse token = new TokenResponse();
            token.setAccess_token(tokenResponse.getToken());
            token.setRefresh_token(tokenResponse.getRefreshToken());

            logger.debug("Received token for user={}: accessToken expires in {} seconds", username, tokenResponse.getExpiresIn());
            return Mono.just(ResponseEntity.ok(token));
        } catch (Exception e) {
            logger.error("Failed to login user={} in realm={}: {}", username, realm, e.getMessage(), e);
            return Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null));
        }
    }

    public Mono<ResponseEntity<String>> logoutUser(JwtAuthenticationToken jwtAuth) {
        if (jwtAuth == null) {
            logger.warn("Invalid authentication: JWT token is null");
            return Mono.just(ResponseEntity.badRequest().body("Invalid authentication"));
        }

        String userId = jwtAuth.getToken().getSubject();
        logger.debug("Attempting to log out user with ID: {} for client: {} in realm: {}", userId, clientId, realm);

        Keycloak keycloakAdmin = null;
        try {
            keycloakAdmin = KeycloakBuilder.builder()
                    .serverUrl(keycloakServerUrl)
                    .realm("master")
                    .username(adminUsername)
                    .password(adminPassword)
                    .clientId("admin-cli")
                    .grantType(OAuth2Constants.PASSWORD)
                    .build();

            try {
                UserRepresentation user = keycloakAdmin.realm(realm).users().get(userId).toRepresentation();
                if (user == null) {
                    return Mono.just(ResponseEntity.status(HttpStatus.NOT_FOUND)
                            .body("User not found in Keycloak"));
                }
            } catch (Exception e) {
                return Mono.just(ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("User not found in Keycloak"));
            }

            try {
                keycloakAdmin.realm(realm).users().get(userId).revokeConsent(clientId);
            } catch (javax.ws.rs.NotFoundException e) {
                logger.warn("No consent found for client {} for user {} in realm {}. Proceeding with session logout.", clientId, userId, realm);
            }

            keycloakAdmin.realm(realm).users().get(userId).logout();

            return Mono.just(ResponseEntity.ok("Logged out successfully"));
        } catch (Exception e) {
            return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error during logout: " + e.getMessage()));
        } finally {
            if (keycloakAdmin != null) {
                try {
                    keycloakAdmin.close();
                } catch (Exception e) {
                    logger.warn("Failed to close Keycloak admin client: {}", e.getMessage());
                }
            }
        }
    }
}
