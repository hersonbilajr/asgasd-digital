package com.asgasd.digital.web.admin;

import com.asgasd.digital.dto.ProdutoForm;
import com.asgasd.digital.exception.RegraNegocioException;
import com.asgasd.digital.model.Produto;
import com.asgasd.digital.service.CategoriaService;
import com.asgasd.digital.service.ImagemService;
import com.asgasd.digital.service.ProdutoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/produtos")
@RequiredArgsConstructor
public class AdminProdutoController {

    private final ProdutoService produtos;
    private final CategoriaService categorias;
    private final ImagemService imagens;

    @GetMapping
    public String listar(@RequestParam(required = false) String termo,
            @RequestParam(defaultValue = "0") int pagina,
            Model model) {
        Page<Produto> resultado = produtos.pesquisarAdmin(termo, PageRequest.of(Math.max(pagina, 0), 12));
        model.addAttribute("pagina", resultado);
        model.addAttribute("termo", termo);
        model.addAttribute("paginaAtual", resultado.getNumber());
        model.addAttribute("totalPaginas", resultado.getTotalPages());
        model.addAttribute("semStock", produtos.contarSemStock());
        return "admin/produtos/lista";
    }

    @GetMapping("/novo")
    public String novo(Model model) {
        if (!model.containsAttribute("produto")) {
            model.addAttribute("produto", new ProdutoForm());
        }
        model.addAttribute("categoriasAdmin", categorias.listarTodas());
        return "admin/produtos/formulario";
    }

    @GetMapping("/{id}/editar")
    public String editar(@PathVariable Long id, Model model) {
        if (!model.containsAttribute("produto")) {
            model.addAttribute("produto", produtos.paraForm(produtos.obter(id)));
        }
        model.addAttribute("categoriasAdmin", categorias.listarTodas());
        return "admin/produtos/formulario";
    }

    @PostMapping("/guardar")
    public String guardar(@Valid @ModelAttribute("produto") ProdutoForm form,
            BindingResult validacao,
            @RequestParam(name = "imagem", required = false) MultipartFile imagem,
            RedirectAttributes redirect,
            Model model) {
        if (validacao.hasErrors()) {
            model.addAttribute("categoriasAdmin", categorias.listarTodas());
            return "admin/produtos/formulario";
        }
        try {
            String caminho = imagens.guardar(imagem);
            if (caminho != null) {
                form.setImagemUrl(caminho);
            }
            if (form.getId() == null) {
                produtos.criar(form);
                redirect.addFlashAttribute("sucesso", "Produto criado com sucesso.");
            } else {
                produtos.atualizar(form.getId(), form);
                redirect.addFlashAttribute("sucesso", "Produto atualizado com sucesso.");
            }
            return "redirect:/admin/produtos";
        } catch (RegraNegocioException ex) {
            model.addAttribute("erro", ex.getMessage());
            model.addAttribute("categoriasAdmin", categorias.listarTodas());
            return "admin/produtos/formulario";
        }
    }

    @PostMapping("/{id}/ativar")
    public String alternarAtivo(@PathVariable Long id, RedirectAttributes redirect) {
        try {
            produtos.alternarAtivo(id);
            redirect.addFlashAttribute("sucesso", "Disponibilidade do produto atualizada.");
        } catch (RuntimeException ex) {
            redirect.addFlashAttribute("erro", ex.getMessage());
        }
        return "redirect:/admin/produtos";
    }

    @PostMapping("/{id}/eliminar")
    public String eliminar(@PathVariable Long id, RedirectAttributes redirect) {
        boolean apagado = produtos.eliminar(id);
        redirect.addFlashAttribute("sucesso", apagado
                ? "Produto eliminado."
                : "O produto tem encomendas associadas e foi apenas desativado.");
        return "redirect:/admin/produtos";
    }
}
