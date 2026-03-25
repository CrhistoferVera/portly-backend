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

    private UUID      usuarioId;
    private String    email;
    private String    rol;
    private String    estado;
    private Boolean   emailVerificado;
    private LocalDateTime fechaRegistro;
    private LocalDateTime ultimoAcceso;

    // datos par perfil 
    private String nombres;
    private String apellidos;
    private String titularProfesional;
    private String sobreMi;
    private String fotoUrl;
    private String pais;
    private String ciudad;

    // proveedores OAuth vinculados por el momento linkedIn y GitHub lo que me tocaba le falta al de google quien sera
    private List<ProveedorDto> proveedores;

    // enlaces
    private List<EnlaceDto> enlaces;

    // experiencia laboral
    private List<ExperienciaDto> experiencias;


    @Data @Builder
    public static class ProveedorDto {
        private String proveedor;
        private String usernameExterno;
        private LocalDateTime ultimaSync;
        private String metadatos; // JSON de repos de GitHub, etc.
    }

    @Data @Builder
    public static class EnlaceDto {
        private String plataforma;
        private String urlPerfil;
        private Boolean visible;
    }

    @Data @Builder
    public static class ExperienciaDto {
        private String  empresa;
        private String  cargo;
        private String  modalidad;
        private LocalDate fechaIni;
        private LocalDate fechaFin;
        private String  descripcion;
        private Boolean esEmpleoActual;
    }
}
