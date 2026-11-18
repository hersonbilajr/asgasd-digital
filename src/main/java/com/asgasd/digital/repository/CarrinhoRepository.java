package com.asgasd.digital.repository;

import com.asgasd.digital.model.Carrinho;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CarrinhoRepository extends JpaRepository<Carrinho, Long> {

    Optional<Carrinho> findByUsuarioId(Long usuarioId);

    Optional<Carrinho> findByToken(String token);
}
