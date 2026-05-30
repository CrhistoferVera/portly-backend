package com.portly.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class CompleteOAuthProfileRequest {

    @NotBlank(message = "El nombre de usuario es obligatorio")
    @Size(min = 3, max = 30, message = "El nombre de usuario debe tener entre 3 y 30 caracteres")
    @Pattern(
        regexp = "^[a-zA-Z0-9_]+$",
        message = "Solo se permiten letras, números y guión bajo (_)"
    )
    private String username;

    @NotBlank(message = "La profesión no puede estar vacía")
    private String profesion;

    @NotBlank(message = "La reseña no puede estar vacía")
    @Size(max = 500, message = "La reseña debe tener menos de 500 caracteres")
    private String resena;
}
