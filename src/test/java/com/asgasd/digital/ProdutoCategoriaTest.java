package com.asgasd.digital;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.asgasd.digital.dto.ProdutoForm;
import com.asgasd.digital.exception.RegraNegocioException;
import com.asgasd.digital.model.Categoria;
import com.asgasd.digital.model.Produto;
import com.asgasd.digital.repository.ProdutoRepository;
import com.asgasd.digital.service.CategoriaService;
import com.asgasd.digital.service.ProdutoService;
import java.math.BigDecimal;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class ProdutoCategoriaTest extends BaseIntegrationTest {

    @Autowired
    private ProdutoService produtos;

    @Autowired
    private CategoriaService categorias;

    @Autowired
    private ProdutoRepository produtoRepository;

    private Categoria novaCategoria() {
        return categorias.criar("Categoria " + UUID.randomUUID().toString().substring(0, 6), "Teste", true);
    }

    private ProdutoForm form(Categoria categoria, String nome) {
        ProdutoForm form = new ProdutoForm();
        form.setNome(nome);
        form.setCategoriaId(categoria.getId());
        form.setPreco(new BigDecimal("2500.00"));
        form.setStock(10);
        form.setDescricaoCurta("Descricao de teste");
        form.setAtivo(true);
        return form;
    }

    @Test
    void criarEAtualizarProduto() {
        Categoria categoria = novaCategoria();
        Produto produto = produtos.criar(form(categoria, "Produto Servico " + UUID.randomUUID().toString().substring(0, 5)));

        assertThat(produto.getId()).isNotNull();
        assertThat(produto.getSlug()).isNotBlank();
        assertThat(produto.getCategoria().getId()).isEqualTo(categoria.getId());

        ProdutoForm edicao = produtos.paraForm(produto);
        edicao.setPreco(new BigDecimal("1990.00"));
        edicao.setStock(3);
        produtos.atualizar(produto.getId(), edicao);

        Produto actualizado = produtos.obter(produto.getId());
        assertThat(actualizado.getPreco()).isEqualByComparingTo("1990.00");
        assertThat(actualizado.getStock()).isEqualTo(3);
    }

    @Test
    void precoPromocionalTemDeSerInferiorAoPreco() {
        Categoria categoria = novaCategoria();
        ProdutoForm form = form(categoria, "Produto Promo " + UUID.randomUUID().toString().substring(0, 5));
        form.setPrecoPromocional(new BigDecimal("3000.00"));

        assertThatThrownBy(() -> produtos.criar(form))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessageContaining("promocional");
    }

    @Test
    void slugEhUnicoParaNomesIguais() {
        Categoria categoria = novaCategoria();
        String nome = "Produto Repetido " + UUID.randomUUID().toString().substring(0, 5);

        Produto primeiro = produtos.criar(form(categoria, nome));
        Produto segundo = produtos.criar(form(categoria, nome));

        assertThat(primeiro.getSlug()).isNotEqualTo(segundo.getSlug());
        assertThat(segundo.getSlug()).startsWith(primeiro.getSlug());
    }

    @Test
    void skuDuplicadoEhRejeitado() {
        Categoria categoria = novaCategoria();
        String sku = "SKU-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase();

        ProdutoForm primeiro = form(categoria, "Produto SKU A " + UUID.randomUUID().toString().substring(0, 5));
        primeiro.setSku(sku);
        produtos.criar(primeiro);

        ProdutoForm segundo = form(categoria, "Produto SKU B " + UUID.randomUUID().toString().substring(0, 5));
        segundo.setSku(sku.toLowerCase());

        assertThatThrownBy(() -> produtos.criar(segundo))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessageContaining("SKU");
    }

    @Test
    void desativarProdutoRemoveDoCatalogo() {
        Categoria categoria = novaCategoria();
        Produto produto = produtos.criar(form(categoria, "Produto Inativo " + UUID.randomUUID().toString().substring(0, 5)));

        produtos.alternarAtivo(produto.getId());

        assertThat(produtos.obter(produto.getId()).isAtivo()).isFalse();
        assertThatThrownBy(() -> produtos.porSlug(produto.getSlug()))
                .isInstanceOf(com.asgasd.digital.exception.RecursoNaoEncontradoException.class);
    }

    @Test
    void eliminarProdutoSemEncomendasApagaRegisto() {
        Categoria categoria = novaCategoria();
        Produto produto = produtos.criar(form(categoria, "Produto Apagavel " + UUID.randomUUID().toString().substring(0, 5)));

        boolean apagado = produtos.eliminar(produto.getId());

        assertThat(apagado).isTrue();
        assertThat(produtoRepository.findById(produto.getId())).isEmpty();
    }

    @Test
    void categoriasNaoPermitemNomesDuplicados() {
        String nome = "Categoria Unica " + UUID.randomUUID().toString().substring(0, 6);
        categorias.criar(nome, "Teste", true);

        assertThatThrownBy(() -> categorias.criar(nome, "Outra", true))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessageContaining("categoria");
    }

    @Test
    void categoriasSaoListadasPorNome() {
        Categoria categoria = novaCategoria();
        assertThat(categorias.listarTodas())
                .extracting(Categoria::getNome)
                .isSorted()
                .contains(categoria.getNome());
    }
}
