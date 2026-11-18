package com.asgasd.digital;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.asgasd.digital.model.Role;
import com.asgasd.digital.model.Usuario;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

class AutenticacaoTest extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private com.asgasd.digital.repository.ContactoRepository contactos;

    @Test
    void paginaDeLoginEstaAcessivel() throws Exception {
        mockMvc.perform(get("/auth/login"))
                .andExpect(status().isOk());
    }

    @Test
    void registoCriaContaComPalavraPasseBCrypt() throws Exception {
        String email = "novo-" + UUID.randomUUID().toString().substring(0, 8) + "@asgasd.co.mz";

        mockMvc.perform(post("/auth/registo").with(csrf())
                        .param("nome", "Cliente Novo")
                        .param("email", email)
                        .param("telefone", "+258840000000")
                        .param("senha", "Segredo@2026")
                        .param("confirmarSenha", "Segredo@2026"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/auth/login?registado"));

        Usuario criado = usuarios.findByEmailIgnoreCase(email).orElseThrow();
        assertThat(criado.getSenha()).isNotEqualTo("Segredo@2026");
        assertThat(criado.getSenha()).startsWith("$2");
        assertThat(passwordEncoder.matches("Segredo@2026", criado.getSenha())).isTrue();
        assertThat(criado.getRoles()).extracting(Role::getNome).contains(Role.CLIENTE);
    }

    @Test
    void registoRejeitaEmailDuplicado() throws Exception {
        String email = "dup-" + UUID.randomUUID().toString().substring(0, 8) + "@asgasd.co.mz";
        mockMvc.perform(post("/auth/registo").with(csrf())
                        .param("nome", "Primeiro Cliente")
                        .param("email", email)
                        .param("senha", "Segredo@2026")
                        .param("confirmarSenha", "Segredo@2026"))
                .andExpect(status().is3xxRedirection());

        mockMvc.perform(post("/auth/registo").with(csrf())
                        .param("nome", "Segundo Cliente")
                        .param("email", email)
                        .param("senha", "Segredo@2026")
                        .param("confirmarSenha", "Segredo@2026"))
                .andExpect(status().isOk())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers
                        .model().attributeHasFieldErrors("registo", "email"));
    }

    @Test
    void loginComCredenciaisValidasRedirecionaParaConta() throws Exception {
        Usuario usuario = criarUsuario(Role.CLIENTE);

        mockMvc.perform(formLogin("/auth/login").user("email", usuario.getEmail()).password("senha", SENHA_PADRAO))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/conta"));

        mockMvc.perform(get("/conta").with(org.springframework.security.test.web.servlet.request
                        .SecurityMockMvcRequestPostProcessors.user(usuario.getEmail())
                        .roles("CLIENTE")))
                .andExpect(status().isOk());
    }

    @Test
    void loginComPalavraPasseErradaFalha() throws Exception {
        Usuario usuario = criarUsuario(Role.CLIENTE);

        mockMvc.perform(formLogin("/auth/login").user("email", usuario.getEmail()).password("senha", "errada"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/auth/login?erro"));
    }

    @Test
    void areaAdministrativaEstaProtegida() throws Exception {
        mockMvc.perform(get("/admin"))
                .andExpect(status().is3xxRedirection())
                .andExpect(header().string("Location", org.hamcrest.Matchers.containsString("/auth/login")));

        Usuario cliente = criarUsuario(Role.CLIENTE);
        mockMvc.perform(get("/admin").with(org.springframework.security.test.web.servlet.request
                        .SecurityMockMvcRequestPostProcessors.user(cliente.getEmail()).roles("CLIENTE")))
                .andExpect(status().isForbidden());

        Usuario admin = criarUsuario(Role.ADMIN);
        mockMvc.perform(get("/admin").with(org.springframework.security.test.web.servlet.request
                        .SecurityMockMvcRequestPostProcessors.user(admin.getEmail()).roles("ADMIN")))
                .andExpect(status().isOk());
    }

    @Test
    void formularioSemTokenCsrfNaoEhProcessado() throws Exception {
        long mensagensAntes = contactos.count();

        // Visitante anonimo: o Spring Security nao processa o pedido sem token CSRF.
        MvcResult anonimo = mockMvc.perform(post("/contacto")
                        .param("nome", "Sem Token")
                        .param("email", "semtoken@exemplo.co.mz")
                        .param("assunto", "Teste")
                        .param("mensagem", "Mensagem de teste sem token CSRF"))
                .andReturn();
        assertThat(anonimo.getResponse().getStatus())
                .as("CSRF deve bloquear o pedido (redirect=%s)", anonimo.getResponse().getRedirectedUrl())
                .isIn(302, 403);
        assertThat(anonimo.getResponse().getRedirectedUrl() == null
                || anonimo.getResponse().getRedirectedUrl().contains("/auth/login")).isTrue();

        // Utilizador autenticado sem token CSRF: pedido tambem recusado.
        Usuario cliente = criarUsuario(Role.CLIENTE);
        MvcResult autenticado = mockMvc.perform(post("/contacto")
                        .with(org.springframework.security.test.web.servlet.request
                                .SecurityMockMvcRequestPostProcessors.user(cliente.getEmail()).roles("CLIENTE"))
                        .param("nome", "Sem Token")
                        .param("email", "semtoken@exemplo.co.mz")
                        .param("assunto", "Teste")
                        .param("mensagem", "Mensagem de teste sem token CSRF"))
                .andReturn();
        assertThat(autenticado.getResponse().getStatus())
                .as("CSRF deve bloquear o pedido (redirect=%s)", autenticado.getResponse().getRedirectedUrl())
                .isIn(302, 403);

        assertThat(contactos.count()).isEqualTo(mensagensAntes);
    }
}
