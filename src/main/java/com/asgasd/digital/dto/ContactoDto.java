package com.asgasd.digital.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ContactoDto {

    @NotBlank(message = "Indique o seu nome.")
    @Size(max = 120)
    private String nome;

    @NotBlank(message = "Indique o seu email.")
    @Email(message = "Introduza um email valido.")
    @Size(max = 160)
    private String email;

    @Pattern(regexp = "^$|^[0-9+()\\s-]{7,20}$", message = "Introduza um telefone valido.")
    private String telefone;

    @NotBlank(message = "Indique o assunto.")
    @Size(max = 150)
    private String assunto;

    @NotBlank(message = "Escreva a sua mensagem.")
    @Size(min = 10, max = 2000, message = "A mensagem deve ter entre 10 e 2000 caracteres.")
    private String mensagem;
}
