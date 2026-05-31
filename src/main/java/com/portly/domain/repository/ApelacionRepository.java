package com.portly.domain.repository;

import com.portly.domain.entity.Apelacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ApelacionRepository extends JpaRepository<Apelacion, Long> {
    List<Apelacion> findAllByOrderByFechaApelacionDesc();
}
