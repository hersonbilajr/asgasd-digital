package com.asgasd.digital.repository;

import com.asgasd.digital.model.MetodoPagamento;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MetodoPagamentoRepository extends JpaRepository<MetodoPagamento, Long> {

    List<MetodoPagamento> findByAtivoTrueOrderByOrdemAsc();

    List<MetodoPagamento> findAllByOrderByOrdemAsc();

    boolean existsByNomeIgnoreCase(String nome);
}
