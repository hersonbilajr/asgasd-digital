package com.asgasd.digital.repository;

import com.asgasd.digital.model.Contacto;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ContactoRepository extends JpaRepository<Contacto, Long> {

    List<Contacto> findAllByOrderByCriadoEmDesc();

    long countByLidaFalse();
}
