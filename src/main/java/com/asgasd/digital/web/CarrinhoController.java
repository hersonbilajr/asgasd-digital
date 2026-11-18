package com.asgasd.digital.web;

import com.asgasd.digital.exception.RecursoNaoEncontradoException;
import com.asgasd.digital.exception.RegraNegocioException;
import com.asgasd.digital.service.CarrinhoService;
import com.asgasd.digital.service.EncomendaService;
import jakarta.servlet.http.HttpSession;
import java.math.BigDecimal;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/carrinho")
@RequiredArgsConstructor
public class CarrinhoController {

    private final CarrinhoService carrinhoService;
    private final EncomendaService encomendas;

    @GetMapping
    public String ver(HttpSession sessao, Model model) {
        var carrinho = carrinhoService.obterCarrinho(sessao);
        BigDecimal custoEnvio = encomendas.calcularCustoEnvio(carrinho.getSubtotal());
        model.addAttribute("carrinho", carrinho);
        model.addAttribute("custoEnvio", custoEnvio);
        model.addAttribute("total", carrinho.getSubtotal().add(custoEnvio));
        return "carrinho";
    }

    @PostMapping("/adicionar")
    public String adicionar(@RequestParam Long produtoId,
            @RequestParam(defaultValue = "1") int quantidade,
            @RequestParam(required = false) String origem,
            HttpSession sessao,
            RedirectAttributes redirect) {
        try {
            carrinhoService.adicionar(sessao, produtoId, quantidade);
            redirect.addFlashAttribute("sucesso", "Produto adicionado ao carrinho.");
        } catch (RegraNegocioException | RecursoNaoEncontradoException ex) {
            redirect.addFlashAttribute("erro", ex.getMessage());
        }
        return "redirect:" + destinoSeguro(origem);
    }

    @PostMapping("/atualizar")
    public String atualizar(@RequestParam Long itemId,
            @RequestParam int quantidade,
            HttpSession sessao,
            RedirectAttributes redirect) {
        try {
            carrinhoService.atualizarQuantidade(sessao, itemId, quantidade);
            redirect.addFlashAttribute("sucesso", "Quantidade atualizada.");
        } catch (RegraNegocioException | RecursoNaoEncontradoException ex) {
            redirect.addFlashAttribute("erro", ex.getMessage());
        }
        return "redirect:/carrinho";
    }

    @PostMapping("/remover")
    public String remover(@RequestParam Long itemId, HttpSession sessao, RedirectAttributes redirect) {
        try {
            carrinhoService.remover(sessao, itemId);
            redirect.addFlashAttribute("sucesso", "Artigo removido do carrinho.");
        } catch (RecursoNaoEncontradoException ex) {
            redirect.addFlashAttribute("erro", ex.getMessage());
        }
        return "redirect:/carrinho";
    }

    @PostMapping("/limpar")
    public String limpar(HttpSession sessao, RedirectAttributes redirect) {
        carrinhoService.limpar(sessao);
        redirect.addFlashAttribute("sucesso", "O carrinho foi esvaziado.");
        return "redirect:/carrinho";
    }

    private String destinoSeguro(String origem) {
        if (origem != null && origem.startsWith("/") && !origem.startsWith("//")) {
            return origem;
        }
        return "/carrinho";
    }
}
