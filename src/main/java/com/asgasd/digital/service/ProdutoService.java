package com.asgasd.digital.service;

import com.asgasd.digital.dto.ProdutoFiltro;
import com.asgasd.digital.dto.ProdutoForm;
import com.asgasd.digital.exception.RecursoNaoEncontradoException;
import com.asgasd.digital.exception.RegraNegocioException;
import com.asgasd.digital.model.Categoria;
import com.asgasd.digital.model.Produto;
import com.asgasd.digital.model.StatusEncomenda;
import com.asgasd.digital.repository.ItemEncomendaRepository;
import com.asgasd.digital.repository.ProdutoRepository;
import com.asgasd.digital.util.SlugUtil;
import jakarta.persistence.criteria.Expression;
import java.math.BigDecimal;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProdutoService {

    private final ProdutoRepository produtos;
    private final ItemEncomendaRepository itensEncomenda;
    private final CategoriaService categorias;

    // ---------------------------------------------------------------- catalogo

    @Transactional(readOnly = true)
    public Page<Produto> pesquisar(ProdutoFiltro filtro, Pageable pageable) {
        ProdutoFiltro f = filtro == null ? new ProdutoFiltro() : filtro;
        Specification<Produto> spec = (root, query, cb) -> cb.isTrue(root.get("ativo"));

        if (f.getCategoriaId() != null) {
            spec = spec.and((root, query, cb) ->
                    cb.equal(root.get("categoria").get("id"), f.getCategoriaId()));
        }
        if (f.getTermo() != null && !f.getTermo().isBlank()) {
            String termo = "%" + f.getTermo().trim().toLowerCase() + "%";
            spec = spec.and((root, query, cb) -> cb.or(
                    cb.like(cb.lower(root.get("nome")), termo),
                    cb.like(cb.lower(cb.coalesce(root.get("descricaoCurta"), "")), termo),
                    cb.like(cb.lower(cb.coalesce(root.get("descricao"), "")), termo)));
        }
        if (f.getPrecoMin() != null) {
            spec = spec.and((root, query, cb) -> {
                Expression<BigDecimal> efetivo = cb.coalesce(root.get("precoPromocional"), root.get("preco"));
                return cb.greaterThanOrEqualTo(efetivo, f.getPrecoMin());
            });
        }
        if (f.getPrecoMax() != null) {
            spec = spec.and((root, query, cb) -> {
                Expression<BigDecimal> efetivo = cb.coalesce(root.get("precoPromocional"), root.get("preco"));
                return cb.lessThanOrEqualTo(efetivo, f.getPrecoMax());
            });
        }
        if (f.isApenasDisponiveis()) {
            spec = spec.and((root, query, cb) -> cb.greaterThan(root.get("stock"), 0));
        }

        return produtos.findAll(spec, PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), ordenacao(f.getOrdenar())));
    }

    private Sort ordenacao(String chave) {
        if (chave == null) {
            return Sort.by(Sort.Order.desc("destaque"), Sort.Order.desc("atualizadoEm"));
        }
        return switch (chave) {
            case ProdutoFiltro.PRECO_ASC -> Sort.by(Sort.Order.asc("preco"), Sort.Order.asc("nome"));
            case ProdutoFiltro.PRECO_DESC -> Sort.by(Sort.Order.desc("preco"), Sort.Order.asc("nome"));
            case ProdutoFiltro.NOME -> Sort.by(Sort.Order.asc("nome"));
            case ProdutoFiltro.NOVIDADE -> Sort.by(Sort.Order.desc("criadoEm"));
            default -> Sort.by(Sort.Order.desc("destaque"), Sort.Order.desc("atualizadoEm"));
        };
    }

    @Transactional(readOnly = true)
    public List<Produto> destaques() {
        return produtos.findTop8ByDestaqueTrueAndAtivoTrueOrderByAtualizadoEmDesc();
    }

    @Transactional(readOnly = true)
    public List<Produto> promocoes() {
        return produtos.findTop8ByPrecoPromocionalIsNotNullAndAtivoTrueOrderByAtualizadoEmDesc();
    }

    @Transactional(readOnly = true)
    public List<Produto> maisVendidos(int limite) {
        return produtos.maisVendidos(StatusEncomenda.CANCELADA, PageRequest.of(0, limite));
    }

    @Transactional(readOnly = true)
    public Produto porSlug(String slug) {
        return produtos.findBySlug(slug)
                .filter(Produto::isAtivo)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Produto nao encontrado."));
    }

    @Transactional(readOnly = true)
    public Produto obter(Long id) {
        return produtos.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Produto nao encontrado."));
    }

    @Transactional(readOnly = true)
    public List<Produto> relacionados(Produto produto, int limite) {
        if (produto.getCategoria() == null) {
            return List.of();
        }
        return produtos.findAll((root, query, cb) -> cb.and(
                        cb.isTrue(root.get("ativo")),
                        cb.equal(root.get("categoria").get("id"), produto.getCategoria().getId()),
                        cb.notEqual(root.get("id"), produto.getId())),
                PageRequest.of(0, limite)).getContent();
    }

    // ------------------------------------------------------------- admin CRUD

    @Transactional(readOnly = true)
    public Page<Produto> pesquisarAdmin(String termo, Pageable pageable) {
        Specification<Produto> spec = (root, query, cb) -> cb.conjunction();
        if (termo != null && !termo.isBlank()) {
            String padrao = "%" + termo.trim().toLowerCase() + "%";
            spec = spec.and((root, query, cb) -> cb.or(
                    cb.like(cb.lower(root.get("nome")), padrao),
                    cb.like(cb.lower(cb.coalesce(root.get("sku"), "")), padrao)));
        }
        return produtos.findAll(spec, PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(),
                Sort.by(Sort.Order.desc("atualizadoEm"))));
    }

    @Transactional(readOnly = true)
    public List<Produto> stockBaixo(int limite) {
        return produtos.stockBaixo(limite);
    }

    @Transactional(readOnly = true)
    public long contarSemStock() {
        return produtos.contarSemStock();
    }

    @Transactional
    public Produto criar(ProdutoForm form) {
        Produto produto = new Produto();
        aplicar(produto, form);
        produto.setSlug(slugUnico(SlugUtil.slugificar(form.getNome()), null));
        return produtos.save(produto);
    }

    @Transactional
    public Produto atualizar(Long id, ProdutoForm form) {
        Produto produto = obter(id);
        if (!produto.getNome().equalsIgnoreCase(form.getNome().trim())) {
            produto.setSlug(slugUnico(SlugUtil.slugificar(form.getNome()), id));
        }
        aplicar(produto, form);
        return produtos.save(produto);
    }

    @Transactional
    public void alternarAtivo(Long id) {
        Produto produto = obter(id);
        produto.setAtivo(!produto.isAtivo());
        produtos.save(produto);
    }

    /** Elimina o produto quando ainda nao foi vendido; caso contrario apenas desativa. */
    @Transactional
    public boolean eliminar(Long id) {
        Produto produto = obter(id);
        if (itensEncomenda.existsByProdutoId(id)) {
            produto.setAtivo(false);
            produtos.save(produto);
            return false;
        }
        produtos.delete(produto);
        return true;
    }

    private void aplicar(Produto produto, ProdutoForm form) {
        Categoria categoria = categorias.obter(form.getCategoriaId());
        if (form.getPrecoPromocional() != null
                && form.getPrecoPromocional().compareTo(form.getPreco()) >= 0) {
            throw new RegraNegocioException("O preco promocional deve ser inferior ao preco normal.");
        }
        if (form.getSku() != null && !form.getSku().isBlank()) {
            String sku = form.getSku().trim().toUpperCase();
            boolean duplicado = produtos.findAll().stream()
                    .filter(p -> sku.equalsIgnoreCase(p.getSku()))
                    .anyMatch(p -> !p.getId().equals(form.getId()));
            if (duplicado) {
                throw new RegraNegocioException("Ja existe um produto com esse SKU.");
            }
            produto.setSku(sku);
        } else {
            produto.setSku(null);
        }
        produto.setNome(form.getNome().trim());
        produto.setDescricaoCurta(form.getDescricaoCurta());
        produto.setDescricao(form.getDescricao());
        produto.setPreco(form.getPreco());
        produto.setPrecoPromocional(form.getPrecoPromocional());
        produto.setStock(form.getStock());
        produto.setDestaque(form.isDestaque());
        produto.setAtivo(form.isAtivo());
        produto.setCategoria(categoria);
        if (form.getImagemUrl() != null && !form.getImagemUrl().isBlank()) {
            produto.setImagemUrl(form.getImagemUrl().trim());
        }
    }

    @Transactional(readOnly = true)
    public ProdutoForm paraForm(Produto produto) {
        ProdutoForm form = new ProdutoForm();
        form.setId(produto.getId());
        form.setNome(produto.getNome());
        form.setSku(produto.getSku());
        form.setCategoriaId(produto.getCategoria() == null ? null : produto.getCategoria().getId());
        form.setPreco(produto.getPreco());
        form.setPrecoPromocional(produto.getPrecoPromocional());
        form.setStock(produto.getStock());
        form.setDescricaoCurta(produto.getDescricaoCurta());
        form.setDescricao(produto.getDescricao());
        form.setImagemUrl(produto.getImagemUrl());
        form.setDestaque(produto.isDestaque());
        form.setAtivo(produto.isAtivo());
        return form;
    }

    private String slugUnico(String base, Long idAtual) {
        String slug = base.isBlank() ? "produto" : base;
        String candidato = slug;
        int contador = 2;
        while (true) {
            Produto existente = produtos.findBySlug(candidato).orElse(null);
            if (existente == null || (idAtual != null && existente.getId().equals(idAtual))) {
                return candidato;
            }
            candidato = slug + "-" + contador++;
        }
    }
}
