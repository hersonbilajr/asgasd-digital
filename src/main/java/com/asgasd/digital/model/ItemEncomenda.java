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
import java.math.BigDecimal;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Linha de uma encomenda. Guarda o nome e o preco praticados no momento da compra. */
@Entity
@Table(name = "itens_encomenda")
@Getter
@Setter
@NoArgsConstructor
public class ItemEncomenda extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "encomenda_id", nullable = false)
    private Encomenda encomenda;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "produto_id")
    private Produto produto;

    @Column(name = "nome_produto", nullable = false, length = 150)
    private String nomeProduto;

    @Column(name = "preco_unitario", nullable = false, precision = 12, scale = 2)
    private BigDecimal precoUnitario;

    @Column(name = "quantidade", nullable = false)
    private int quantidade;

    @Column(name = "subtotal", nullable = false, precision = 12, scale = 2)
    private BigDecimal subtotal;

    public static ItemEncomenda de(Produto produto, int quantidade) {
        ItemEncomenda item = new ItemEncomenda();
        item.setProduto(produto);
        item.setNomeProduto(produto.getNome());
        item.setPrecoUnitario(produto.getPrecoVenda());
        item.setQuantidade(quantidade);
        item.setSubtotal(produto.getPrecoVenda().multiply(BigDecimal.valueOf(quantidade)));
        return item;
    }
}
