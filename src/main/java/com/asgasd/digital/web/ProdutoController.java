package com.asgasd.digital.web;

import com.asgasd.digital.dto.ProdutoFiltro;
import com.asgasd.digital.model.Produto;
import com.asgasd.digital.service.CategoriaService;
import com.asgasd.digital.service.ProdutoService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
public class ProdutoController {

    private static final int PRODUTOS_POR_PAGINA = 9;

    private final ProdutoService produtos;
    private final CategoriaService categorias;

    @GetMapping("/produtos")
    public String catalogo(@ModelAttribute("filtro") ProdutoFiltro filtro,
            @RequestParam(name = "pagina", defaultValue = "0") int pagina,
            Model model) {
        Page<Produto> resultado = produtos.pesquisar(filtro,
                PageRequest.of(Math.max(pagina, 0), PRODUTOS_POR_PAGINA));
        model.addAttribute("pagina", resultado);
        model.addAttribute("paginaAtual", resultado.getNumber());
        model.addAttribute("totalPaginas", resultado.getTotalPages());
        model.addAttribute("totalProdutos", resultado.getTotalElements());
        model.addAttribute("categoriasFiltro", categorias.listarAtivas());
        return "produtos/lista";
    }

    @GetMapping("/produto/{slug}")
    public String detalhe(@PathVariable String slug, Model model) {
        Produto produto = produtos.porSlug(slug);
        model.addAttribute("produto", produto);
        model.addAttribute("relacionados", produtos.relacionados(produto, 4));
        return "produtos/detalhe";
    }
}
