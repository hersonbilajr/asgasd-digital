package com.asgasd.digital.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;
import org.junit.jupiter.api.Test;

/** Testa a conversao de DATABASE_URL (postgres://) em propriedades JDBC. */
class DatabaseUrlTest {

    @Test
    void converteUriCompleta() {
        Map<String, Object> props = DatabaseUrlEnvironmentPostProcessor.normalizar(
                "postgres://asgasd:s3nh4@dpg-abc.oregon.render.com:5432/asgasd");

        assertThat(props)
                .containsEntry("spring.datasource.url", "jdbc:postgresql://dpg-abc.oregon.render.com:5432/asgasd")
                .containsEntry("spring.datasource.username", "asgasd")
                .containsEntry("spring.datasource.password", "s3nh4");
    }

    @Test
    void usaPorta5432QuandoAusenteEMantemParametros() {
        Map<String, Object> props = DatabaseUrlEnvironmentPostProcessor.normalizar(
                "postgresql://utilizador:pass@ep-fria.neon.tech/neondb?sslmode=require");

        assertThat(props)
                .containsEntry("spring.datasource.url",
                        "jdbc:postgresql://ep-fria.neon.tech:5432/neondb?sslmode=require")
                .containsEntry("spring.datasource.username", "utilizador");
    }

    @Test
    void descodificaCredenciaisComCaracteresEspeciais() {
        Map<String, Object> props = DatabaseUrlEnvironmentPostProcessor.normalizar(
                "postgres://utilizador:pa%40ss%3Aword@127.0.0.1:6543/base");

        assertThat(props).containsEntry("spring.datasource.password", "pa@ss:word");
    }

    @Test
    void ignoraUrlsJdbcEVazias() {
        assertThat(DatabaseUrlEnvironmentPostProcessor.normalizar("jdbc:postgresql://localhost:5432/asgasd")).isEmpty();
        assertThat(DatabaseUrlEnvironmentPostProcessor.normalizar("")).isEmpty();
        assertThat(DatabaseUrlEnvironmentPostProcessor.normalizar(null)).isEmpty();
    }
}
