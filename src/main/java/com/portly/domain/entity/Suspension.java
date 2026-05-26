package com.portly.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "suspension")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(exclude = {"usuario"})
public class Suspension {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    @EqualsAndHashCode.Include
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false,
                foreignKey = @ForeignKey(name = "fk_suspension_usuario"))
    private Usuario usuario;

    @Column(name = "motivo", nullable = false, columnDefinition = "TEXT")
    private String motivo;

    @Builder.Default
    @Column(name = "fecha_suspension", nullable = false)
    private LocalDateTime fechaSuspension = LocalDateTime.now();

    @Column(name = "admin_id", nullable = false, length = 50)
    private String adminId;

    @Column(name = "motivo_cancelacion", columnDefinition = "TEXT")
    private String motivoCancelacion;

    @Builder.Default
    @Column(name = "cancelada", nullable = false)
    private Boolean cancelada = false;

    @Column(name = "fecha_cancelacion")
    private LocalDateTime fechaCancelacion;
}
