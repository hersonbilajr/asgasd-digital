package com.asgasd.digital.model;

/** Estado do pagamento de uma encomenda (confirmacao manual pelo administrador). */
public enum StatusPagamento {
    PENDENTE("Pendente", "secondary"),
    CONFIRMADO("Confirmado", "success"),
    REJEITADO("Rejeitado", "danger"),
    REEMBOLSADO("Reembolsado", "warning");

    private final String descricao;
    private final String corBootstrap;

    StatusPagamento(String descricao, String corBootstrap) {
        this.descricao = descricao;
        this.corBootstrap = corBootstrap;
    }

    public String getDescricao() {
        return descricao;
    }

    public String getCorBootstrap() {
        return corBootstrap;
    }
}
