package com.asgasd.digital.repository;

import com.asgasd.digital.model.Categoria;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoriaRepository extends JpaRepository<Categoria, Long> {

    List<Categoria> findByAtivoTrueOrderByNomeAsc();

    List<Categoria> findAllByOrderByNomeAsc();

    Optional<Categoria> findBySlug(String slug);

    boolean existsByNomeIgnoreCase(String nome);

    boolean existsBySlug(String slug);
}
