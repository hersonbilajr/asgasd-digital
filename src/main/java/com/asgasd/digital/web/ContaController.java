package com.asgasd.digital.web;

import com.asgasd.digital.dto.PerfilDto;
import com.asgasd.digital.dto.SenhaDto;
import com.asgasd.digital.exception.RegraNegocioException;
import com.asgasd.digital.model.Encomenda;
import com.asgasd.digital.model.Usuario;
import com.asgasd.digital.repository.EnderecoRepository;
import com.asgasd.digital.service.EncomendaService;
import com.asgasd.digital.service.UsuarioService;
import jakarta.validation.Valid;
import java.security.Principal;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/conta")
@RequiredArgsConstructor
public class ContaController {

    private final UsuarioService usuarios;
    private final EncomendaService encomendas;
    private final EnderecoRepository enderecos;

    @ModelAttribute("usuarioConta")
    public Usuario usuarioConta(Principal principal) {
        return usuarios.obterPorEmail(principal.getName());
    }

    @GetMapping
    public String painel(Principal principal, Model model) {
        Usuario usuario = usuarioActual(principal);
        model.addAttribute("encomendas", encomendas.doUsuario(usuario.getId()));
        model.addAttribute("enderecos", enderecos.findByUsuarioIdOrderByPrincipalDesc(usuario.getId()));
        return "conta/painel";
    }

    @GetMapping("/perfil")
    public String perfil(Principal principal, Model model) {
        Usuario usuario = usuarioActual(principal);
        if (!model.containsAttribute("perfil")) {
            PerfilDto perfil = new PerfilDto();
            perfil.setNome(usuario.getNome());
            perfil.setEmail(usuario.getEmail());
            perfil.setTelefone(usuario.getTelefone());
            model.addAttribute("perfil", perfil);
        }
        if (!model.containsAttribute("senhaForm")) {
            model.addAttribute("senhaForm", new SenhaDto());
        }
        return "conta/perfil";
    }

    @PostMapping("/perfil")
    public String guardarPerfil(@Valid @ModelAttribute("perfil") PerfilDto perfil,
            BindingResult validacao,
            Principal principal,
            Model model,
            RedirectAttributes redirect) {
        Usuario usuario = usuarioActual(principal);
        if (validacao.hasErrors()) {
            model.addAttribute("senhaForm", new SenhaDto());
            return "conta/perfil";
        }
        try {
            usuarios.atualizarPerfil(usuario.getId(), perfil);
            redirect.addFlashAttribute("sucesso", "Dados atualizados com sucesso.");
            return "redirect:/conta/perfil";
        } catch (RegraNegocioException ex) {
            model.addAttribute("erro", ex.getMessage());
            model.addAttribute("senhaForm", new SenhaDto());
            return "conta/perfil";
        }
    }

    @PostMapping("/senha")
    public String alterarSenha(@Valid @ModelAttribute("senhaForm") SenhaDto senha,
            BindingResult validacao,
            Principal principal,
            Model model,
            RedirectAttributes redirect) {
        Usuario usuario = usuarioActual(principal);
        if (validacao.hasErrors()) {
            PerfilDto perfil = new PerfilDto();
            perfil.setNome(usuario.getNome());
            perfil.setEmail(usuario.getEmail());
            perfil.setTelefone(usuario.getTelefone());
            model.addAttribute("perfil", perfil);
            return "conta/perfil";
        }
        try {
            usuarios.alterarSenha(usuario.getId(), senha);
            redirect.addFlashAttribute("sucesso", "Palavra-passe alterada com sucesso.");
        } catch (RegraNegocioException ex) {
            redirect.addFlashAttribute("erro", ex.getMessage());
        }
        return "redirect:/conta/perfil";
    }

    @GetMapping("/encomendas")
    public String historico(Principal principal, Model model) {
        Usuario usuario = usuarioActual(principal);
        model.addAttribute("encomendas", encomendas.doUsuario(usuario.getId()));
        return "conta/encomendas";
    }

    @GetMapping("/encomendas/{id}")
    public String detalhe(@PathVariable Long id, Principal principal, Model model) {
        Usuario usuario = usuarioActual(principal);
        Encomenda encomenda = encomendas.obterDoUsuario(id, usuario.getId());
        model.addAttribute("encomenda", encomenda);
        return "conta/encomenda-detalhe";
    }

    @PostMapping("/encomendas/{id}/cancelar")
    public String cancelar(@PathVariable Long id,
            Principal principal,
            RedirectAttributes redirect) {
        Usuario usuario = usuarioActual(principal);
        try {
            encomendas.cancelarPeloCliente(id, usuario);
            redirect.addFlashAttribute("sucesso", "Encomenda cancelada. O stock foi reposto.");
        } catch (RegraNegocioException ex) {
            redirect.addFlashAttribute("erro", ex.getMessage());
        }
        return "redirect:/conta/encomendas/" + id;
    }

    /** Utilizador autenticado resolvido pelo email da sessao (nunca pelo pedido HTTP). */
    private Usuario usuarioActual(Principal principal) {
        return usuarios.obterPorEmail(principal.getName());
    }

    @PostMapping("/encomendas/{id}/comprovativo")
    public String comprovativo(@PathVariable Long id,
            @RequestParam(name = "referencia", required = false) String referencia,
            Principal principal,
            RedirectAttributes redirect) {
        Usuario usuario = usuarioActual(principal);
        try {
            encomendas.registarComprovativo(id, usuario, referencia);
            redirect.addFlashAttribute("sucesso", "Referencia do comprovativo registada. Aguarde confirmacao.");
        } catch (RegraNegocioException ex) {
            redirect.addFlashAttribute("erro", ex.getMessage());
        }
        return "redirect:/conta/encomendas/" + id;
    }
}
