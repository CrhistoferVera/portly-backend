package com.portly.config;

import com.portly.security.JwtAuthFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // 1. Enciende el CORS en Spring Security (Jala el @Bean de abajo automáticamente)
            .cors(Customizer.withDefaults()) 
            .csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                // 2. Permitir explícitamente las peticiones OPTIONS (el "preflight" del navegador)
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                
                // 3. Rutas públicas
                .requestMatchers("/auth/**").permitAll()
                //.requestMatchers("/error").permitAll()
                
                // 4. Todo lo demás requiere autenticación
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        
        // Aquí pones la URL de tu frontend en React (Suele ser 5173 en Vite o 3000 en Create React App)
        configuration.setAllowedOrigins(List.of(
            "http://localhost:5173",
            "http://localhost:3000",
            "https://portly-frontend-three.vercel.app"
        ));
        
        // Métodos HTTP permitidos
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        
        // Cabeceras permitidas (Authorization es crucial para que pase el JWT)
        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type", "*"));
        
        // Permitir envío de credenciales (cookies, auth headers)
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        // Aplicar esta configuración de CORS a TODAS las rutas de tu API
        source.registerCorsConfiguration("/**", configuration); 
        return source;
    }
}
