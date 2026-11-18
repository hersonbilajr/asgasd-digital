package com.asgasd.digital.service;

import com.asgasd.digital.exception.RecursoNaoEncontradoException;
import com.asgasd.digital.exception.RegraNegocioException;
import com.asgasd.digital.model.MetodoPagamento;
import com.asgasd.digital.repository.MetodoPagamentoRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MetodoPagamentoService {

    private final MetodoPagamentoRepository metodos;

    @Transactional(readOnly = true)
    public List<MetodoPagamento> listarAtivos() {
        return metodos.findByAtivoTrueOrderByOrdemAsc();
    }

    @Transactional(readOnly = true)
    public List<MetodoPagamento> listarTodos() {
        return metodos.findAllByOrderByOrdemAsc();
    }

    @Transactional(readOnly = true)
    public MetodoPagamento obter(Long id) {
        return metodos.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Metodo de pagamento nao encontrado."));
    }

    @Transactional
    public MetodoPagamento guardar(MetodoPagamento dados) {
        if (dados.getNome() == null || dados.getNome().isBlank()) {
            throw new RegraNegocioException("O nome do metodo de pagamento e obrigatorio.");
        }
        MetodoPagamento metodo = dados.getId() == null ? new MetodoPagamento() : obter(dados.getId());
        boolean duplicado = metodos.findAll().stream()
                .filter(m -> m.getNome().equalsIgnoreCase(dados.getNome().trim()))
                .anyMatch(m -> !m.getId().equals(metodo.getId()));
        if (duplicado) {
            throw new RegraNegocioException("Ja existe um metodo com esse nome.");
        }
        metodo.setNome(dados.getNome().trim());
        metodo.setDescricao(dados.getDescricao());
        metodo.setInstrucoes(dados.getInstrucoes());
        metodo.setOrdem(dados.getOrdem());
        metodo.setAtivo(dados.isAtivo());
        return metodos.save(metodo);
    }

    @Transactional
    public void alternarAtivo(Long id) {
        MetodoPagamento metodo = obter(id);
        metodo.setAtivo(!metodo.isAtivo());
        metodos.save(metodo);
    }
}
