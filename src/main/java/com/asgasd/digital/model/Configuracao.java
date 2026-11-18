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

/** Configuracao editavel no painel administrativo (contactos, redes sociais). */
@Entity
@Table(name = "configuracoes")
@Getter
@Setter
@NoArgsConstructor
public class Configuracao extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "chave", nullable = false, unique = true, length = 60)
    private String chave;

    @Column(name = "valor", length = 1000)
    private String valor;

    @Column(name = "descricao", length = 200)
    private String descricao;

    public Configuracao(String chave, String valor, String descricao) {
        this.chave = chave;
        this.valor = valor;
        this.descricao = descricao;
    }
}
