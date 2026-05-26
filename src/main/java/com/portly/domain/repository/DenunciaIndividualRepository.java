package com.portly.domain.repository;

import com.portly.domain.entity.DenunciaIndividual;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DenunciaIndividualRepository extends JpaRepository<DenunciaIndividual, Long> {

    boolean existsByDenunciaAgrupada_IdAndCreadoPor(Long denunciaAgrupadaId, String creadoPor);
}
