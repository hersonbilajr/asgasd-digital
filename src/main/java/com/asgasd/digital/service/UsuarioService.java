package com.asgasd.digital.service;

import com.asgasd.digital.dto.PerfilDto;
import com.asgasd.digital.dto.RegistoDto;
import com.asgasd.digital.dto.SenhaDto;
import com.asgasd.digital.exception.RecursoNaoEncontradoException;
import com.asgasd.digital.exception.RegraNegocioException;
import com.asgasd.digital.model.PasswordResetToken;
import com.asgasd.digital.model.Role;
import com.asgasd.digital.model.Usuario;
import com.asgasd.digital.repository.PasswordResetTokenRepository;
import com.asgasd.digital.repository.RoleRepository;
import com.asgasd.digital.repository.UsuarioRepository;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Locale;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UsuarioService {

    private final UsuarioRepository usuarios;
    private final RoleRepository roles;
    private final PasswordResetTokenRepository tokens;
    private final PasswordEncoder encoder;
    private final EmailService emailService;

    @Value("${app.base-url}")
    private String baseUrl;

    // ---------------------------------------------------------------- registo

    @Transactional
    public Usuario registar(RegistoDto dto) {
        String email = normalizarEmail(dto.getEmail());
        if (usuarios.existsByEmailIgnoreCase(email)) {
            throw new RegraNegocioException("Ja existe uma conta com este email.");
        }
        if (!dto.isSenhasIguais()) {
            throw new RegraNegocioException("As palavras-passe nao coincidem.");
        }
        Usuario usuario = new Usuario();
        usuario.setNome(dto.getNome().trim());
        usuario.setEmail(email);
        usuario.setTelefone(dto.getTelefone());
        usuario.setSenha(encoder.encode(dto.getSenha()));
        usuario.setAtivo(true);
        Role role = roles.findByNome(Role.CLIENTE)
                .orElseThrow(() -> new RegraNegocioException("Perfil de cliente nao configurado."));
        usuario.adicionarRole(role);
        Usuario salvo = usuarios.save(usuario);
        log.info("Nova conta criada: {}", email);
        return salvo;
    }

    @Transactional(readOnly = true)
    public Usuario obter(Long id) {
        return usuarios.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Utilizador nao encontrado."));
    }

    @Transactional(readOnly = true)
    public Usuario obterPorEmail(String email) {
        return usuarios.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Utilizador nao encontrado."));
    }

    @Transactional(readOnly = true)
    public Optional<Usuario> procurarPorEmail(String email) {
        return email == null ? Optional.empty() : usuarios.findByEmailIgnoreCase(email.trim());
    }

    @Transactional(readOnly = true)
    public Page<Usuario> pesquisar(String termo, Pageable pageable) {
        return usuarios.pesquisar(termo, pageable);
    }

    // ---------------------------------------------------------------- perfil

    @Transactional
    public Usuario atualizarPerfil(Long usuarioId, PerfilDto dto) {
        Usuario usuario = obter(usuarioId);
        String email = normalizarEmail(dto.getEmail());
        usuarios.findByEmailIgnoreCase(email)
                .filter(outro -> !outro.getId().equals(usuarioId))
                .ifPresent(outro -> {
                    throw new RegraNegocioException("Este email ja esta associado a outra conta.");
                });
        usuario.setNome(dto.getNome().trim());
        usuario.setEmail(email);
        usuario.setTelefone(dto.getTelefone());
        return usuarios.save(usuario);
    }

    @Transactional
    public void alterarSenha(Long usuarioId, SenhaDto dto) {
        Usuario usuario = obter(usuarioId);
        if (dto.getSenhaAtual() == null || !encoder.matches(dto.getSenhaAtual(), usuario.getSenha())) {
            throw new RegraNegocioException("A palavra-passe atual nao esta correta.");
        }
        aplicarNovaSenha(usuario, dto);
    }

    private void aplicarNovaSenha(Usuario usuario, SenhaDto dto) {
        if (!dto.isIguais()) {
            throw new RegraNegocioException("As palavras-passe nao coincidem.");
        }
        if (encoder.matches(dto.getNovaSenha(), usuario.getSenha())) {
            throw new RegraNegocioException("A nova palavra-passe deve ser diferente da atual.");
        }
        usuario.setSenha(encoder.encode(dto.getNovaSenha()));
        usuarios.save(usuario);
    }

    // ----------------------------------------------------- recuperacao de senha

    /**
     * Gera um token de recuperacao e envia-o por email.
     *
     * @return URL de redefinicao quando gerada (util para testes e para o log do servidor).
     */
    @Transactional
    public Optional<String> solicitarRecuperacao(String email) {
        Optional<Usuario> utilizador = procurarPorEmail(email);
        if (utilizador.isEmpty()) {
            // Nao revelamos se o email existe (evita enumeracao de contas).
            return Optional.empty();
        }
        Usuario usuario = utilizador.get();
        tokens.invalidarTodosDoUsuario(usuario.getId(), LocalDateTime.now());

        byte[] bytes = new byte[32];
        new SecureRandom().nextBytes(bytes);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);

        PasswordResetToken registo = new PasswordResetToken();
        registo.setUsuario(usuario);
        registo.setToken(token);
        registo.setExpiraEm(LocalDateTime.now().plusHours(2));
        tokens.save(registo);

        String link = baseUrl + "/auth/redefinir?token=" + token;
        emailService.enviar(usuario.getEmail(), "Asgasd Digital - recuperacao de palavra-passe",
                "Ola " + usuario.getPrimeiroNome() + ",\n\n"
                        + "Recebemos um pedido para redefinir a sua palavra-passe.\n"
                        + "Abra o link abaixo (valido durante 2 horas):\n\n"
                        + link + "\n\n"
                        + "Se nao fez este pedido, ignore esta mensagem.\n\n"
                        + "Asgasd Digital");
        log.info("Pedido de recuperacao de palavra-passe para {}", usuario.getEmail());
        return Optional.of(link);
    }

    @Transactional
    public void redefinirSenha(SenhaDto dto) {
        PasswordResetToken registo = tokens.findByToken(dto.getToken() == null ? "" : dto.getToken().trim())
                .orElseThrow(() -> new RegraNegocioException("Link de recuperacao invalido."));
        if (!registo.isValido()) {
            throw new RegraNegocioException("Este link de recuperacao expirou. Solicite um novo.");
        }
        aplicarNovaSenha(registo.getUsuario(), dto);
        registo.setUsadoEm(LocalDateTime.now());
        tokens.save(registo);
        log.info("Palavra-passe redefinida para {}", registo.getUsuario().getEmail());
    }

    @Transactional(readOnly = true)
    public boolean tokenValido(String token) {
        return tokens.findByToken(token == null ? "" : token.trim())
                .map(PasswordResetToken::isValido)
                .orElse(false);
    }

    private String normalizarEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new RegraNegocioException("O email e obrigatorio.");
        }
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
