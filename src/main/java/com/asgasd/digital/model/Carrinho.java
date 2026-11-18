package com.asgasd.digital.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Carrinho de compras. Pertence a um cliente autenticado ou a um visitante (token de sessao). */
@Entity
@Table(name = "carrinhos")
@Getter
@Setter
@NoArgsConstructor
public class Carrinho extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    private Usuario usuario;

    @Column(name = "token", length = 64, unique = true)
    private String token;

    @OneToMany(mappedBy = "carrinho", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<ItemCarrinho> itens = new ArrayList<>();

    public BigDecimal getSubtotal() {
        return itens.stream()
                .map(ItemCarrinho::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public int getQuantidadeTotal() {
        return itens.stream().mapToInt(ItemCarrinho::getQuantidade).sum();
    }

    public boolean isVazio() {
        return itens.isEmpty();
    }

    public Optional<ItemCarrinho> procurarItem(Produto produto) {
        return itens.stream()
                .filter(i -> i.getProduto() != null && i.getProduto().getId().equals(produto.getId()))
                .findFirst();
    }
}
