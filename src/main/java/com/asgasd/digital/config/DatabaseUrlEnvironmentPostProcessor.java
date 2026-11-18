package com.asgasd.digital.config;

import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

/**
 * Aceita `DATABASE_URL` no formato URI usado por fornecedores como Render, Railway ou Heroku
 * (`postgres://utilizador:password@host:porta/base`) e traduz para os parametros JDBC do Spring.
 * Valores que ja comecem por `jdbc:` sao deixados intactos.
 */
public class DatabaseUrlEnvironmentPostProcessor implements EnvironmentPostProcessor, Ordered {

    static final String PROPRIEDADE = "DATABASE_URL";

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment ambiente, SpringApplication aplicacao) {
        String url = ambiente.getProperty(PROPRIEDADE);
        Map<String, Object> propriedades = normalizar(url);
        if (!propriedades.isEmpty()) {
            ambiente.getPropertySources().addFirst(new MapPropertySource("databaseUrlNormalizada", propriedades));
        }
    }

    /** Converte uma URI `postgres://` nos valores esperados por spring.datasource. */
    static Map<String, Object> normalizar(String url) {
        Map<String, Object> resultado = new HashMap<>();
        if (url == null || url.isBlank()) {
            return resultado;
        }
        String limpo = url.trim();
        if (!limpo.startsWith("postgres://") && !limpo.startsWith("postgresql://")) {
            return resultado;
        }

        URI uri = URI.create(limpo);

        String utilizador = null;
        String password = null;
        String informacao = uri.getUserInfo();
        if (informacao != null && !informacao.isBlank()) {
            int separador = informacao.indexOf(':');
            if (separador >= 0) {
                utilizador = descodificar(informacao.substring(0, separador));
                password = descodificar(informacao.substring(separador + 1));
            } else {
                utilizador = descodificar(informacao);
            }
        }

        int porta = uri.getPort() > 0 ? uri.getPort() : 5432;
        String base = (uri.getPath() == null || uri.getPath().isBlank()) ? "" : uri.getPath();
        String parametros = uri.getQuery();

        StringBuilder jdbc = new StringBuilder("jdbc:postgresql://")
                .append(uri.getHost())
                .append(':')
                .append(porta)
                .append(base);
        if (parametros != null && !parametros.isBlank()) {
            jdbc.append('?').append(parametros);
        }

        resultado.put("spring.datasource.url", jdbc.toString());
        if (utilizador != null) {
            resultado.put("spring.datasource.username", utilizador);
        }
        if (password != null) {
            resultado.put("spring.datasource.password", password);
        }
        return resultado;
    }

    private static String descodificar(String valor) {
        return URLDecoder.decode(valor, StandardCharsets.UTF_8);
    }

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }
}
