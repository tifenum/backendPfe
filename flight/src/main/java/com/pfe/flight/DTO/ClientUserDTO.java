package com.pfe.flight.DTO;
import lombok.Getter;

@Getter

public class ClientUserDTO {
    private String id;
    private String email;

    public ClientUserDTO() {
    }

    public ClientUserDTO(String id, String email) {
        this.id = id;
        this.email = email;
    }
}
