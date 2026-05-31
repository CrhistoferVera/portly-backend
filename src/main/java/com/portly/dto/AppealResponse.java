package com.portly.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AppealResponse {

    private Long id;
    private String userId;
    private String userName;
    private String userEmail;
    private String motivo;
    private LocalDateTime fechaApelacion;
    private String estadoCuenta; // 'restringido' o 'suspendido'
    private String estadoApelacion; // 'pendiente', 'aprobada', 'rechazada'
    private LocalDateTime fechaResolucion;
    private String adminId;
}
