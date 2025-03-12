package com.pfe.users.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import java.util.Collection;
import java.util.Collections;

public class JwtAuthenticationToken implements Authentication {
    private final String username;
    private boolean authenticated;

    public JwtAuthenticationToken(String username) {
        this.username = username;
        this.authenticated = true;
    }

    @Override public String getName() { return username; }
    @Override public Collection<? extends GrantedAuthority> getAuthorities() { return Collections.emptyList(); }
    @Override public Object getCredentials() { return null; }
    @Override public Object getDetails() { return null; }
    @Override public Object getPrincipal() { return username; }
    @Override public boolean isAuthenticated() { return authenticated; }
    @Override public void setAuthenticated(boolean isAuthenticated) { this.authenticated = isAuthenticated; }
}