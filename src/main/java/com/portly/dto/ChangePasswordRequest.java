package com.portly.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ChangePasswordRequest {
    @NotBlank(message = "El email no puede estar vacío")
    @Email(message = "Formato de email inválido")
    private String email;

    @NotBlank(message = "La contraseña actual no puede estar vacía")
    private String contrasenaActual;

    @NotBlank(message = "La nueva contraseña no puede estar vacía")
    private String nuevaContrasena;
}
