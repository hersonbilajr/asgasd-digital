package com.asgasd.digital.web;

import com.asgasd.digital.exception.RecursoNaoEncontradoException;
import com.asgasd.digital.exception.RegraNegocioException;
import com.asgasd.digital.model.Categoria;
import com.asgasd.digital.model.Usuario;
import com.asgasd.digital.service.CarrinhoService;
import com.asgasd.digital.service.CategoriaService;
import com.asgasd.digital.service.ConfiguracaoService;
import com.asgasd.digital.service.ContactoService;
import com.asgasd.digital.service.UsuarioService;
import jakarta.servlet.http.HttpSession;
import java.time.Year;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.servlet.ModelAndView;

/** Disponibiliza dados comuns (menu, contactos, carrinho) a todas as paginas. */
@ControllerAdvice
@RequiredArgsConstructor
public class GlobalControllerAdvice {

    private final CategoriaService categorias;
    private final ConfiguracaoService configuracoes;
    private final CarrinhoService carrinhos;
    private final ContactoService contactos;
    private final UsuarioService usuarios;

    @ModelAttribute("categoriasMenu")
    public List<Categoria> categoriasMenu() {
        return categorias.listarAtivas();
    }

    @ModelAttribute("config")
    public Map<String, String> configuracao() {
        return configuracoes.todas();
    }

    @ModelAttribute("whatsappLink")
    public String whatsappLink() {
        return configuracoes.getLinkWhatsapp();
    }

    @ModelAttribute("anoAtual")
    public int anoAtual() {
        return Year.now().getValue();
    }

    @ModelAttribute("quantidadeCarrinho")
    public int quantidadeCarrinho(HttpSession sessao) {
        return carrinhos.quantidadeItens(sessao);
    }

    @ModelAttribute("usuarioLogado")
    public Usuario usuarioLogado() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()
                || auth instanceof org.springframework.security.authentication.AnonymousAuthenticationToken) {
            return null;
        }
        return usuarios.procurarPorEmail(auth.getName()).orElse(null);
    }

    @ModelAttribute("mensagensNaoLidas")
    public long mensagensNaoLidas() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) {
            return 0;
        }
        boolean admin = auth.getAuthorities().stream()
                .anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()));
        return admin ? contactos.contarNaoLidas() : 0;
    }

    @ExceptionHandler(RecursoNaoEncontradoException.class)
    public ModelAndView naoEncontrado(RecursoNaoEncontradoException ex) {
        return erro(404, "Pagina nao encontrada", ex.getMessage());
    }

    @ExceptionHandler(RegraNegocioException.class)
    public ModelAndView regraNegocio(RegraNegocioException ex) {
        return erro(400, "Nao foi possivel concluir a operacao", ex.getMessage());
    }

    private ModelAndView erro(int codigo, String titulo, String mensagem) {
        ModelAndView mv = new ModelAndView("erro");
        mv.setStatus(org.springframework.http.HttpStatusCode.valueOf(codigo));
        mv.addObject("codigo", codigo);
        mv.addObject("titulo", titulo);
        mv.addObject("mensagem", mensagem);
        return mv;
    }
}
