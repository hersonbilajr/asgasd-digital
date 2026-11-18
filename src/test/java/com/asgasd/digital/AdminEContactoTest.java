package com.asgasd.digital;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.asgasd.digital.model.Categoria;
import com.asgasd.digital.model.Role;
import com.asgasd.digital.model.Usuario;
import com.asgasd.digital.repository.CategoriaRepository;
import com.asgasd.digital.repository.ConfiguracaoRepository;
import com.asgasd.digital.repository.ContactoRepository;
import com.asgasd.digital.service.ConfiguracaoService;
import com.asgasd.digital.util.SlugUtil;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;

class AdminEContactoTest extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ContactoRepository contactos;

    @Autowired
    private CategoriaRepository categorias;

    @Autowired
    private ConfiguracaoRepository configuracoes;

    @Test
    void administradorCriaProdutoPeloPainel() throws Exception {
        Usuario admin = criarUsuario(Role.ADMIN);
        Categoria categoria = new Categoria();
        categoria.setNome("Painel " + UUID.randomUUID().toString().substring(0, 6));
        categoria.setSlug(SlugUtil.slugificar(categoria.getNome()));
        categoria.setAtivo(true);
        categorias.save(categoria);

        String nome = "Produto Painel " + UUID.randomUUID().toString().substring(0, 6);

        mockMvc.perform(multipart("/admin/produtos/guardar")
                        .param("nome", nome)
                        .param("categoriaId", categoria.getId().toString())
                        .param("preco", "12345.00")
                        .param("stock", "7")
                        .param("descricaoCurta", "Criado pelo teste")
                        .param("ativo", "true")
                        .with(csrf())
                        .with(user(admin.getEmail()).roles("ADMIN")))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/produtos"));
    }

    @Test
    void clienteNaoPodeCriarProdutos() throws Exception {
        Usuario cliente = criarUsuario(Role.CLIENTE);

        mockMvc.perform(multipart("/admin/produtos/guardar")
                        .param("nome", "Produto Proibido")
                        .param("categoriaId", "1")
                        .param("preco", "100.00")
                        .param("stock", "1")
                        .with(csrf())
                        .with(user(cliente.getEmail()).roles("CLIENTE")))
                .andExpect(status().isForbidden());
    }

    @Test
    void administradorGuardaDadosDeContacto() throws Exception {
        Usuario admin = criarUsuario(Role.ADMIN);

        mockMvc.perform(post("/admin/configuracoes/contactos")
                        .param("cfg.contacto.telefone", "+258 84 111 2222")
                        .param("cfg.contacto.email", "contacto@asgasd.co.mz")
                        .param("cfg.contacto.endereco", "Av. Teste 100, Maputo")
                        .param("cfg.contacto.whatsapp", "258841112222")
                        .param("cfg.contacto.horario", "Seg-Sex 9h-17h")
                        .with(csrf())
                        .with(user(admin.getEmail()).roles("ADMIN")))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/configuracoes"));

        assertThat(configuracoes.findByChave(ConfiguracaoService.TELEFONE).orElseThrow().getValor())
                .isEqualTo("+258 84 111 2222");
    }

    @Test
    void formularioDeContactoGuardaMensagem() throws Exception {
        long antes = contactos.count();

        mockMvc.perform(post("/contacto").with(csrf())
                        .param("nome", "Visitante Teste")
                        .param("email", "visitante@exemplo.co.mz")
                        .param("telefone", "+258840000000")
                        .param("assunto", "Duvida sobre entrega")
                        .param("mensagem", "Gostaria de saber o prazo de entrega para a Beira."))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/contacto"));

        assertThat(contactos.count()).isEqualTo(antes + 1);
        assertThat(contactos.findAllByOrderByCriadoEmDesc().get(0).getAssunto())
                .isEqualTo("Duvida sobre entrega");
    }

    @Test
    void mensagemCurtaEhRejeitadaComErroDeValidacao() throws Exception {
        long antes = contactos.count();

        mockMvc.perform(post("/contacto").with(csrf())
                        .param("nome", "Visitante Teste")
                        .param("email", "visitante@exemplo.co.mz")
                        .param("assunto", "Curta")
                        .param("mensagem", "curta"))
                .andExpect(status().isOk())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers
                        .model().attributeHasFieldErrors("mensagemForm", "mensagem"));

        assertThat(contactos.count()).isEqualTo(antes);
    }

    @Test
    void paginasAdministrativasRenderizam() throws Exception {
        Usuario admin = criarUsuario(Role.ADMIN);

        for (String pagina : new String[]{"/admin", "/admin/produtos", "/admin/categorias",
                "/admin/encomendas", "/admin/clientes", "/admin/mensagens", "/admin/configuracoes"}) {
            mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get(pagina)
                            .with(user(admin.getEmail()).roles("ADMIN")))
                    .andExpect(status().isOk());
        }
    }
}
