package com.portly.domain.repository;

import com.portly.domain.entity.CodigoRecuperacion;
import com.portly.domain.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface CodigoRecuperacionRepository extends JpaRepository<CodigoRecuperacion, Integer> {
    Optional<CodigoRecuperacion> findByUsuarioAndCodigo(Usuario usuario, String codigo);
    void deleteByUsuario(Usuario usuario);
}