package com.asgasd.digital.repository;

import com.asgasd.digital.model.PasswordResetToken;
import java.time.LocalDateTime;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {

    Optional<PasswordResetToken> findByToken(String token);

    @Modifying
    @Query("update PasswordResetToken t set t.usadoEm = :agora where t.usuario.id = :usuarioId and t.usadoEm is null")
    void invalidarTodosDoUsuario(@Param("usuarioId") Long usuarioId, @Param("agora") LocalDateTime agora);
}
