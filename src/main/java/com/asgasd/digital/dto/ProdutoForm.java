package com.asgasd.digital.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.Setter;

/** Formulario de criacao/edicao de produto no painel administrativo. */
@Getter
@Setter
public class ProdutoForm {

    private Long id;

    @NotBlank(message = "Indique o nome do produto.")
    @Size(max = 150)
    private String nome;

    @Size(max = 40)
    private String sku;

    @NotNull(message = "Escolha uma categoria.")
    private Long categoriaId;

    @NotNull(message = "Indique o preco.")
    @DecimalMin(value = "0.01", message = "O preco deve ser superior a zero.")
    private BigDecimal preco;

    @DecimalMin(value = "0.00", message = "O preco promocional nao pode ser negativo.")
    private BigDecimal precoPromocional;

    @Min(value = 0, message = "O stock nao pode ser negativo.")
    private int stock;

    @Size(max = 300)
    private String descricaoCurta;

    @Size(max = 2000)
    private String descricao;

    private String imagemUrl;

    private boolean destaque;

    private boolean ativo = true;
}
