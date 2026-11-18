package com.asgasd.digital.service;

import com.asgasd.digital.exception.RecursoNaoEncontradoException;
import com.asgasd.digital.exception.RegraNegocioException;
import com.asgasd.digital.model.Categoria;
import com.asgasd.digital.repository.CategoriaRepository;
import com.asgasd.digital.util.SlugUtil;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CategoriaService {

    private final CategoriaRepository repository;

    @Transactional(readOnly = true)
    public List<Categoria> listarAtivas() {
        return repository.findByAtivoTrueOrderByNomeAsc();
    }

    @Transactional(readOnly = true)
    public List<Categoria> listarTodas() {
        return repository.findAllByOrderByNomeAsc();
    }

    @Transactional(readOnly = true)
    public Categoria obter(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Categoria nao encontrada."));
    }

    @Transactional
    public Categoria criar(String nome, String descricao, boolean ativo) {
        validarNome(nome, null);
        Categoria categoria = new Categoria();
        aplicar(categoria, nome, descricao, ativo);
        categoria.setSlug(slugUnico(SlugUtil.slugificar(nome), null));
        return repository.save(categoria);
    }

    @Transactional
    public Categoria atualizar(Long id, String nome, String descricao, boolean ativo) {
        Categoria categoria = obter(id);
        validarNome(nome, id);
        aplicar(categoria, nome, descricao, ativo);
        categoria.setSlug(slugUnico(SlugUtil.slugificar(nome), id));
        return repository.save(categoria);
    }

    @Transactional
    public void alternarAtivo(Long id) {
        Categoria categoria = obter(id);
        categoria.setAtivo(!categoria.isAtivo());
        repository.save(categoria);
    }

    private void aplicar(Categoria categoria, String nome, String descricao, boolean ativo) {
        categoria.setNome(nome.trim());
        categoria.setDescricao(descricao == null ? null : descricao.trim());
        categoria.setAtivo(ativo);
    }

    private void validarNome(String nome, Long idAtual) {
        if (nome == null || nome.isBlank()) {
            throw new RegraNegocioException("O nome da categoria e obrigatorio.");
        }
        repository.findAll().stream()
                .filter(c -> c.getNome().equalsIgnoreCase(nome.trim()))
                .filter(c -> idAtual == null || !c.getId().equals(idAtual))
                .findFirst()
                .ifPresent(c -> {
                    throw new RegraNegocioException("Ja existe uma categoria com esse nome.");
                });
    }

    private String slugUnico(String base, Long idAtual) {
        String slug = base.isBlank() ? "categoria" : base;
        String candidato = slug;
        int contador = 2;
        while (true) {
            Categoria existente = repository.findBySlug(candidato).orElse(null);
            if (existente == null || (idAtual != null && existente.getId().equals(idAtual))) {
                return candidato;
            }
            candidato = slug + "-" + contador++;
        }
    }
}
