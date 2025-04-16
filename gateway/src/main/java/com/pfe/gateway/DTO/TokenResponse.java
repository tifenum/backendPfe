package com.pfe.gateway.DTO;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TokenResponse {
    private String access_token;
    private String refresh_token;
    private int expires_in;
    private String token_type;

    public TokenResponse(String s) {

    }
}
