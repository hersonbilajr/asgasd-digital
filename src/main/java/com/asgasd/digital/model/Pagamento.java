package com.asgasd.digital.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Registo do pagamento associado a uma encomenda. */
@Entity
@Table(name = "pagamentos")
@Getter
@Setter
@NoArgsConstructor
public class Pagamento extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "encomenda_id", nullable = false, unique = true)
    private Encomenda encomenda;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "metodo_pagamento_id")
    private MetodoPagamento metodoPagamento;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private StatusPagamento status = StatusPagamento.PENDENTE;

    @Column(name = "valor", nullable = false, precision = 12, scale = 2)
    private BigDecimal valor;

    /** Referencia do comprovativo fornecida pelo cliente/administrador. */
    @Column(name = "referencia_externa", length = 120)
    private String referenciaExterna;

    @Column(name = "instrucoes", length = 1200)
    private String instrucoes;

    @Column(name = "confirmado_em")
    private LocalDateTime confirmadoEm;

    public boolean isPendente() {
        return status == StatusPagamento.PENDENTE;
    }
}
