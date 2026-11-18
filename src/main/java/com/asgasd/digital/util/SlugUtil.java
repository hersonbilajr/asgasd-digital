package com.asgasd.digital.util;

import java.text.Normalizer;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.UUID;

/** Utilitarios de texto usados pelos servicos. */
public final class SlugUtil {

    private SlugUtil() {
    }

    /** Converte "Rato Sem Fios" em "rato-sem-fios". */
    public static String slugificar(String texto) {
        if (texto == null) {
            return "";
        }
        String semAcentos = Normalizer.normalize(texto, Normalizer.Form.NFD)
                .replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
        return semAcentos.toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("^-+|-+$", "");
    }

    /** Referencia legivel de encomenda, ex.: ASG-20260101-7F3A9C. */
    public static String gerarReferenciaEncomenda() {
        String data = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);
        String aleatorio = UUID.randomUUID().toString().replace("-", "").substring(0, 6).toUpperCase(Locale.ROOT);
        return "ASG-" + data + "-" + aleatorio;
    }
}
