package com.portly.domain.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(
    name = "proveedor_oauth",
    uniqueConstraints = @UniqueConstraint(
        name = "proveedor_oauth_usuario_proveedor_unique",
        columnNames = {"usuario_id", "proveedor"}
    )
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProveedorOauth {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @JsonBackReference("usuario-proveedores")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false,
                foreignKey = @ForeignKey(name = "fk_proveedor_oauth_usuario"))
    private Usuario usuario;

    @Column(name = "proveedor", nullable = false, length = 30)
    private String proveedor;

    @Column(name = "proveedor_user_id", nullable = false, length = 255)
    private String proveedorUserId;

    @Column(name = "username_externo", length = 100)
    private String usernameExterno;

    @Column(name = "access_token", nullable = false, columnDefinition = "TEXT")
    private String accessToken;

    @Column(name = "refresh_token", columnDefinition = "TEXT")
    private String refreshToken;

    @Column(name = "expira_token")
    private LocalDateTime expiraToken;

    @Column(name = "creado_en", nullable = false)
    private LocalDateTime creadoEn;

    @Column(name = "ultima_sync")
    private LocalDateTime ultimaSync;

    /** pa los repos */
    @Column(name = "metadatos", columnDefinition = "TEXT")
    private String metadatos;
}
