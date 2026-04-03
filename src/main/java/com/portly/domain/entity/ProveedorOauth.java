package com.portly.domain.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(
    name = "autenticacion_externa",
    uniqueConstraints = @UniqueConstraint(
        name = "autenticacion_externa_usuario_proveedor_unique",
        columnNames = {"id_usuario", "nombre_proveedor"}
    )
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProveedorOauth {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_proveedor", nullable = false)
    private Integer idProveedor;

    @JsonBackReference("usuario-proveedores")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario", nullable = false,
                foreignKey = @ForeignKey(name = "fk_autenticacion_externa_usuario"))
    private Usuario usuario;

    @Column(name = "nombre_proveedor", nullable = false, length = 30)
    private String nombreProveedor;

    @Column(name = "id_usuario_proveedor", nullable = false, length = 255)
    private String idUsuarioProveedor;

    @Column(name = "nombre_usuario_externo", length = 100)
    private String nombreUsuarioExterno;

    @Column(name = "clave_acceso_proveedor", nullable = false, columnDefinition = "TEXT")
    private String claveAccesoProveedor;

    @Column(name = "clave_actualizacion", columnDefinition = "TEXT")
    private String claveActualizacion;

    @Column(name = "fecha_expiracion_clave")
    private LocalDateTime fechaExpiracionClave;

    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion;

    @Column(name = "fecha_ultima_sincronizacion")
    private LocalDateTime fechaUltimaSincronizacion;

    // JSON con los repos de GitHub u otros metadatos del proveedor
    @Column(name = "metadatos", columnDefinition = "TEXT")
    private String metadatos;
}
