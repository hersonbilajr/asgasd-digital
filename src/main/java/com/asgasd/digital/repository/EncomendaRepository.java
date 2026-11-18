package com.asgasd.digital.repository;

import com.asgasd.digital.model.Encomenda;
import com.asgasd.digital.model.StatusEncomenda;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface EncomendaRepository extends JpaRepository<Encomenda, Long> {

    Optional<Encomenda> findByReferencia(String referencia);

    @Query("select distinct e from Encomenda e left join fetch e.itens where e.id = :id")
    Optional<Encomenda> findByIdComItens(@Param("id") Long id);

    @Query("select distinct e from Encomenda e left join fetch e.itens where e.id = :id and e.usuario.id = :usuarioId")
    Optional<Encomenda> findByIdAndUsuarioIdComItens(@Param("id") Long id, @Param("usuarioId") Long usuarioId);

    @Query("select distinct e from Encomenda e left join fetch e.itens where e.referencia = :referencia")
    Optional<Encomenda> findByReferenciaComItens(@Param("referencia") String referencia);

    boolean existsByReferencia(String referencia);

    List<Encomenda> findByUsuarioIdOrderByCriadoEmDesc(Long usuarioId);

    Optional<Encomenda> findByIdAndUsuarioId(Long id, Long usuarioId);

    Page<Encomenda> findAllByOrderByCriadoEmDesc(Pageable pageable);

    Page<Encomenda> findByStatusOrderByCriadoEmDesc(StatusEncomenda status, Pageable pageable);

    long countByStatus(StatusEncomenda status);

    @Query("select coalesce(sum(e.total), 0) from Encomenda e where e.status in :status")
    BigDecimal somarTotal(@Param("status") Collection<StatusEncomenda> status);

    @Query("select e.status, count(e) from Encomenda e group by e.status")
    List<Object[]> contarPorStatus();
}
