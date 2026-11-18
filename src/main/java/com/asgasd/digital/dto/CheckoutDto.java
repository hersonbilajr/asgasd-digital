package com.asgasd.digital.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/** Dados recolhidos no checkout. */
@Getter
@Setter
public class CheckoutDto {

    @NotBlank(message = "Indique o nome de quem recebe a encomenda.")
    @Size(max = 120)
    private String nomeContacto;

    @NotBlank(message = "Indique um telefone de contacto.")
    @Pattern(regexp = "^[0-9+()\\s-]{7,20}$", message = "Introduza um telefone valido.")
    private String telefoneContacto;

    @NotBlank(message = "Indique um email de contacto.")
    @Email(message = "Introduza um email valido.")
    @Size(max = 160)
    private String emailContacto;

    @NotBlank(message = "Indique a provincia de entrega.")
    @Size(max = 60)
    private String provincia;

    @NotBlank(message = "Indique a cidade de entrega.")
    @Size(max = 60)
    private String cidade;

    @NotBlank(message = "Indique o bairro de entrega.")
    @Size(max = 80)
    private String bairro;

    @NotBlank(message = "Indique a rua/avenida e o numero da casa.")
    @Size(max = 160)
    private String rua;

    @Size(max = 160)
    private String referencia;

    @NotNull(message = "Escolha um metodo de pagamento.")
    private Long metodoPagamentoId;

    @Size(max = 500, message = "As notas nao podem exceder 500 caracteres.")
    private String notas;

    private boolean guardarEndereco = true;

    public String enderecoFormatado() {
        StringBuilder sb = new StringBuilder(rua.trim());
        sb.append(", ").append(bairro.trim());
        sb.append(", ").append(cidade.trim());
        sb.append(", ").append(provincia.trim());
        if (referencia != null && !referencia.isBlank()) {
            sb.append(" | Referencia: ").append(referencia.trim());
        }
        return sb.toString();
    }
}
