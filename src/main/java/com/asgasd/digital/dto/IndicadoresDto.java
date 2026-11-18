package com.asgasd.digital.dto;

import com.asgasd.digital.model.Encomenda;
import com.asgasd.digital.model.Produto;
import com.asgasd.digital.model.StatusEncomenda;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/** Indicadores calculados a partir das encomendas reais registadas na base de dados. */
public record IndicadoresDto(
        BigDecimal totalFaturado,
        BigDecimal totalEntregue,
        BigDecimal ticketMedio,
        long totalEncomendas,
        long totalClientes,
        long totalProdutos,
        long semStock,
        long mensagensNaoLidas,
        Map<StatusEncomenda, Long> porStatus,
        List<Encomenda> encomendasRecentes,
        List<Produto> maisVendidos,
        List<Produto> stockBaixo) {
}
