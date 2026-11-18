package com.asgasd.digital.web.admin;

import com.asgasd.digital.repository.EnderecoRepository;
import com.asgasd.digital.service.EncomendaService;
import com.asgasd.digital.service.UsuarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/admin/clientes")
@RequiredArgsConstructor
public class AdminClienteController {

    private final UsuarioService usuarios;
    private final EncomendaService encomendas;
    private final EnderecoRepository enderecos;

    @GetMapping
    public String listar(@RequestParam(required = false) String termo,
            @RequestParam(defaultValue = "0") int pagina,
            Model model) {
        Page<?> resultado = usuarios.pesquisar(termo, PageRequest.of(Math.max(pagina, 0), 15));
        model.addAttribute("pagina", resultado);
        model.addAttribute("termo", termo);
        model.addAttribute("paginaAtual", resultado.getNumber());
        model.addAttribute("totalPaginas", resultado.getTotalPages());
        return "admin/clientes";
    }

    @GetMapping("/{id}")
    public String detalhe(@PathVariable Long id, Model model) {
        var cliente = usuarios.obter(id);
        model.addAttribute("cliente", cliente);
        model.addAttribute("encomendasCliente", encomendas.doUsuario(id));
        model.addAttribute("enderecosCliente", enderecos.findByUsuarioIdOrderByPrincipalDesc(id));
        return "admin/cliente-detalhe";
    }
}
