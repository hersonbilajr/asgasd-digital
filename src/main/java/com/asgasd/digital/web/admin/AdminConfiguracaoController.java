package com.asgasd.digital.web.admin;

import com.asgasd.digital.exception.RegraNegocioException;
import com.asgasd.digital.model.MetodoPagamento;
import com.asgasd.digital.service.ConfiguracaoService;
import com.asgasd.digital.service.MetodoPagamentoService;
import java.util.LinkedHashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/configuracoes")
@RequiredArgsConstructor
public class AdminConfiguracaoController {

    private final ConfiguracaoService configuracoes;
    private final MetodoPagamentoService metodos;

    @GetMapping
    public String pagina(Model model) {
        model.addAttribute("configAdmin", configuracoes.todas());
        if (!model.containsAttribute("metodoPagamento")) {
            model.addAttribute("metodoPagamento", new MetodoPagamento());
        }
        model.addAttribute("metodosAdmin", metodos.listarTodos());
        return "admin/configuracoes";
    }

    @PostMapping("/contactos")
    public String guardarContactos(@RequestParam Map<String, String> parametros, RedirectAttributes redirect) {
        Map<String, String> valores = new LinkedHashMap<>();
        parametros.forEach((chave, valor) -> {
            if (chave.startsWith("cfg.")) {
                valores.put(chave, valor);
            }
        });
        configuracoes.guardar(valores);
        redirect.addFlashAttribute("sucesso", "Dados de contacto atualizados.");
        return "redirect:/admin/configuracoes";
    }

    @PostMapping("/pagamentos")
    public String guardarMetodo(@ModelAttribute MetodoPagamento metodo, RedirectAttributes redirect) {
        try {
            metodos.guardar(metodo);
            redirect.addFlashAttribute("sucesso", "Metodo de pagamento guardado.");
        } catch (RegraNegocioException ex) {
            redirect.addFlashAttribute("erro", ex.getMessage());
        }
        return "redirect:/admin/configuracoes";
    }

    @PostMapping("/pagamentos/{id}/ativar")
    public String alternarMetodo(@PathVariable Long id, RedirectAttributes redirect) {
        metodos.alternarAtivo(id);
        redirect.addFlashAttribute("sucesso", "Disponibilidade do metodo atualizada.");
        return "redirect:/admin/configuracoes";
    }
}
