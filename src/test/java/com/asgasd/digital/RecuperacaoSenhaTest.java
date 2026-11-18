package com.asgasd.digital;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.asgasd.digital.dto.SenhaDto;
import com.asgasd.digital.exception.RegraNegocioException;
import com.asgasd.digital.model.Role;
import com.asgasd.digital.model.StatusEncomenda;
import com.asgasd.digital.model.Usuario;
import com.asgasd.digital.repository.EncomendaRepository;
import com.asgasd.digital.service.UsuarioService;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class RecuperacaoSenhaTest extends BaseIntegrationTest {

    @Autowired
    private UsuarioService usuarios;

    @Autowired
    private EncomendaRepository encomendas;

    @Test
    void pedidoDeRecuperacaoGeraLinkERedefinePalavraPasse() {
        Usuario utilizador = criarUsuario(Role.CLIENTE);

        Optional<String> link = usuarios.solicitarRecuperacao(utilizador.getEmail());
        assertThat(link).isPresent();
        assertThat(link.get()).contains("/auth/redefinir?token=");

        String token = link.get().substring(link.get().indexOf("token=") + 6);
        assertThat(usuarios.tokenValido(token)).isTrue();

        SenhaDto senha = new SenhaDto();
        senha.setToken(token);
        senha.setNovaSenha("NovaSenha@2026");
        senha.setConfirmarSenha("NovaSenha@2026");
        usuarios.redefinirSenha(senha);

        Usuario actualizado = usuarios.obterPorEmail(utilizador.getEmail());
        assertThat(encoder.matches("NovaSenha@2026", actualizado.getSenha())).isTrue();
        assertThat(usuarios.tokenValido(token)).isFalse();

        assertThatThrownBy(() -> usuarios.redefinirSenha(senha))
                .isInstanceOf(RegraNegocioException.class);
    }

    @Test
    void emailInexistenteNaoRevelaInformacao() {
        assertThat(usuarios.solicitarRecuperacao("nao-existe@exemplo.co.mz")).isEmpty();
    }

    @Test
    void senhasDiferentesSaoRejeitadas() {
        assertThatThrownBy(() -> {
            SenhaDto senha = new SenhaDto();
            senha.setToken("invalido");
            senha.setNovaSenha("UmaSenha@2026");
            senha.setConfirmarSenha("OutraSenha@2026");
            usuarios.redefinirSenha(senha);
        }).isInstanceOf(RegraNegocioException.class);
    }

    @Test
    void indicadoresUsamApenasEncomendasNaoCanceladas() {
        assertThat(encomendas.somarTotal(List.of(
                StatusEncomenda.PENDENTE,
                StatusEncomenda.CONFIRMADA,
                StatusEncomenda.EM_PREPARACAO,
                StatusEncomenda.ENVIADA,
                StatusEncomenda.ENTREGUE))).isNotNull();
    }
}
