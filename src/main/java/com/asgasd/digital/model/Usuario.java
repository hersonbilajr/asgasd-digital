package com.asgasd.digital.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Conta de cliente ou de administrador da loja. */
@Entity
@Table(name = "usuarios")
@Getter
@Setter
@NoArgsConstructor
public class Usuario extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nome", nullable = false, length = 120)
    private String nome;

    @Column(name = "email", nullable = false, unique = true, length = 160)
    private String email;

    /** Hash BCrypt da palavra-passe. Nunca guardamos a palavra-passe em texto simples. */
    @Column(name = "senha", nullable = false, length = 100)
    private String senha;

    @Column(name = "telefone", length = 30)
    private String telefone;

    @Column(name = "ativo", nullable = false)
    private boolean ativo = true;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "usuario_roles",
            joinColumns = @JoinColumn(name = "usuario_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id"))
    private Set<Role> roles = new HashSet<>();

    @OneToMany(mappedBy = "usuario", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Endereco> enderecos = new ArrayList<>();

    @OneToOne(mappedBy = "usuario", fetch = FetchType.LAZY)
    private Carrinho carrinho;

    @OneToMany(mappedBy = "usuario", fetch = FetchType.LAZY)
    private List<Encomenda> encomendas = new ArrayList<>();

    public void adicionarRole(Role role) {
        this.roles.add(role);
    }

    public boolean isAdministrador() {
        return roles.stream().anyMatch(r -> Role.ADMIN.equals(r.getNome()));
    }

    public String getPrimeiroNome() {
        if (nome == null || nome.isBlank()) {
            return "Cliente";
        }
        return nome.trim().split("\\s+")[0];
    }

    public String getIniciais() {
        if (nome == null || nome.isBlank()) {
            return "AD";
        }
        String[] partes = nome.trim().split("\\s+");
        if (partes.length == 1) {
            return partes[0].substring(0, Math.min(2, partes[0].length())).toUpperCase();
        }
        return (partes[0].charAt(0) + "" + partes[partes.length - 1].charAt(0)).toUpperCase();
    }

    public Endereco getEnderecoPrincipal() {
        return enderecos.stream()
                .filter(Endereco::isPrincipal)
                .findFirst()
                .orElse(enderecos.isEmpty() ? null : enderecos.get(0));
    }
}
