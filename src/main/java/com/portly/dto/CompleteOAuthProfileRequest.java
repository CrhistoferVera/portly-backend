package com.portly.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class CompleteOAuthProfileRequest {

    @NotBlank(message = "La profesión no puede estar vacía")
    private String profesion;

    @NotBlank(message = "La reseña no puede estar vacía")
    @Size(max = 500, message = "La reseña debe tener menos de 500 caracteres")
    private String resena;
}
