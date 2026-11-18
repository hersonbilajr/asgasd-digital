package com.asgasd.digital;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.asgasd.digital.model.Categoria;
import com.asgasd.digital.model.Produto;
import com.asgasd.digital.repository.CategoriaRepository;
import com.asgasd.digital.repository.ProdutoRepository;
import com.asgasd.digital.util.SlugUtil;
import java.math.BigDecimal;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;

class CatalogoTest extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ProdutoRepository produtos;

    @Autowired
    private CategoriaRepository categorias;

    private Categoria categoria(String nome) {
        Categoria categoria = new Categoria();
        categoria.setNome(nome + " " + UUID.randomUUID().toString().substring(0, 6));
        categoria.setSlug(SlugUtil.slugificar(categoria.getNome()));
        categoria.setAtivo(true);
        return categorias.save(categoria);
    }

    private Produto produto(String nome, Categoria categoria, String preco, boolean ativo) {
        Produto produto = new Produto();
        produto.setNome(nome);
        produto.setSlug(SlugUtil.slugificar(nome + "-" + UUID.randomUUID().toString().substring(0, 6)));
        produto.setPreco(new BigDecimal(preco));
        produto.setStock(4);
        produto.setCategoria(categoria);
        produto.setAtivo(ativo);
        produto.setDescricaoCurta("Produto de teste");
        return produtos.save(produto);
    }

    @Test
    void paginaInicialApresentaSeccoesPrincipais() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Categorias de produtos")))
                .andExpect(content().string(containsString("Produtos em destaque")))
                .andExpect(content().string(containsString("Produtos mais vendidos")))
                .andExpect(content().string(containsString("Porque comprar na Asgasd Digital")));
    }

    @Test
    void catalogoListaApenasProdutosAtivos() throws Exception {
        Categoria cat = categoria("Filtro");
        Produto visivel = produto("Produto Visivel X1", cat, "1500.00", true);
        Produto oculto = produto("Produto Oculto X2", cat, "2500.00", false);

        mockMvc.perform(get("/produtos"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString(visivel.getNome())))
                .andExpect(content().string(not(containsString(oculto.getNome()))));
    }

    @Test
    void pesquisaPorNomeFiltraResultados() throws Exception {
        Categoria cat = categoria("Pesquisa");
        Produto encontrado = produto("Portatil Alfa Beta", cat, "30000.00", true);
        Produto outro = produto("Coluna Gama Delta", cat, "3000.00", true);

        mockMvc.perform(get("/produtos").param("termo", "alfa"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString(encontrado.getNome())))
                .andExpect(content().string(not(containsString(outro.getNome()))));
    }

    @Test
    void filtroPorCategoriaEIntervaloDePrecoFunciona() throws Exception {
        Categoria catA = categoria("Categoria A");
        Categoria catB = categoria("Categoria B");
        Produto barato = produto("Produto Barato A", catA, "900.00", true);
        Produto caro = produto("Produto Caro A", catA, "90000.00", true);
        Produto outraCategoria = produto("Produto Outra B", catB, "1500.00", true);

        mockMvc.perform(get("/produtos")
                        .param("categoriaId", String.valueOf(catA.getId()))
                        .param("precoMin", "500")
                        .param("precoMax", "5000"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString(barato.getNome())))
                .andExpect(content().string(not(containsString(caro.getNome()))))
                .andExpect(content().string(not(containsString(outraCategoria.getNome()))));
    }

    @Test
    void paginaDoProdutoApresentaPrecosEDisponibilidade() throws Exception {
        Categoria cat = categoria("Detalhe");
        Produto produto = produto("Produto Detalhado Z", cat, "4500.00", true);

        mockMvc.perform(get("/produto/" + produto.getSlug()))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Produto Detalhado Z")))
                .andExpect(content().string(containsString("Adicionar ao carrinho")));
    }

    @Test
    void produtoInativoOuInexistenteDevolve404() throws Exception {
        Categoria cat = categoria("Inativo");
        Produto produto = produto("Produto Removido W", cat, "1000.00", false);

        mockMvc.perform(get("/produto/" + produto.getSlug()))
                .andExpect(status().isNotFound());

        mockMvc.perform(get("/produto/produto-que-nao-existe"))
                .andExpect(status().isNotFound());
    }
}
