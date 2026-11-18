package com.asgasd.digital.service;

import com.asgasd.digital.exception.RegraNegocioException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

/** Guarda as fotografias carregadas pelo painel administrativo. */
@Service
@Slf4j
public class ImagemService {

    private static final List<String> TIPOS_PERMITIDOS =
            List.of("image/jpeg", "image/png", "image/webp", "image/gif");

    @Value("${app.upload.dir}")
    private String pastaUpload;

    /**
     * Guarda o ficheiro e devolve o caminho publico (/media/...).
     *
     * @return null quando nao foi enviado ficheiro.
     */
    public String guardar(MultipartFile ficheiro) {
        if (ficheiro == null || ficheiro.isEmpty()) {
            return null;
        }
        String tipo = ficheiro.getContentType() == null ? "" : ficheiro.getContentType().toLowerCase(Locale.ROOT);
        if (!TIPOS_PERMITIDOS.contains(tipo)) {
            throw new RegraNegocioException("Formato de imagem nao suportado. Use JPG, PNG, WEBP ou GIF.");
        }
        String extensao = switch (tipo) {
            case "image/png" -> ".png";
            case "image/webp" -> ".webp";
            case "image/gif" -> ".gif";
            default -> ".jpg";
        };
        try {
            Path destino = Paths.get(pastaUpload).toAbsolutePath().normalize();
            Files.createDirectories(destino);
            String nome = UUID.randomUUID().toString().replace("-", "") + extensao;
            try (InputStream entrada = ficheiro.getInputStream()) {
                Files.copy(entrada, destino.resolve(nome), StandardCopyOption.REPLACE_EXISTING);
            }
            log.info("Imagem guardada em {}", destino.resolve(nome));
            return "/media/" + nome;
        } catch (IOException ex) {
            log.error("Falha ao guardar imagem", ex);
            throw new RegraNegocioException("Nao foi possivel guardar a imagem enviada.");
        }
    }
}
