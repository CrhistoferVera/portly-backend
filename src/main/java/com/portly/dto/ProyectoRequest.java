package com.portly.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Data
public class ProyectoRequest {

    @NotBlank(message = "El título del proyecto es obligatorio")
    @Size(max = 150)
    private String titulo;

    @Size(max = 500)
    private String resumen;

    private String descripcionRepositorio;



    /** PUBLICO o PRIVADO */
    private String estadoPublicacion;

    private LocalDate fechaInicio;

    private LocalDate fechaFin;

    private Boolean esActual = false;

    // ─── GitHub ──────────────────────────────────────────────────────────────

    /** ID del repo en GitHub, presente solo cuando se importa */
    private String idRepositorioGithub;

    private Boolean importadoDesdeGithub = false;

    // ─── Colecciones ─────────────────────────────────────────────────────────

    /** IDs de habilidades del catálogo (tecnología_proyecto) */
    private List<Long> idHabilidades = new ArrayList<>();



    /** IDs de evidencias ya subidas que se vincularán a este proyecto */
    private List<Integer> idEvidencias = new ArrayList<>();
}
