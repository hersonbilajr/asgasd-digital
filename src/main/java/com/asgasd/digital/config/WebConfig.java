package com.asgasd.digital.config;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@Slf4j
public class WebConfig implements WebMvcConfigurer {

    @Value("${app.upload.dir}")
    private String pastaUpload;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        Path pasta = Paths.get(pastaUpload).toAbsolutePath().normalize();
        try {
            Files.createDirectories(pasta);
        } catch (IOException ex) {
            log.warn("Nao foi possivel criar a pasta de uploads {}: {}", pasta, ex.getMessage());
        }
        registry.addResourceHandler("/media/**")
                .addResourceLocations(pasta.toUri().toString())
                .setCachePeriod(3600);
    }
}
