package com.asgasd.digital;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.asgasd.digital.dto.CheckoutDto;
import com.asgasd.digital.model.Categoria;
import com.asgasd.digital.model.Encomenda;
import com.asgasd.digital.model.MetodoPagamento;
import com.asgasd.digital.model.Produto;
import com.asgasd.digital.model.Role;
import com.asgasd.digital.model.Usuario;
import com.asgasd.digital.repository.CategoriaRepository;
import com.asgasd.digital.repository.MetodoPagamentoRepository;
import com.asgasd.digital.repository.ProdutoRepository;
import com.asgasd.digital.service.CarrinhoService;
import com.asgasd.digital.service.EncomendaService;
import com.asgasd.digital.util.SlugUtil;
import java.math.BigDecimal;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;

/** Verifica as paginas HTML da area do cliente com dados reais. */
class AreaClienteTest extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CarrinhoService carrinhos;

    @Autowired
    private EncomendaService encomendas;

    @Autowired
    private ProdutoRepository produtos;

    @Autowired
    private CategoriaRepository categorias;

    @Autowired
    private MetodoPagamentoRepository metodos;

    @Test
    void clienteVeDetalheDaSuaEncomenda() throws Exception {
        Categoria categoria = new Categoria();
        categoria.setNome("Area " + UUID.randomUUID().toString().substring(0, 6));
        categoria.setSlug(SlugUtil.slugificar(categoria.getNome()));
        categoria.setAtivo(true);
        categorias.save(categoria);

        Produto produto = new Produto();
        produto.setNome("Produto Area " + UUID.randomUUID().toString().substring(0, 6));
        produto.setSlug(SlugUtil.slugificar(produto.getNome()));
        produto.setPreco(new BigDecimal("1500.00"));
        produto.setStock(3);
        produto.setCategoria(categoria);
        produto.setAtivo(true);
        produtos.save(produto);

        MetodoPagamento metodo = new MetodoPagamento();
        metodo.setNome("Metodo Area " + UUID.randomUUID().toString().substring(0, 6));
        metodo.setAtivo(true);
        metodo.setOrdem(2);
        metodos.save(metodo);

        Usuario cliente = criarUsuario(Role.CLIENTE);
        var carrinho = carrinhos.adicionar(new MockHttpSession(), produto.getId(), 2);

        CheckoutDto dados = new CheckoutDto();
        dados.setNomeContacto("Cliente Area");
        dados.setTelefoneContacto("+258840000000");
        dados.setEmailContacto("cliente.area@exemplo.co.mz");
        dados.setProvincia("Maputo Cidade");
        dados.setCidade("Maputo");
        dados.setBairro("Polana");
        dados.setRua("Rua Area 10");
        dados.setMetodoPagamentoId(metodo.getId());
        Encomenda encomenda = encomendas.criar(cliente, carrinho, dados);

        mockMvc.perform(get("/conta").with(user(cliente.getEmail()).roles("CLIENTE")))
                .andExpect(status().isOk());

        mockMvc.perform(get("/conta/encomendas").with(user(cliente.getEmail()).roles("CLIENTE")))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString(encomenda.getReferencia())));

        mockMvc.perform(get("/conta/encomendas/{id}", encomenda.getId())
                        .with(user(cliente.getEmail()).roles("CLIENTE")))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Pagamento")));

        mockMvc.perform(get("/conta/perfil").with(user(cliente.getEmail()).roles("CLIENTE")))
                .andExpect(status().isOk());
    }
}
