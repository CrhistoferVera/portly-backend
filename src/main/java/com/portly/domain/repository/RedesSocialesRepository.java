package com.portly.domain.repository;

import com.portly.domain.entity.RedesSociales;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RedesSocialesRepository extends JpaRepository<RedesSociales, Integer> {
    Optional<RedesSociales> findByPerfilUsuario_IdPerfilUsuarioAndNombre(Integer idPerfilUsuario, String nombre);
    List<RedesSociales> findByPerfilUsuario_Usuario_Email(String email);
}
