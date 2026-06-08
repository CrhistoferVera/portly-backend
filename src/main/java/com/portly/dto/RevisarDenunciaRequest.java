package com.portly.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RevisarDenunciaRequest {

    @NotBlank(message = "El resultado de la revisión es obligatorio")
    private String resultado;
}
