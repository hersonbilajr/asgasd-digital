package com.asgasd.digital.web.admin;

import com.asgasd.digital.exception.RegraNegocioException;
import com.asgasd.digital.service.CategoriaService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/categorias")
@RequiredArgsConstructor
public class AdminCategoriaController {

    private final CategoriaService categorias;

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("categoriasAdmin", categorias.listarTodas());
        return "admin/categorias";
    }

    @PostMapping("/guardar")
    public String guardar(@RequestParam(required = false) Long id,
            @RequestParam String nome,
            @RequestParam(required = false) String descricao,
            @RequestParam(defaultValue = "false") boolean ativo,
            RedirectAttributes redirect) {
        try {
            if (id == null) {
                categorias.criar(nome, descricao, ativo);
                redirect.addFlashAttribute("sucesso", "Categoria criada com sucesso.");
            } else {
                categorias.atualizar(id, nome, descricao, ativo);
                redirect.addFlashAttribute("sucesso", "Categoria atualizada com sucesso.");
            }
        } catch (RegraNegocioException ex) {
            redirect.addFlashAttribute("erro", ex.getMessage());
        }
        return "redirect:/admin/categorias";
    }

    @PostMapping("/{id}/ativar")
    public String alternar(@PathVariable Long id, RedirectAttributes redirect) {
        categorias.alternarAtivo(id);
        redirect.addFlashAttribute("sucesso", "Estado da categoria atualizado.");
        return "redirect:/admin/categorias";
    }
}
