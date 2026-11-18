package com.asgasd.digital.repository;

import com.asgasd.digital.model.Produto;
import com.asgasd.digital.model.StatusEncomenda;
import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProdutoRepository extends JpaRepository<Produto, Long>, JpaSpecificationExecutor<Produto> {

    Optional<Produto> findBySlug(String slug);

    /** Bloqueia a linha do produto durante o checkout para evitar venda de stock inexistente. */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from Produto p where p.id = :id")
    Optional<Produto> findByIdComLock(@Param("id") Long id);

    boolean existsBySkuIgnoreCase(String sku);

    boolean existsBySlug(String slug);

    List<Produto> findTop8ByDestaqueTrueAndAtivoTrueOrderByAtualizadoEmDesc();

    List<Produto> findTop8ByPrecoPromocionalIsNotNullAndAtivoTrueOrderByAtualizadoEmDesc();

    @Query("select p from Produto p where p.ativo = true and p.stock <= :limite order by p.stock asc")
    List<Produto> stockBaixo(@Param("limite") int limite);

    @Query("select count(p) from Produto p where p.ativo = true and p.stock = 0")
    long contarSemStock();

    @Query("""
            select i.produto from ItemEncomenda i
            where i.produto is not null and i.encomenda.status <> :cancelada
            group by i.produto
            order by sum(i.quantidade) desc
            """)
    List<Produto> maisVendidos(@Param("cancelada") StatusEncomenda cancelada, Pageable pageable);
}
