package com.portly.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * DTO que representa un repositorio de GitHub disponible para importar.
 * Lo devuelve GET /api/proyectos/github/repos
 */
@Data
@Builder
public class GitHubRepoDto {

    /** ID numérico del repositorio en GitHub */
    private String repoId;

    /** Nombre del repositorio (ej. "my-portfolio") */
    private String nombre;

    /** Nombre completo owner/repo (ej. "usuario/my-portfolio") */
    private String nombreCompleto;

    private String descripcion;

    /** Fecha de creación en GitHub (ISO-8601) */
    private String fechaCreacion;

    /** Fecha de última actualización en GitHub (ISO-8601) */
    private String fechaActualizacion;

    /** Lenguajes detectados por GitHub (ej. ["Java","TypeScript"]) */
    private List<String> lenguajes;

    private String urlHtml;

    private Boolean esPrivado;

    /** Número de estrellas del repositorio */
    private Integer stargazersCount;
}
