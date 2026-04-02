package com.portly.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

@Getter
@AllArgsConstructor
public class AuthResponse {

    private String token;
    private UUID   usuarioId;
    private String email;
    private String rol;
}
