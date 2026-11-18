package com.asgasd.digital.web;

import com.asgasd.digital.model.Produto;
import com.asgasd.digital.service.CategoriaService;
import com.asgasd.digital.service.ProdutoService;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class HomeController {

    /** Testemunhos de demonstracao - nao representam clientes reais. */
    public record Testemunho(String nome, String cidade, String texto) {
    }

    private static final List<Testemunho> TESTEMUNHOS = List.of(
            new Testemunho("Ana M.", "Maputo", "Encomenda entregue no dia seguinte e equipamento bem embalado."),
            new Testemunho("Carlos T.", "Matola", "Apoio ao cliente respondeu em minutos pelo WhatsApp."),
            new Testemunho("Sara B.", "Beira", "Precos claros e acompanhamento da encomenda passo a passo."));

    private final ProdutoService produtos;
    private final CategoriaService categorias;

    @GetMapping("/")
    public String inicio(Model model) {
        List<Produto> destaques = produtos.destaques();
        List<Produto> maisVendidos = new ArrayList<>(produtos.maisVendidos(8));
        if (maisVendidos.isEmpty()) {
            maisVendidos = produtos.pesquisar(null, org.springframework.data.domain.PageRequest.of(0, 8)).getContent();
        }
        model.addAttribute("destaques", destaques);
        model.addAttribute("promocoes", produtos.promocoes());
        model.addAttribute("maisVendidos", maisVendidos);
        model.addAttribute("categoriasVitrine", categorias.listarAtivas());
        model.addAttribute("testemunhos", TESTEMUNHOS);
        return "index";
    }
}
