package com.asgasd.digital.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Mensagem enviada pelo formulario de contacto. */
@Entity
@Table(name = "contactos")
@Getter
@Setter
@NoArgsConstructor
public class Contacto extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nome", nullable = false, length = 120)
    private String nome;

    @Column(name = "email", nullable = false, length = 160)
    private String email;

    @Column(name = "telefone", length = 30)
    private String telefone;

    @Column(name = "assunto", nullable = false, length = 150)
    private String assunto;

    @Column(name = "mensagem", nullable = false, length = 2000)
    private String mensagem;

    @Column(name = "lida", nullable = false)
    private boolean lida;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;
}
