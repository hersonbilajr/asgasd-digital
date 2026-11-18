package com.asgasd.digital.web;

import com.asgasd.digital.dto.RegistoDto;
import com.asgasd.digital.dto.SenhaDto;
import com.asgasd.digital.exception.RegraNegocioException;
import com.asgasd.digital.service.EmailService;
import com.asgasd.digital.service.UsuarioService;
import jakarta.validation.Valid;
import java.security.Principal;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UsuarioService usuarios;
    private final EmailService emailService;

    @Value("${app.seed.demo:false}")
    private boolean ambienteDemo;

    @GetMapping("/login")
    public String login(@RequestParam(required = false) String erro,
            @RequestParam(required = false) String expirada,
            @RequestParam(required = false) String registado,
            @RequestParam(required = false) String redefinida,
            Principal principal) {
        if (principal != null) {
            return "redirect:/conta";
        }
        return "auth/login";
    }

    @GetMapping("/registo")
    public String registoForm(Model model, Principal principal) {
        if (principal != null) {
            return "redirect:/conta";
        }
        if (!model.containsAttribute("registo")) {
            model.addAttribute("registo", new RegistoDto());
        }
        return "auth/registo";
    }

    @PostMapping("/registo")
    public String registar(@Valid @ModelAttribute("registo") RegistoDto registo,
            BindingResult validacao,
            RedirectAttributes redirect) {
        if (!registo.isSenhasIguais()) {
            validacao.rejectValue("confirmarSenha", "senhas.diferentes", "As palavras-passe nao coincidem.");
        }
        if (validacao.hasErrors()) {
            return "auth/registo";
        }
        try {
            usuarios.registar(registo);
            redirect.addFlashAttribute("sucesso", "Conta criada com sucesso. Inicie sessao para continuar.");
            return "redirect:/auth/login?registado";
        } catch (RegraNegocioException ex) {
            validacao.rejectValue("email", "email.em.uso", ex.getMessage());
            return "auth/registo";
        }
    }

    @GetMapping("/recuperar")
    public String recuperarForm() {
        return "auth/recuperar";
    }

    @PostMapping("/recuperar")
    public String recuperar(@RequestParam(name = "email", required = false) String email,
            Model model,
            RedirectAttributes redirect) {
        if (email == null || email.isBlank()) {
            model.addAttribute("erro", "Indique o email associado a sua conta.");
            return "auth/recuperar";
        }
        Optional<String> link = usuarios.solicitarRecuperacao(email);
        model.addAttribute("sucesso",
                "Se existir uma conta com esse email, enviámos as instrucoes de recuperacao. "
                        + "Verifique a caixa de entrada e o spam.");
        if (link.isPresent() && ambienteDemo && emailService != null) {
            model.addAttribute("linkRecuperacao", link.get());
        }
        return "auth/recuperar";
    }

    @GetMapping("/redefinir")
    public String redefinirForm(@RequestParam(name = "token", required = false) String token, Model model) {
        if (token == null || !usuarios.tokenValido(token)) {
            model.addAttribute("invalido", true);
            return "auth/redefinir";
        }
        SenhaDto form = new SenhaDto();
        form.setToken(token);
        model.addAttribute("senhaForm", form);
        return "auth/redefinir";
    }

    @PostMapping("/redefinir")
    public String redefinir(@Valid @ModelAttribute("senhaForm") SenhaDto senha,
            BindingResult validacao,
            RedirectAttributes redirect) {
        if (validacao.hasErrors()) {
            return "auth/redefinir";
        }
        try {
            usuarios.redefinirSenha(senha);
            redirect.addFlashAttribute("sucesso", "Palavra-passe redefinida. Ja pode iniciar sessao.");
            return "redirect:/auth/login?redefinida";
        } catch (RegraNegocioException ex) {
            validacao.reject("erro", ex.getMessage());
            return "auth/redefinir";
        }
    }
}
