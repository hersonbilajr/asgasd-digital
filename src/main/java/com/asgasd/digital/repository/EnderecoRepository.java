package com.asgasd.digital.repository;

import com.asgasd.digital.model.Endereco;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EnderecoRepository extends JpaRepository<Endereco, Long> {

    List<Endereco> findByUsuarioIdOrderByPrincipalDesc(Long usuarioId);

    Optional<Endereco> findByIdAndUsuarioId(Long id, Long usuarioId);
}
