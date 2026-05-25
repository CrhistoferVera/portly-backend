package com.portly.service;

import com.portly.domain.repository.PerfilUsuarioRepository;
import com.portly.domain.repository.PortafolioRepository;
import com.portly.domain.repository.UsuarioRepository;
import com.portly.dto.DashboardStatsResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminDashboardService {

    private final UsuarioRepository usuarioRepository;
    private final PortafolioRepository portafolioRepository;
    private final PerfilUsuarioRepository perfilUsuarioRepository;

    public DashboardStatsResponse getDashboardStats() {
        LocalDateTime desde = LocalDateTime.now().minusDays(7);

        long usuariosRegistradosSemana = usuarioRepository.countByFechaCreacionAfter(desde);
        long portafoliosPublicosSemana = portafolioRepository.countPublicosDesde(desde);
        long cuentasSuspendidas = usuarioRepository.countSuspendidos();

        List<DashboardStatsResponse.PlantillaStats> plantillasMasUsadas =
                portafolioRepository.findTopPlantillas(PageRequest.of(0, 3));

        List<DashboardStatsResponse.ProfesionStats> profesionesMasRegistradas =
                perfilUsuarioRepository.findTopProfesiones(PageRequest.of(0, 5));

        return DashboardStatsResponse.builder()
                .usuariosRegistradosSemana(usuariosRegistradosSemana)
                .portafoliosPublicosSemana(portafoliosPublicosSemana)
                .denunciasPendientes(0)
                .cuentasSuspendidas(cuentasSuspendidas)
                .plantillasMasUsadas(plantillasMasUsadas)
                .profesionesMasRegistradas(profesionesMasRegistradas)
                .build();
    }
}
