package com.asgasd.digital.web;

import com.asgasd.digital.dto.ContactoDto;
import com.asgasd.digital.model.Usuario;
import com.asgasd.digital.service.ContactoService;
import com.asgasd.digital.service.UsuarioService;
import jakarta.validation.Valid;
import java.security.Principal;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
public class ContactoController {

    private final ContactoService contactos;
    private final UsuarioService usuarios;

    @GetMapping("/contacto")
    public String formulario(Model model, Principal principal) {
        if (!model.containsAttribute("mensagemForm")) {
            ContactoDto dto = new ContactoDto();
            if (principal != null) {
                usuarios.procurarPorEmail(principal.getName()).ifPresent(usuario -> {
                    dto.setNome(usuario.getNome());
                    dto.setEmail(usuario.getEmail());
                    dto.setTelefone(usuario.getTelefone());
                });
            }
            model.addAttribute("mensagemForm", dto);
        }
        return "contacto";
    }

    @PostMapping("/contacto")
    public String enviar(@Valid @ModelAttribute("mensagemForm") ContactoDto mensagem,
            BindingResult validacao,
            Principal principal,
            RedirectAttributes redirect) {
        if (validacao.hasErrors()) {
            return "contacto";
        }
        Usuario usuario = principal == null ? null
                : usuarios.procurarPorEmail(principal.getName()).orElse(null);
        contactos.registar(mensagem, usuario);
        redirect.addFlashAttribute("sucesso",
                "Mensagem enviada. A equipa Asgasd Digital responde normalmente em 24 horas uteis.");
        return "redirect:/contacto";
    }
}
