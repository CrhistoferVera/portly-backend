package com.portly.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SuspenderUsuarioRequest {

    @NotBlank(message = "El motivo de la suspensión es obligatorio")
    private String motivo;
}
