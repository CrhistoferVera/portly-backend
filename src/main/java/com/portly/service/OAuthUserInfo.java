package com.portly.service;

import lombok.Builder;
import lombok.Data;


@Data
@Builder
public class OAuthUserInfo {

    private String proveedor;          
    private String proveedorUserId;    
    private String usernameExterno;   
    private String email;
    private String nombres;
    private String apellidos;
    private String titularProfesional; 
    private String fotoUrl;
    private String urlPerfil;          
    private String accessToken;
    private String refreshToken;
    private String metadatos;          
}
