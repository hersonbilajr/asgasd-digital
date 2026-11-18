package com.asgasd.digital;

import com.asgasd.digital.model.Role;
import com.asgasd.digital.model.Usuario;
import com.asgasd.digital.repository.RoleRepository;
import com.asgasd.digital.repository.UsuarioRepository;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

/** Configuracao comum: contexto completo, MockMvc e base de dados PostgreSQL de teste. */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public abstract class BaseIntegrationTest {

    protected static final String SENHA_PADRAO = "Teste@2026";

    @Autowired
    protected UsuarioRepository usuarios;

    @Autowired
    protected RoleRepository roles;

    @Autowired
    protected PasswordEncoder encoder;

    protected Usuario criarUsuario(String perfil) {
        String sufixo = UUID.randomUUID().toString().substring(0, 8);
        Usuario usuario = new Usuario();
        usuario.setNome("Utilizador " + sufixo);
        usuario.setEmail("teste-" + sufixo + "@asgasd.co.mz");
        usuario.setSenha(encoder.encode(SENHA_PADRAO));
        usuario.setAtivo(true);
        usuario.adicionarRole(roles.findByNome(perfil)
                .orElseGet(() -> roles.save(new Role(perfil))));
        return usuarios.save(usuario);
    }
}
