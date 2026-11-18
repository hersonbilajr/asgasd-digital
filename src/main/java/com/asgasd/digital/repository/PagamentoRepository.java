package com.asgasd.digital.repository;

import com.asgasd.digital.model.Pagamento;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PagamentoRepository extends JpaRepository<Pagamento, Long> {

    Optional<Pagamento> findByEncomendaId(Long encomendaId);
}
