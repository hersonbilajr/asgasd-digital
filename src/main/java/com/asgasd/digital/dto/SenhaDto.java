package com.asgasd.digital.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SenhaDto {

    /** Preenchido apenas quando o cliente altera a palavra-passe estando autenticado. */
    private String senhaAtual;

    @NotBlank(message = "Indique a nova palavra-passe.")
    @Size(min = 8, max = 60, message = "A palavra-passe deve ter pelo menos 8 caracteres.")
    private String novaSenha;

    @NotBlank(message = "Repita a nova palavra-passe.")
    private String confirmarSenha;

    private String token;

    public boolean isIguais() {
        return novaSenha != null && novaSenha.equals(confirmarSenha);
    }
}
