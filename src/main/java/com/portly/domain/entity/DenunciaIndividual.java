package com.portly.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "denuncia_individual")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(exclude = {"denunciaAgrupada"})
public class DenunciaIndividual {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    @EqualsAndHashCode.Include
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "denuncia_agrupada_id", nullable = false,
                foreignKey = @ForeignKey(name = "fk_denuncia_individual_agrupada"))
    private DenunciaAgrupada denunciaAgrupada;

    @Column(name = "motivo", nullable = false, length = 50)
    private String motivo;

    @Column(name = "descripcion", length = 300)
    private String descripcion;

    @Column(name = "creado_por", nullable = false, length = 100)
    private String creadoPor;

    @Column(name = "reporter_name")
    private String reporterName;

    @Column(name = "reporter_avatar")
    private String reporterAvatar;

    @Builder.Default
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}
