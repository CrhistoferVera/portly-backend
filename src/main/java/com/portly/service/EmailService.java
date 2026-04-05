package com.portly.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    // Spring inyectará aquí el correo que pusiste en las variables de entorno
    @Value("${spring.mail.username:}")
    private String correoRemitente;

    @Async
    public void enviarCodigoRecuperacion(String destino, String codigo) {
        log.info("Enviando código de recuperación: destino={}", destino);
        try {
            SimpleMailMessage mensaje = new SimpleMailMessage();
            mensaje.setFrom(correoRemitente);
            mensaje.setTo(destino);
            mensaje.setSubject("Código de Recuperación - Portly");
            mensaje.setText("""
                    Hola,

                    Has solicitado restablecer tu contraseña en Portly.
                    Tu código de verificación de 6 dígitos es: %s

                    Este código expirará en 10 minutos.
                    Si no solicitaste este cambio, ignora este correo.
                    """.formatted(codigo));
            mailSender.send(mensaje);
            log.info("Código de recuperación enviado correctamente: destino={}", destino);
        } catch (MailException ex) {
            log.error("Error al enviar email de recuperación: destino={}, error={}", destino, ex.getMessage());
            throw ex;
        }
    }
}