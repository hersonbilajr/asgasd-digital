package com.asgasd.digital.web.admin;

import com.asgasd.digital.exception.RegraNegocioException;
import com.asgasd.digital.model.StatusEncomenda;
import com.asgasd.digital.service.EncomendaService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/encomendas")
@RequiredArgsConstructor
public class AdminEncomendaController {

    private final EncomendaService encomendas;

    @GetMapping
    public String listar(@RequestParam(required = false) StatusEncomenda status,
            @RequestParam(defaultValue = "0") int pagina,
            Model model) {
        Page<?> resultado = encomendas.listar(status, PageRequest.of(Math.max(pagina, 0), 15));
        model.addAttribute("pagina", resultado);
        model.addAttribute("statusSelecionado", status);
        model.addAttribute("estados", StatusEncomenda.values());
        model.addAttribute("paginaAtual", resultado.getNumber());
        model.addAttribute("totalPaginas", resultado.getTotalPages());
        return "admin/encomendas/lista";
    }

    @GetMapping("/{id}")
    public String detalhe(@PathVariable Long id, Model model) {
        model.addAttribute("encomenda", encomendas.obter(id));
        model.addAttribute("estados", StatusEncomenda.values());
        return "admin/encomendas/detalhe";
    }

    @PostMapping("/{id}/status")
    public String atualizarStatus(@PathVariable Long id,
            @RequestParam StatusEncomenda status,
            RedirectAttributes redirect) {
        try {
            encomendas.atualizarStatus(id, status);
            redirect.addFlashAttribute("sucesso", "Estado da encomenda atualizado para " + status.getDescricao() + ".");
        } catch (RegraNegocioException ex) {
            redirect.addFlashAttribute("erro", ex.getMessage());
        }
        return "redirect:/admin/encomendas/" + id;
    }

    @PostMapping("/{id}/pagamento/confirmar")
    public String confirmarPagamento(@PathVariable Long id, RedirectAttributes redirect) {
        try {
            encomendas.confirmarPagamento(id);
            redirect.addFlashAttribute("sucesso", "Pagamento confirmado.");
        } catch (RegraNegocioException ex) {
            redirect.addFlashAttribute("erro", ex.getMessage());
        }
        return "redirect:/admin/encomendas/" + id;
    }

    @PostMapping("/{id}/pagamento/rejeitar")
    public String rejeitarPagamento(@PathVariable Long id,
            @RequestParam(required = false) String motivo,
            RedirectAttributes redirect) {
        try {
            encomendas.rejeitarPagamento(id, motivo);
            redirect.addFlashAttribute("sucesso", "Pagamento marcado como rejeitado.");
        } catch (RegraNegocioException ex) {
            redirect.addFlashAttribute("erro", ex.getMessage());
        }
        return "redirect:/admin/encomendas/" + id;
    }
}
