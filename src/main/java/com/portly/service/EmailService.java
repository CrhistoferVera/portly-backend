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
    public void enviarCodigoRegistro(String destino, String codigo) {
        log.info("Enviando código de registro: destino={}", destino);
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("api-key", brevoApiKey);
            headers.setContentType(MediaType.APPLICATION_JSON);

            String body = """
                {
                    "sender": {"name": "Portly", "email": "%s"},
                    "to": [{"email": "%s"}],
                    "subject": "Verifica tu correo - Portly",
                    "textContent": "Hola,\\n\\nGracias por registrarte en Portly.\\nTu código de verificación de 6 dígitos es: %s\\n\\nEste código expirará en 10 minutos.\\nSi no solicitaste este registro, ignora este correo."
                }
                """.formatted(correoRemitente, destino, codigo);

            HttpEntity<String> entity = new HttpEntity<>(body, headers);
            restTemplate.postForEntity("https://api.brevo.com/v3/smtp/email", entity, String.class);
            log.info("Código de registro enviado correctamente: destino={}", destino);
        } catch (Exception ex) {
            log.error("Error al enviar email de registro: destino={}, error={}", destino, ex.getMessage());
            throw new RuntimeException(ex);
        }
    }

    @Async
    public void enviarNotificacionEmailRegistrado(String destino) {
        log.info("Enviando notificación de email ya registrado: destino={}", destino);
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("api-key", brevoApiKey);
            headers.setContentType(MediaType.APPLICATION_JSON);

            String body = """
                {
                    "sender": {"name": "Portly", "email": "%s"},
                    "to": [{"email": "%s"}],
                    "subject": "Intento de registro - Portly",
                    "textContent": "Hola,\\n\\nAlguien intentó registrarse en Portly con este correo, pero ya existe una cuenta asociada a él.\\n\\nSi fuiste tú, inicia sesión normalmente. Si no fuiste tú, ignora este correo."
                }
                """.formatted(correoRemitente, destino);

            HttpEntity<String> entity = new HttpEntity<>(body, headers);
            restTemplate.postForEntity("https://api.brevo.com/v3/smtp/email", entity, String.class);
        } catch (Exception ex) {
            log.error("Error al enviar notificación de email registrado: destino={}, error={}", destino, ex.getMessage());
        }
    }

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
    @Async
    public void enviarNotificacionRestriccionDenunciantes(String destino, String nombreUsuarioRestringido) {
        log.info("Enviando notificación de restricción al denunciante: destino={}", destino);
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("api-key", brevoApiKey);
            headers.setContentType(MediaType.APPLICATION_JSON);

            String body = """
                {
                    "sender": {"name": "Portly", "email": "%s"},
                    "to": [{"email": "%s"}],
                    "subject": "Actualización sobre tu denuncia - Portly",
                    "textContent": "Hola,\\n\\nTe informamos que, tras revisar tu denuncia, hemos tomado medidas. El usuario %s ha sido restringido en nuestra plataforma por infringir nuestras políticas.\\n\\nGracias por ayudarnos a mantener una comunidad segura."
                }
                """.formatted(correoRemitente, destino, nombreUsuarioRestringido);

            HttpEntity<String> entity = new HttpEntity<>(body, headers);
            restTemplate.postForEntity("https://api.brevo.com/v3/smtp/email", entity, String.class);
        } catch (Exception ex) {
            log.error("Error al enviar notificación de restricción: destino={}, error={}", destino, ex.getMessage());
        }
    }

    @Async
    public void enviarNotificacionSuspensionDenunciantes(String destino, String nombreUsuarioSuspendido) {
        log.info("Enviando notificación de suspensión al denunciante: destino={}", destino);
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("api-key", brevoApiKey);
            headers.setContentType(MediaType.APPLICATION_JSON);

            String body = """
                {
                    "sender": {"name": "Portly", "email": "%s"},
                    "to": [{"email": "%s"}],
                    "subject": "Actualización sobre tu denuncia - Portly",
                    "textContent": "Hola,\\n\\nTe informamos que, tras revisar tu denuncia, hemos tomado medidas. El usuario %s ha sido suspendido de la plataforma por infringir nuestras políticas.\\n\\nGracias por ayudarnos a mantener una comunidad segura."
                }
                """.formatted(correoRemitente, destino, nombreUsuarioSuspendido);

            HttpEntity<String> entity = new HttpEntity<>(body, headers);
            restTemplate.postForEntity("https://api.brevo.com/v3/smtp/email", entity, String.class);
        } catch (Exception ex) {
            log.error("Error al enviar notificación de suspensión: destino={}, error={}", destino, ex.getMessage());
        }
    }

    @Async
    public void enviarNotificacionReactivacionCuenta(String destino, String nombreUsuario) {
        log.info("Enviando notificación de reactivación de cuenta: destino={}", destino);
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("api-key", brevoApiKey);
            headers.setContentType(MediaType.APPLICATION_JSON);

            String body = """
                {
                    "sender": {"name": "Portly", "email": "%s"},
                    "to": [{"email": "%s"}],
                    "subject": "Tu cuenta ha sido reactivada - Portly",
                    "textContent": "Hola %s,\\n\\nTe informamos que tu cuenta ha sido reactivada exitosamente. Ya puedes volver a disfrutar de todas las funcionalidades de Portly.\\n\\nRecuerda cumplir siempre con nuestras políticas de la comunidad."
                }
                """.formatted(correoRemitente, destino, nombreUsuario);

            HttpEntity<String> entity = new HttpEntity<>(body, headers);
            restTemplate.postForEntity("https://api.brevo.com/v3/smtp/email", entity, String.class);
        } catch (Exception ex) {
            log.error("Error al enviar notificación de reactivación: destino={}, error={}", destino, ex.getMessage());
        }
    }
}
