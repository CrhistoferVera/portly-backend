package com.portly.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReportarDenunciaRequest {

    @NotNull(message = "El ID del portafolio es obligatorio")
    private UUID portfolioId;

    @NotBlank(message = "El motivo es obligatorio")
    private String reason;

    @Size(max = 300, message = "La descripción no puede superar los 300 caracteres")
    private String description;

    @NotBlank(message = "El identificador del reportante es obligatorio")
    private String reportedBy;

    private String reporterName;
    private String reporterAvatar;
}
