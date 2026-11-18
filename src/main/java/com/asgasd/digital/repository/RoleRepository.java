package com.asgasd.digital.repository;

import com.asgasd.digital.model.Role;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoleRepository extends JpaRepository<Role, Long> {

    Optional<Role> findByNome(String nome);
}
