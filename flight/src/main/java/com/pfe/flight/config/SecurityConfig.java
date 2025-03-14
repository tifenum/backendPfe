package com.pfe.flight.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        return http
                // Disable CSRF protection for testing or non-browser clients
                .csrf(csrf -> csrf.disable())
                // Allow all exchanges without authentication
                .authorizeExchange(exchanges -> exchanges
                        .anyExchange().permitAll())
                .build();
    }
}
