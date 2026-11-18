package com.asgasd.digital.model;

import jakarta.persistence.CascadeType;
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
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Encomenda registada na base de dados. */
@Entity
@Table(name = "encomendas")
@Getter
@Setter
@NoArgsConstructor
public class Encomenda extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Numero de referencia mostrado ao cliente (ex.: ASG-20260101-4F7A2C). */
    @Column(name = "referencia", nullable = false, unique = true, length = 30)
    private String referencia;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private StatusEncomenda status = StatusEncomenda.PENDENTE;

    @Column(name = "subtotal", nullable = false, precision = 12, scale = 2)
    private BigDecimal subtotal = BigDecimal.ZERO;

    @Column(name = "custo_envio", nullable = false, precision = 12, scale = 2)
    private BigDecimal custoEnvio = BigDecimal.ZERO;

    @Column(name = "total", nullable = false, precision = 12, scale = 2)
    private BigDecimal total = BigDecimal.ZERO;

    @Column(name = "nome_contacto", nullable = false, length = 120)
    private String nomeContacto;

    @Column(name = "telefone_contacto", nullable = false, length = 30)
    private String telefoneContacto;

    @Column(name = "email_contacto", nullable = false, length = 160)
    private String emailContacto;

    @Column(name = "endereco_entrega", nullable = false, length = 600)
    private String enderecoEntrega;

    @Column(name = "notas", length = 500)
    private String notas;

    @OneToMany(mappedBy = "encomenda", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ItemEncomenda> itens = new ArrayList<>();

    @OneToOne(mappedBy = "encomenda", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private Pagamento pagamento;

    public void adicionarItem(ItemEncomenda item) {
        item.setEncomenda(this);
        this.itens.add(item);
    }

    public void definirPagamento(Pagamento pagamento) {
        if (pagamento != null) {
            pagamento.setEncomenda(this);
        }
        this.pagamento = pagamento;
    }

    public int getQuantidadeTotal() {
        return itens.stream().mapToInt(ItemEncomenda::getQuantidade).sum();
    }

    public boolean isCancelavel() {
        return status.podeCancelar();
    }
}
