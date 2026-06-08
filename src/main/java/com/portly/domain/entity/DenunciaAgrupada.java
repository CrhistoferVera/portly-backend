package com.portly.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "denuncia_agrupada")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(exclude = {"denunciasIndividuales", "portafolio", "ownerUsuario"})
public class DenunciaAgrupada {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    @EqualsAndHashCode.Include
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "portfolio_id", nullable = false,
                foreignKey = @ForeignKey(name = "fk_denuncia_agrupada_portafolio"))
    private Portafolio portafolio;

    @Column(name = "portfolio_title", nullable = false, length = 255)
    private String portfolioTitle;

    @Column(name = "portfolio_public_url", nullable = false, length = 255)
    private String portfolioPublicUrl;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_user_id", nullable = false,
                foreignKey = @ForeignKey(name = "fk_denuncia_agrupada_usuario"))
    private Usuario ownerUsuario;

    @Column(name = "owner_user_name", nullable = false, length = 255)
    private String ownerUserName;

    @Column(name = "owner_user_status", nullable = false, length = 20)
    private String ownerUserStatus;

    @Builder.Default
    @Column(name = "status", nullable = false, length = 20)
    private String status = "pendiente";

    @Column(name = "revision_resultado", columnDefinition = "TEXT")
    private String revisionResultado;

    @Column(name = "revision_fecha")
    private LocalDateTime revisionFecha;

    @Column(name = "revision_admin_id", length = 50)
    private String revisionAdminId;

    @Builder.Default
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Builder.Default
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();

    @Builder.Default
    @OneToMany(mappedBy = "denunciaAgrupada", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DenunciaIndividual> denunciasIndividuales = new ArrayList<>();

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
