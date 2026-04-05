package com.portly.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@Slf4j
public class EmailService {

    @Value("${brevo.api.key}")
    private String brevoApiKey;

    @Value("${spring.mail.username}")
    private String correoRemitente;

    private final RestTemplate restTemplate = new RestTemplate();

    @Async
    public void enviarCodigoRecuperacion(String destino, String codigo) {
        log.info("Enviando código de recuperación: destino={}", destino);
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("api-key", brevoApiKey);
            headers.setContentType(MediaType.APPLICATION_JSON);

            String body = """
                {
                    "sender": {"name": "Portly", "email": "%s"},
                    "to": [{"email": "%s"}],
                    "subject": "Código de Recuperación - Portly",
                    "textContent": "Hola,\\n\\nHas solicitado restablecer tu contraseña en Portly.\\nTu código de verificación de 6 dígitos es: %s\\n\\nEste código expirará en 10 minutos.\\nSi no solicitaste este cambio, ignora este correo."
                }
                """.formatted(correoRemitente, destino, codigo);

            HttpEntity<String> entity = new HttpEntity<>(body, headers);
            restTemplate.postForEntity("https://api.brevo.com/v3/smtp/email", entity, String.class);
            log.info("Código enviado correctamente: destino={}", destino);
        } catch (Exception ex) {
            log.error("Error al enviar email: destino={}, error={}", destino, ex.getMessage());
            throw new RuntimeException(ex);
        }
    }
}
