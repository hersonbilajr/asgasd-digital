package com.asgasd.digital.web.admin;

import com.asgasd.digital.service.ContactoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/mensagens")
@RequiredArgsConstructor
public class AdminMensagemController {

    private final ContactoService contactos;

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("mensagens", contactos.listar());
        return "admin/mensagens";
    }

    @PostMapping("/{id}/lida")
    public String alternarLida(@PathVariable Long id, RedirectAttributes redirect) {
        contactos.alternarLida(id);
        redirect.addFlashAttribute("sucesso", "Estado da mensagem atualizado.");
        return "redirect:/admin/mensagens";
    }
}
