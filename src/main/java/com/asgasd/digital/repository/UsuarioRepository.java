package com.asgasd.digital.repository;

import com.asgasd.digital.model.Usuario;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCase(String email);

    long countByAtivoTrue();

    @Query("""
            select u from Usuario u
            where :termo is null or :termo = ''
               or lower(u.nome) like lower(concat('%', :termo, '%'))
               or lower(u.email) like lower(concat('%', :termo, '%'))
            order by u.nome asc
            """)
    Page<Usuario> pesquisar(@Param("termo") String termo, Pageable pageable);

    @Query("select u from Usuario u join u.roles r where r.nome = :role order by u.criadoEm desc")
    List<Usuario> findByRole(@Param("role") String role);
}
