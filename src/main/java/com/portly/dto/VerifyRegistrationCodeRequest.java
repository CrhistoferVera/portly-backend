package com.portly.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class VerifyRegistrationCodeRequest {

    @NotBlank(message = "El correo es obligatorio")
    private String email;

    @NotBlank(message = "El código es obligatorio")
    private String codigo;
}
