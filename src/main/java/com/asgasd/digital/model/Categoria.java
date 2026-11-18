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

/** Categoria usada para organizar o catalogo. */
@Entity
@Table(name = "categorias")
@Getter
@Setter
@NoArgsConstructor
public class Categoria extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nome", nullable = false, unique = true, length = 80)
    private String nome;

    @Column(name = "slug", nullable = false, unique = true, length = 90)
    private String slug;

    @Column(name = "descricao", length = 255)
    private String descricao;

    @Column(name = "ativo", nullable = false)
    private boolean ativo = true;
}
