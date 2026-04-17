package com.portly.domain.repository;

import com.portly.domain.entity.ProyectoRepositorio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface ProyectoRepositorioRepository extends JpaRepository<ProyectoRepositorio, Long> {

    @Modifying
    @Query("DELETE FROM ProyectoRepositorio r WHERE r.proyecto.idProyecto = :idProyecto")
    void deleteByProyecto_IdProyecto(Long idProyecto);
}
