package com.pfe.users.security;

import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.web.server.authentication.AuthenticationWebFilter;
import org.springframework.security.web.server.authentication.ServerAuthenticationConverter;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

public class JwtAuthenticationFilter extends AuthenticationWebFilter {

    public JwtAuthenticationFilter(ReactiveAuthenticationManager authenticationManager, JwtUtil jwtUtil) {
        super(authenticationManager);
        setServerAuthenticationConverter(createJwtConverter(jwtUtil));
    }

    private ServerAuthenticationConverter createJwtConverter(JwtUtil jwtUtil) {
        return exchange -> {
            // Skip authentication for OPTIONS requests
            if (exchange.getRequest().getMethod() == HttpMethod.OPTIONS) {
                return Mono.empty();
            }

            return Mono.justOrEmpty(exchange.getRequest().getHeaders().getFirst("Authorization"))
                    .filter(header -> header.startsWith("Bearer "))
                    .map(header -> header.substring(7))
                    .flatMap(jwtUtil::validateToken)
                    .map(claims -> new JwtAuthenticationToken(claims.getSubject()));
        };
    }
}