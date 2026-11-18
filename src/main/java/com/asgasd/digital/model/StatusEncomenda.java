package com.asgasd.digital.model;

/** Estado do ciclo de vida de uma encomenda. */
public enum StatusEncomenda {
    PENDENTE("Pendente", "secondary"),
    CONFIRMADA("Confirmada", "info"),
    EM_PREPARACAO("Em preparacao", "warning"),
    ENVIADA("Enviada", "primary"),
    ENTREGUE("Entregue", "success"),
    CANCELADA("Cancelada", "danger");

    private final String descricao;
    private final String corBootstrap;

    StatusEncomenda(String descricao, String corBootstrap) {
        this.descricao = descricao;
        this.corBootstrap = corBootstrap;
    }

    public String getDescricao() {
        return descricao;
    }

    public String getCorBootstrap() {
        return corBootstrap;
    }

    public boolean podeCancelar() {
        return this == PENDENTE || this == CONFIRMADA || this == EM_PREPARACAO;
    }

    public boolean isFinal() {
        return this == ENTREGUE || this == CANCELADA;
    }
}
