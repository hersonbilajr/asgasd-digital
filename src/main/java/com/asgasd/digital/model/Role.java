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

/** Perfil de acesso atribuido a um utilizador (ROLE_ADMIN, ROLE_CLIENTE). */
@Entity
@Table(name = "roles")
@Getter
@Setter
@NoArgsConstructor
public class Role {

    public static final String ADMIN = "ROLE_ADMIN";
    public static final String CLIENTE = "ROLE_CLIENTE";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nome", nullable = false, unique = true, length = 40)
    private String nome;

    public Role(String nome) {
        this.nome = nome;
    }
}
