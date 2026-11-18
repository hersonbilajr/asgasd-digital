package com.asgasd.digital.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegistoDto {

    @NotBlank(message = "Indique o seu nome completo.")
    @Size(min = 3, max = 120, message = "O nome deve ter entre 3 e 120 caracteres.")
    private String nome;

    @NotBlank(message = "Indique o seu email.")
    @Email(message = "Introduza um email valido.")
    @Size(max = 160, message = "O email e demasiado longo.")
    private String email;

    @Pattern(regexp = "^$|^[0-9+()\\s-]{7,20}$", message = "Introduza um telefone valido.")
    private String telefone;

    @NotBlank(message = "Escolha uma palavra-passe.")
    @Size(min = 8, max = 60, message = "A palavra-passe deve ter pelo menos 8 caracteres.")
    private String senha;

    @NotBlank(message = "Repita a palavra-passe.")
    private String confirmarSenha;

    public boolean isSenhasIguais() {
        return senha != null && senha.equals(confirmarSenha);
    }
}
