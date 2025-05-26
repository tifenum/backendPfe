package com.pfe.gateway.DTO;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class ResetPasswordRequest {
    private String email;
}