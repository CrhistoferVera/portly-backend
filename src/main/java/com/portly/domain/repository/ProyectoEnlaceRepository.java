package com.portly.domain.repository;

import com.portly.domain.entity.ProyectoEnlace;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface ProyectoEnlaceRepository extends JpaRepository<ProyectoEnlace, Long> {

    @Modifying
    @Query("DELETE FROM ProyectoEnlace e WHERE e.proyecto.idProyecto = :idProyecto")
    void deleteByProyecto_IdProyecto(Long idProyecto);
}
