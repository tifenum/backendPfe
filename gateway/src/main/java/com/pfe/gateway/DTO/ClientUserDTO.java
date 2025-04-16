package com.pfe.gateway.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ClientUserDTO {
    private String id;
    private String username;
    private String email;
}
