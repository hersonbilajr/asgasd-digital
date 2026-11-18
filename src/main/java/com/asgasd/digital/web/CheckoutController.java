package com.asgasd.digital.web;

import com.asgasd.digital.dto.CheckoutDto;
import com.asgasd.digital.exception.RecursoNaoEncontradoException;
import com.asgasd.digital.exception.RegraNegocioException;
import com.asgasd.digital.model.Carrinho;
import com.asgasd.digital.model.Encomenda;
import com.asgasd.digital.model.Endereco;
import com.asgasd.digital.model.Usuario;
import com.asgasd.digital.repository.EnderecoRepository;
import com.asgasd.digital.service.CarrinhoService;
import com.asgasd.digital.service.EncomendaService;
import com.asgasd.digital.service.MetodoPagamentoService;
import com.asgasd.digital.service.UsuarioService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.security.Principal;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
public class CheckoutController {

    private final CarrinhoService carrinhos;
    private final EncomendaService encomendas;
    private final MetodoPagamentoService metodos;
    private final UsuarioService usuarios;
    private final EnderecoRepository enderecos;

    @GetMapping("/checkout")
    public String formulario(HttpSession sessao, Principal principal, Model model, RedirectAttributes redirect) {
        Carrinho carrinho = carrinhos.obterCarrinho(sessao);
        if (carrinho.isVazio()) {
            redirect.addFlashAttribute("erro", "O carrinho esta vazio. Adicione produtos antes de finalizar.");
            return "redirect:/carrinho";
        }
        Usuario usuario = usuarios.obterPorEmail(principal.getName());
        CheckoutDto dados = new CheckoutDto();
        dados.setNomeContacto(usuario.getNome());
        dados.setEmailContacto(usuario.getEmail());
        dados.setTelefoneContacto(usuario.getTelefone());
        List<Endereco> lista = enderecos.findByUsuarioIdOrderByPrincipalDesc(usuario.getId());
        if (!lista.isEmpty()) {
            Endereco principalEndereco = lista.get(0);
            dados.setProvincia(principalEndereco.getProvincia());
            dados.setCidade(principalEndereco.getCidade());
            dados.setBairro(principalEndereco.getBairro());
            dados.setRua(principalEndereco.getRua());
            dados.setReferencia(principalEndereco.getReferencia());
        }
        model.addAttribute("enderecos", lista);
        return renderizar(carrinho, dados, model);
    }

    @PostMapping("/checkout/confirmar")
    public String confirmar(@Valid @ModelAttribute("dados") CheckoutDto dados,
            BindingResult validacao,
            HttpSession sessao,
            Principal principal,
            Model model,
            RedirectAttributes redirect) {
        Carrinho carrinho = carrinhos.obterCarrinho(sessao);
        if (carrinho.isVazio()) {
            redirect.addFlashAttribute("erro", "O carrinho esta vazio.");
            return "redirect:/carrinho";
        }
        if (validacao.hasErrors()) {
            return renderizar(carrinho, dados, model);
        }
        try {
            Usuario usuario = usuarios.obterPorEmail(principal.getName());
            Encomenda encomenda = encomendas.criar(usuario, carrinho, dados);
            redirect.addFlashAttribute("sucesso", "Encomenda registada com sucesso.");
            return "redirect:/encomenda/" + encomenda.getReferencia();
        } catch (RegraNegocioException ex) {
            model.addAttribute("erro", ex.getMessage());
            return renderizar(carrinho, dados, model);
        }
    }

    @GetMapping("/encomenda/{referencia}")
    public String confirmacao(@PathVariable String referencia, Principal principal, Model model) {
        Encomenda encomenda = encomendas.porReferencia(referencia);
        Usuario usuario = usuarios.obterPorEmail(principal.getName());
        boolean dono = encomenda.getUsuario() != null
                && encomenda.getUsuario().getId().equals(usuario.getId());
        if (!dono && !usuario.isAdministrador()) {
            throw new RecursoNaoEncontradoException("Encomenda nao encontrada.");
        }
        model.addAttribute("encomenda", encomenda);
        return "encomenda/confirmacao";
    }

    private String renderizar(Carrinho carrinho, CheckoutDto dados, Model model) {
        BigDecimal custoEnvio = encomendas.calcularCustoEnvio(carrinho.getSubtotal());
        model.addAttribute("carrinho", carrinho);
        model.addAttribute("dados", dados);
        model.addAttribute("metodosPagamento", metodos.listarAtivos());
        model.addAttribute("custoEnvio", custoEnvio);
        model.addAttribute("total", carrinho.getSubtotal().add(custoEnvio));
        return "encomenda/checkout";
    }
}
