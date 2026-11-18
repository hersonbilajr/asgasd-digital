package com.asgasd.digital;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.asgasd.digital.dto.CheckoutDto;
import com.asgasd.digital.exception.RegraNegocioException;
import com.asgasd.digital.model.Carrinho;
import com.asgasd.digital.model.Categoria;
import com.asgasd.digital.model.Encomenda;
import com.asgasd.digital.model.ItemCarrinho;
import com.asgasd.digital.model.MetodoPagamento;
import com.asgasd.digital.model.Produto;
import com.asgasd.digital.model.Role;
import com.asgasd.digital.model.StatusEncomenda;
import com.asgasd.digital.model.StatusPagamento;
import com.asgasd.digital.model.Usuario;
import com.asgasd.digital.repository.CarrinhoRepository;
import com.asgasd.digital.repository.CategoriaRepository;
import com.asgasd.digital.repository.MetodoPagamentoRepository;
import com.asgasd.digital.repository.ProdutoRepository;
import com.asgasd.digital.service.CarrinhoService;
import com.asgasd.digital.service.EncomendaService;
import com.asgasd.digital.util.SlugUtil;
import java.math.BigDecimal;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpSession;

class CarrinhoEncomendaTest extends BaseIntegrationTest {

    @Autowired
    private CarrinhoService carrinhoService;

    @Autowired
    private EncomendaService encomendaService;

    @Autowired
    private ProdutoRepository produtos;

    @Autowired
    private CategoriaRepository categorias;

    @Autowired
    private MetodoPagamentoRepository metodos;

    @Autowired
    private CarrinhoRepository carrinhos;

    private Produto produto;
    private MetodoPagamento metodo;

    @BeforeEach
    void prepararDados() {
        Categoria categoria = new Categoria();
        categoria.setNome("Teste " + UUID.randomUUID().toString().substring(0, 6));
        categoria.setSlug(SlugUtil.slugificar(categoria.getNome()));
        categoria.setAtivo(true);
        categorias.save(categoria);

        produto = new Produto();
        produto.setNome("Produto Carrinho " + UUID.randomUUID().toString().substring(0, 6));
        produto.setSlug(SlugUtil.slugificar(produto.getNome()));
        produto.setPreco(new BigDecimal("1000.00"));
        produto.setStock(5);
        produto.setCategoria(categoria);
        produto.setAtivo(true);
        produto = produtos.save(produto);

        metodo = new MetodoPagamento();
        metodo.setNome("Metodo Teste " + UUID.randomUUID().toString().substring(0, 6));
        metodo.setInstrucoes("Instrucoes de teste");
        metodo.setAtivo(true);
        metodo.setOrdem(1);
        metodo = metodos.save(metodo);
    }

    @Test
    void adicionarAtualizarERemoverArtigos() {
        MockHttpSession sessao = new MockHttpSession();

        Carrinho carrinho = carrinhoService.adicionar(sessao, produto.getId(), 2);
        assertThat(carrinho.getItens()).hasSize(1);
        assertThat(carrinho.getQuantidadeTotal()).isEqualTo(2);
        assertThat(carrinho.getSubtotal()).isEqualByComparingTo("2000.00");

        Long itemId = carrinho.getItens().get(0).getId();
        carrinho = carrinhoService.atualizarQuantidade(sessao, itemId, 4);
        assertThat(carrinho.getQuantidadeTotal()).isEqualTo(4);
        assertThat(carrinhoService.quantidadeItens(sessao)).isEqualTo(4);

        carrinho = carrinhoService.remover(sessao, itemId);
        assertThat(carrinho.isVazio()).isTrue();
        assertThat(carrinhoService.quantidadeItens(sessao)).isZero();
    }

    @Test
    void quantidadeAcimaDoStockEhLimitada() {
        MockHttpSession sessao = new MockHttpSession();

        assertThatThrownBy(() -> carrinhoService.adicionar(sessao, produto.getId(), 10))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessageContaining("stock");
    }

    @Test
    void criarEncomendaCalculaTotaisBaixaStockEGeraReferencia() {
        Usuario cliente = criarUsuario(Role.CLIENTE);
        MockHttpSession sessao = new MockHttpSession();
        Carrinho carrinho = carrinhoService.adicionar(sessao, produto.getId(), 2);

        Encomenda encomenda = encomendaService.criar(cliente, carrinho, dadosCheckout());

        assertThat(encomenda.getReferencia()).matches("ASG-\\d{8}-[A-Z0-9]{6}");
        assertThat(encomenda.getSubtotal()).isEqualByComparingTo("2000.00");
        assertThat(encomenda.getCustoEnvio()).isEqualByComparingTo("250.00");
        assertThat(encomenda.getTotal()).isEqualByComparingTo("2250.00");
        assertThat(encomenda.getStatus()).isEqualTo(StatusEncomenda.PENDENTE);
        assertThat(encomenda.getItens()).hasSize(1);
        assertThat(encomenda.getPagamento()).isNotNull();
        assertThat(encomenda.getPagamento().getStatus()).isEqualTo(StatusPagamento.PENDENTE);
        assertThat(encomenda.getPagamento().getValor()).isEqualByComparingTo("2250.00");

        Produto actualizado = produtos.findById(produto.getId()).orElseThrow();
        assertThat(actualizado.getStock()).isEqualTo(3);

        Carrinho carrinhoVazio = carrinhos.findById(carrinho.getId()).orElseThrow();
        assertThat(carrinhoVazio.isVazio()).isTrue();
    }

    @Test
    void envioEhGratisAcimaDoLimite() {
        produto.setPreco(new BigDecimal("6000.00"));
        produtos.save(produto);

        Usuario cliente = criarUsuario(Role.CLIENTE);
        Carrinho carrinho = carrinhoService.adicionar(new MockHttpSession(), produto.getId(), 1);

        Encomenda encomenda = encomendaService.criar(cliente, carrinho, dadosCheckout());
        assertThat(encomenda.getCustoEnvio()).isEqualByComparingTo("0.00");
        assertThat(encomenda.getTotal()).isEqualByComparingTo("6000.00");
    }

    @Test
    void cancelarEncomendaRepoeStock() {
        Usuario cliente = criarUsuario(Role.CLIENTE);
        Carrinho carrinho = carrinhoService.adicionar(new MockHttpSession(), produto.getId(), 3);
        Encomenda encomenda = encomendaService.criar(cliente, carrinho, dadosCheckout());

        assertThat(produtos.findById(produto.getId()).orElseThrow().getStock()).isEqualTo(2);

        encomendaService.cancelarPeloCliente(encomenda.getId(), cliente);

        Encomenda cancelada = encomendaService.obter(encomenda.getId());
        assertThat(cancelada.getStatus()).isEqualTo(StatusEncomenda.CANCELADA);
        assertThat(produtos.findById(produto.getId()).orElseThrow().getStock()).isEqualTo(5);

        assertThatThrownBy(() -> encomendaService.cancelarPeloCliente(encomenda.getId(), cliente))
                .isInstanceOf(RegraNegocioException.class);
    }

    @Test
    void encomendaComStockInsuficienteEhRejeitada() {
        Usuario cliente = criarUsuario(Role.CLIENTE);

        Carrinho carrinho = new Carrinho();
        carrinho.setToken(UUID.randomUUID().toString());
        ItemCarrinho item = new ItemCarrinho();
        item.setCarrinho(carrinho);
        item.setProduto(produto);
        item.setQuantidade(99);
        item.setPrecoUnitario(produto.getPreco());
        carrinho.getItens().add(item);
        Carrinho guardado = carrinhos.save(carrinho);

        assertThatThrownBy(() -> encomendaService.criar(cliente, guardado, dadosCheckout()))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessageContaining("Stock insuficiente");

        assertThat(produtos.findById(produto.getId()).orElseThrow().getStock()).isEqualTo(5);
    }

    @Test
    void clienteConsultaDetalheDaSuaEncomenda() {
        Usuario cliente = criarUsuario(Role.CLIENTE);
        Carrinho carrinho = carrinhoService.adicionar(new MockHttpSession(), produto.getId(), 1);
        Encomenda criada = encomendaService.criar(cliente, carrinho, dadosCheckout());

        Encomenda detalhe = encomendaService.obterDoUsuario(criada.getId(), cliente.getId());
        assertThat(detalhe.getReferencia()).isEqualTo(criada.getReferencia());
        assertThat(detalhe.getItens()).isNotEmpty();
        assertThat(detalhe.getUsuario().getId()).isEqualTo(cliente.getId());
    }

    @Test
    void confirmarPagamentoConfirmaEncomendaPendente() {
        Usuario cliente = criarUsuario(Role.CLIENTE);
        Carrinho carrinho = carrinhoService.adicionar(new MockHttpSession(), produto.getId(), 1);
        Encomenda encomenda = encomendaService.criar(cliente, carrinho, dadosCheckout());

        encomendaService.confirmarPagamento(encomenda.getId());

        Encomenda confirmada = encomendaService.obter(encomenda.getId());
        assertThat(confirmada.getPagamento().getStatus()).isEqualTo(StatusPagamento.CONFIRMADO);
        assertThat(confirmada.getPagamento().getConfirmadoEm()).isNotNull();
        assertThat(confirmada.getStatus()).isEqualTo(StatusEncomenda.CONFIRMADA);
    }

    private CheckoutDto dadosCheckout() {
        CheckoutDto dto = new CheckoutDto();
        dto.setNomeContacto("Cliente Teste");
        dto.setTelefoneContacto("+258840000000");
        dto.setEmailContacto("cliente@exemplo.co.mz");
        dto.setProvincia("Maputo Cidade");
        dto.setCidade("Maputo");
        dto.setBairro("Polana");
        dto.setRua("Rua Exemplo 42");
        dto.setReferencia("Portao azul");
        dto.setMetodoPagamentoId(metodo.getId());
        dto.setGuardarEndereco(false);
        return dto;
    }
}
