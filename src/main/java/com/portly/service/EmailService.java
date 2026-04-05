package com.portly.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.scheduling.annotation.Async;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    // Spring inyectará aquí el correo que pusiste en las variables de entorno
    @Value("${spring.mail.username:}")
    private String correoRemitente;

    @Async
    public void enviarCodigoRecuperacion(String destino, String codigo) {
        SimpleMailMessage mensaje = new SimpleMailMessage();
        
        // Usamos la variable inyectada en lugar de texto fijo
        mensaje.setFrom(correoRemitente); 
        mensaje.setTo(destino);
        mensaje.setSubject("Código de Recuperación - Portly");
        mensaje.setText("Hola,\n\nHas solicitado restablecer tu contraseña en Portly.\n"
                + "Tu código de verificación de 6 dígitos es: " + codigo + "\n\n"
                + "Este código expirará en 10 minutos.\n"
                + "Si no solicitaste este cambio, ignora este correo.");

        mailSender.send(mensaje);
    }
}