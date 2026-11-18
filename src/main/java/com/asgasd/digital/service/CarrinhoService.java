package com.asgasd.digital.service;

import com.asgasd.digital.exception.RecursoNaoEncontradoException;
import com.asgasd.digital.exception.RegraNegocioException;
import com.asgasd.digital.model.Carrinho;
import com.asgasd.digital.model.ItemCarrinho;
import com.asgasd.digital.model.Produto;
import com.asgasd.digital.model.Usuario;
import com.asgasd.digital.repository.CarrinhoRepository;
import com.asgasd.digital.repository.ProdutoRepository;
import com.asgasd.digital.repository.UsuarioRepository;
import jakarta.servlet.http.HttpSession;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Carrinho de compras.
 *
 * <p>Clientes autenticados tem um carrinho persistente associado a conta. Visitantes
 * recebem um token guardado na sessao HTTP, que e convertido no carrinho da conta
 * assim que iniciam sessao.</p>
 */
@Service
@RequiredArgsConstructor
public class CarrinhoService {

    public static final String TOKEN_SESSAO = "asgasd.carrinho.token";

    private final CarrinhoRepository carrinhos;
    private final ProdutoRepository produtos;
    private final UsuarioRepository usuarios;

    @Transactional
    public Carrinho obterCarrinho(HttpSession sessao) {
        Optional<Usuario> utilizador = utilizadorAutenticado();
        if (utilizador.isPresent()) {
            Carrinho carrinho = carrinhos.findByUsuarioId(utilizador.get().getId())
                    .orElseGet(() -> {
                        Carrinho novo = new Carrinho();
                        novo.setUsuario(utilizador.get());
                        return carrinhos.save(novo);
                    });
            fundirCarrinhoDeVisitante(carrinho, sessao);
            return carrinho;
        }
        String token = (String) sessao.getAttribute(TOKEN_SESSAO);
        if (token == null) {
            token = UUID.randomUUID().toString().replace("-", "");
            sessao.setAttribute(TOKEN_SESSAO, token);
            Carrinho novo = new Carrinho();
            novo.setToken(token);
            return carrinhos.save(novo);
        }
        final String tokenAtual = token;
        return carrinhos.findByToken(tokenAtual).orElseGet(() -> {
            Carrinho novo = new Carrinho();
            novo.setToken(tokenAtual);
            return carrinhos.save(novo);
        });
    }

    /** Quantidade de artigos para o cabecalho, sem criar carrinhos em cada pedido. */
    @Transactional(readOnly = true)
    public int quantidadeItens(HttpSession sessao) {
        Optional<Usuario> utilizador = utilizadorAutenticado();
        if (utilizador.isPresent()) {
            return carrinhos.findByUsuarioId(utilizador.get().getId())
                    .map(Carrinho::getQuantidadeTotal)
                    .orElse(0);
        }
        String token = (String) sessao.getAttribute(TOKEN_SESSAO);
        if (token == null) {
            return 0;
        }
        return carrinhos.findByToken(token).map(Carrinho::getQuantidadeTotal).orElse(0);
    }

    @Transactional
    public Carrinho adicionar(HttpSession sessao, Long produtoId, int quantidade) {
        if (quantidade < 1) {
            throw new RegraNegocioException("A quantidade minima e 1 unidade.");
        }
        Produto produto = produtos.findById(produtoId)
                .filter(Produto::isAtivo)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Produto nao encontrado."));
        if (produto.getStock() <= 0) {
            throw new RegraNegocioException("O produto \"" + produto.getNome() + "\" esta sem stock.");
        }
        Carrinho carrinho = obterCarrinho(sessao);
        ItemCarrinho item = carrinho.procurarItem(produto).orElse(null);
        int atual = item == null ? 0 : item.getQuantidade();
        int pretendida = atual + quantidade;
        int disponivel = produto.getStock();

        if (pretendida > disponivel) {
            if (item != null) {
                item.setQuantidade(disponivel);
            } else {
                item = novoItem(carrinho, produto, disponivel);
            }
            carrinhos.save(carrinho);
            throw new RegraNegocioException("Apenas temos " + disponivel + " unidade(s) de \""
                    + produto.getNome() + "\" em stock. A quantidade foi ajustada.");
        }
        if (item != null) {
            item.setQuantidade(pretendida);
            item.setPrecoUnitario(produto.getPrecoVenda());
        } else {
            novoItem(carrinho, produto, quantidade);
        }
        return carrinhos.save(carrinho);
    }

    @Transactional
    public Carrinho atualizarQuantidade(HttpSession sessao, Long itemId, int quantidade) {
        Carrinho carrinho = obterCarrinho(sessao);
        ItemCarrinho item = carrinho.getItens().stream()
                .filter(i -> i.getId().equals(itemId))
                .findFirst()
                .orElseThrow(() -> new RecursoNaoEncontradoException("Artigo do carrinho nao encontrado."));
        if (quantidade <= 0) {
            carrinho.getItens().remove(item);
            return carrinhos.save(carrinho);
        }
        int disponivel = item.getProduto().getStock();
        if (quantidade > disponivel) {
            throw new RegraNegocioException("Apenas existem " + disponivel + " unidade(s) disponiveis de \""
                    + item.getProduto().getNome() + "\".");
        }
        item.setQuantidade(quantidade);
        item.setPrecoUnitario(item.getProduto().getPrecoVenda());
        return carrinhos.save(carrinho);
    }

    @Transactional
    public Carrinho remover(HttpSession sessao, Long itemId) {
        Carrinho carrinho = obterCarrinho(sessao);
        carrinho.getItens().removeIf(i -> i.getId().equals(itemId));
        return carrinhos.save(carrinho);
    }

    @Transactional
    public void limpar(HttpSession sessao) {
        Carrinho carrinho = obterCarrinho(sessao);
        carrinho.getItens().clear();
        carrinhos.save(carrinho);
    }

    /** Carrinho persistente do cliente (usado no checkout). */
    @Transactional(readOnly = true)
    public Carrinho doUsuario(Usuario usuario) {
        return carrinhos.findByUsuarioId(usuario.getId())
                .orElseThrow(() -> new RegraNegocioException("O seu carrinho esta vazio."));
    }

    private ItemCarrinho novoItem(Carrinho carrinho, Produto produto, int quantidade) {
        ItemCarrinho item = new ItemCarrinho();
        item.setCarrinho(carrinho);
        item.setProduto(produto);
        item.setQuantidade(quantidade);
        item.setPrecoUnitario(produto.getPrecoVenda());
        carrinho.getItens().add(item);
        return item;
    }

    private void fundirCarrinhoDeVisitante(Carrinho carrinhoDaConta, HttpSession sessao) {
        String token = (String) sessao.getAttribute(TOKEN_SESSAO);
        if (token == null) {
            return;
        }
        carrinhos.findByToken(token).ifPresent(visitante -> {
            List<ItemCarrinho> itensVisitante = List.copyOf(visitante.getItens());
            for (ItemCarrinho itemVisitante : itensVisitante) {
                Produto produto = itemVisitante.getProduto();
                if (produto == null || !produto.isAtivo()) {
                    continue;
                }
                ItemCarrinho existente = carrinhoDaConta.procurarItem(produto).orElse(null);
                int total = (existente == null ? 0 : existente.getQuantidade()) + itemVisitante.getQuantidade();
                int limitado = Math.min(total, produto.getStock());
                if (limitado <= 0) {
                    continue;
                }
                if (existente == null) {
                    novoItem(carrinhoDaConta, produto, limitado);
                } else {
                    existente.setQuantidade(limitado);
                }
            }
            carrinhos.save(carrinhoDaConta);
            carrinhos.delete(visitante);
            sessao.removeAttribute(TOKEN_SESSAO);
        });
    }

    private Optional<Usuario> utilizadorAutenticado() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth instanceof AnonymousAuthenticationToken) {
            return Optional.empty();
        }
        return usuarios.findByEmailIgnoreCase(auth.getName());
    }
}
