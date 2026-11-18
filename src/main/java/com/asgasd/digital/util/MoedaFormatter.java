package com.asgasd.digital.util;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;
import org.springframework.stereotype.Component;

/** Disponibiliza ${@moeda.mt(valor)} nas templates Thymeleaf. */
@Component("moeda")
public class MoedaFormatter {

    private static final String SUFIXO = " MT";

    public String mt(BigDecimal valor) {
        return formatar(valor, SUFIXO);
    }

    public String numero(BigDecimal valor) {
        return formatar(valor, "");
    }

    private String formatar(BigDecimal valor, String sufixo) {
        BigDecimal v = valor == null ? BigDecimal.ZERO : valor;
        DecimalFormatSymbols simbolos = new DecimalFormatSymbols(new Locale("pt", "MZ"));
        simbolos.setGroupingSeparator(' ');
        simbolos.setDecimalSeparator(',');
        DecimalFormat formato = new DecimalFormat("#,##0.00", simbolos);
        return formato.format(v) + sufixo;
    }
}
