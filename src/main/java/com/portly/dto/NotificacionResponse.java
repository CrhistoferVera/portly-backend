package com.portly.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class NotificacionResponse {
    private UUID id;
    private String mensaje;
    private Boolean leido;
    private LocalDateTime fechaCreacion;
}
