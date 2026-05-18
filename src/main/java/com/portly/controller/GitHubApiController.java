package com.portly.controller;

import com.portly.dto.FrontGitHubRepoResponse;
import com.portly.dto.GitHubRepoDto;
import com.portly.service.GitHubRepoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Controlador adaptador para repos de GitHub.
 * Expone GET /api/github/repos que es la ruta que el frontend espera
 * (en vez de /api/proyectos/github/repos del controlador original).
 */
@RestController
@RequestMapping("/api/github")
@RequiredArgsConstructor
public class GitHubApiController {

    private final GitHubRepoService gitHubRepoService;

    @GetMapping("/repos")
    public ResponseEntity<List<FrontGitHubRepoResponse>> listarRepos(Authentication auth) {
        UUID idUsuario = (UUID) auth.getPrincipal();

        List<GitHubRepoDto> backendRepos = gitHubRepoService.listarReposDisponibles(idUsuario);

        List<FrontGitHubRepoResponse> frontRepos = backendRepos.stream()
                .map(r -> FrontGitHubRepoResponse.builder()
                        .id(r.getRepoId() != null ? Integer.parseInt(r.getRepoId()) : 0)
                        .name(r.getNombre())
                        .fullName(r.getNombreCompleto())
                        .description(r.getDescripcion())
                        .htmlUrl(r.getUrlHtml())
                        .createdAt(r.getFechaCreacion())
                        .updatedAt(r.getFechaActualizacion())
                        .stargazersCount(r.getStargazersCount() != null ? r.getStargazersCount() : 0)
                        .languages(r.getLenguajes() != null ? r.getLenguajes() : Collections.emptyList())
                        .topics(Collections.emptyList()) // GitHub API no devuelve topics en /repos :((((((
                        .build())
                .collect(Collectors.toList());

        return ResponseEntity.ok(frontRepos);
    }
}
