package com.asgasd.digital.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Metodo de pagamento configuravel pelo administrador.
 *
 * <p>Nao existe integracao com um fornecedor de pagamentos: cada metodo descreve
 * instrucoes de pagamento manual (transferencia, deposito, pagamento na entrega)
 * e o administrador confirma o pagamento no painel.</p>
 */
@Entity
@Table(name = "metodos_pagamento")
@Getter
@Setter
@NoArgsConstructor
public class MetodoPagamento extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nome", nullable = false, unique = true, length = 80)
    private String nome;

    @Column(name = "descricao", length = 255)
    private String descricao;

    @Column(name = "instrucoes", length = 1200)
    private String instrucoes;

    @Column(name = "ativo", nullable = false)
    private boolean ativo = true;

    @Column(name = "ordem", nullable = false)
    private int ordem;
}
