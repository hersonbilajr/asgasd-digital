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
import java.math.RoundingMode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Produto vendido na loja. */
@Entity
@Table(name = "produtos")
@Getter
@Setter
@NoArgsConstructor
public class Produto extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nome", nullable = false, length = 150)
    private String nome;

    @Column(name = "slug", nullable = false, unique = true, length = 170)
    private String slug;

    @Column(name = "descricao_curta", length = 300)
    private String descricaoCurta;

    @Column(name = "descricao", length = 2000)
    private String descricao;

    @Column(name = "preco", nullable = false, precision = 12, scale = 2)
    private BigDecimal preco = BigDecimal.ZERO;

    @Column(name = "preco_promocional", precision = 12, scale = 2)
    private BigDecimal precoPromocional;

    @Column(name = "sku", unique = true, length = 40)
    private String sku;

    @Column(name = "imagem_url", length = 500)
    private String imagemUrl;

    @Column(name = "stock", nullable = false)
    private int stock;

    @Column(name = "destaque", nullable = false)
    private boolean destaque;

    @Column(name = "ativo", nullable = false)
    private boolean ativo = true;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "categoria_id")
    private Categoria categoria;

    /** Preco efetivamente cobrado (promocional quando existe e e inferior). */
    public BigDecimal getPrecoVenda() {
        if (precoPromocional != null && precoPromocional.compareTo(BigDecimal.ZERO) > 0
                && precoPromocional.compareTo(preco) < 0) {
            return precoPromocional;
        }
        return preco;
    }

    public boolean isEmPromocao() {
        return precoPromocional != null
                && precoPromocional.compareTo(BigDecimal.ZERO) > 0
                && precoPromocional.compareTo(preco) < 0;
    }

    public int getDescontoPercentual() {
        if (!isEmPromocao() || preco.compareTo(BigDecimal.ZERO) == 0) {
            return 0;
        }
        BigDecimal desconto = preco.subtract(precoPromocional)
                .multiply(BigDecimal.valueOf(100))
                .divide(preco, 0, RoundingMode.HALF_UP);
        return desconto.intValue();
    }

    public boolean isDisponivel() {
        return ativo && stock > 0;
    }

    public String getImagemOuPadrao() {
        return (imagemUrl == null || imagemUrl.isBlank()) ? "/img/produto-placeholder.svg" : imagemUrl;
    }
}
