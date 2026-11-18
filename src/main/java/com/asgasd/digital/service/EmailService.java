package com.asgasd.digital.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

/**
 * Envio de email transacional.
 *
 * <p>Quando nao existe servidor SMTP configurado (variaveis SPRING_MAIL_* ausentes),
 * a aplicacao nao finge que enviou: regista a mensagem no log do servidor para que o
 * administrador possa entregar o link manualmente.</p>
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final ObjectProvider<JavaMailSender> mailSenderProvider;

    @Value("${spring.mail.username:${MAIL_FROM:nao-responder@asgasd.co.mz}}")
    private String remetente;

    public boolean enviar(String destino, String assunto, String corpo) {
        JavaMailSender sender = mailSenderProvider.getIfAvailable();
        if (sender == null) {
            log.warn("""
                    SMTP nao configurado. Email nao enviado.
                    Para: {}
                    Assunto: {}
                    Conteudo:
                    {}""", destino, assunto, corpo);
            return false;
        }
        try {
            SimpleMailMessage mensagem = new SimpleMailMessage();
            mensagem.setFrom(remetente);
            mensagem.setTo(destino);
            mensagem.setSubject(assunto);
            mensagem.setText(corpo);
            sender.send(mensagem);
            log.info("Email enviado para {}", destino);
            return true;
        } catch (MailException ex) {
            log.error("Falha ao enviar email para {}: {}", destino, ex.getMessage());
            log.warn("Conteudo do email nao enviado:\n{}", corpo);
            return false;
        }
    }
}
