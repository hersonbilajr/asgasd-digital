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

/** Endereco de entrega guardado na conta do cliente. */
@Entity
@Table(name = "enderecos")
@Getter
@Setter
@NoArgsConstructor
public class Endereco extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Column(name = "provincia", length = 60)
    private String provincia;

    @Column(name = "cidade", length = 60)
    private String cidade;

    @Column(name = "bairro", length = 80)
    private String bairro;

    @Column(name = "rua", length = 160)
    private String rua;

    @Column(name = "referencia", length = 160)
    private String referencia;

    @Column(name = "codigo_postal", length = 20)
    private String codigoPostal;

    @Column(name = "principal", nullable = false)
    private boolean principal;

    /** Representacao textual usada nas encomendas e nos emails. */
    public String getResumo() {
        StringBuilder sb = new StringBuilder();
        if (rua != null && !rua.isBlank()) {
            sb.append(rua);
        }
        if (bairro != null && !bairro.isBlank()) {
            sb.append(sb.isEmpty() ? "" : ", ").append(bairro);
        }
        if (cidade != null && !cidade.isBlank()) {
            sb.append(sb.isEmpty() ? "" : ", ").append(cidade);
        }
        if (provincia != null && !provincia.isBlank()) {
            sb.append(sb.isEmpty() ? "" : ", ").append(provincia);
        }
        if (referencia != null && !referencia.isBlank()) {
            sb.append(sb.isEmpty() ? "" : " | ").append("Referencia: ").append(referencia);
        }
        return sb.toString();
    }
}
