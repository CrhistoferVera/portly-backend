package com.portly.domain.repository;

import com.portly.domain.entity.Portafolio;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface PortafolioRepository extends JpaRepository<Portafolio, UUID> {

    List<Portafolio> findByUsuario_IdUsuarioOrderByFechaCreacionDesc(UUID idUsuario);

    List<Portafolio> findByPlantilla_IdPlantilla(String idPlantilla);

    boolean existsByUrlPublica(String urlPublica);

    boolean existsByUsuario_IdUsuarioAndNombreIgnoreCase(UUID idUsuario, String nombre);

    java.util.Optional<Portafolio> findByUrlPublicaEndingWith(String suffix);
}
