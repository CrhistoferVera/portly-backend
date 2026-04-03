package com.portly.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;


@Data
@Builder
public class UsuarioProfileResponse {

    private UUID      idUsuario;
    private String    email;
    private String    rol;
    private String    estado;
    private Boolean   correoVerificado;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaUltimoAcceso;

    // datos del perfil
    private String nombre;
    private String apellido;
    private String titularProfesional;
    private String acercaDeMi;
    private String enlaceFoto;
    private String pais;
    private String ciudad;

    // proveedores OAuth vinculados (LinkedIn, GitHub, Google)
    private List<ProveedorDto> proveedores;

    // enlaces profesionales
    private List<EnlaceDto> enlaces;

    // experiencia laboral
    private List<ExperienciaDto> experiencias;


    @Data @Builder
    public static class ProveedorDto {
        private String nombreProveedor;
        private String nombreUsuarioExterno;
        private LocalDateTime fechaUltimaSincronizacion;
        private String metadatos;
    }

    @Data @Builder
    public static class EnlaceDto {
        private String plataformaProfesional;
        private String direccionEnlace;
        private Boolean esVisible;
    }

    @Data @Builder
    public static class ExperienciaDto {
        private String  empresa;
        private String  cargo;
        private String  modalidadTrabajo;
        private LocalDate fechaInicio;
        private LocalDate fechaFin;
        private String  descripcion;
        private Boolean esEmpleoActual;
    }
}
