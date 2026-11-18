package com.asgasd.digital.repository;

import com.asgasd.digital.model.ItemEncomenda;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ItemEncomendaRepository extends JpaRepository<ItemEncomenda, Long> {

    boolean existsByProdutoId(Long produtoId);
}
