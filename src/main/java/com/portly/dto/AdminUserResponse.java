package com.portly.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminUserResponse {
    private UUID idUsuario;
    private String email;
    private String nombreCompleto;
    private LocalDateTime fechaCreacion;
    private String estado;
    private Boolean hasPublicPortfolio;
}
