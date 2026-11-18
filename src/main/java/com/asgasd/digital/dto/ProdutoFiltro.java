package com.asgasd.digital.dto;

import java.math.BigDecimal;
import lombok.Getter;
import lombok.Setter;

/** Parametros de pesquisa e filtragem do catalogo. */
@Getter
@Setter
public class ProdutoFiltro {

    public static final String RELEVANCIA = "relevancia";
    public static final String PRECO_ASC = "preco-asc";
    public static final String PRECO_DESC = "preco-desc";
    public static final String NOME = "nome";
    public static final String NOVIDADE = "novidade";

    private String termo;
    private Long categoriaId;
    private BigDecimal precoMin;
    private BigDecimal precoMax;
    private String ordenar = RELEVANCIA;
    private boolean apenasDisponiveis;

    public boolean temFiltros() {
        return (termo != null && !termo.isBlank())
                || categoriaId != null
                || precoMin != null
                || precoMax != null
                || apenasDisponiveis;
    }
}
