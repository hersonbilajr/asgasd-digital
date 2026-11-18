package com.asgasd.digital.service;

import com.asgasd.digital.dto.CheckoutDto;
import com.asgasd.digital.exception.RecursoNaoEncontradoException;
import com.asgasd.digital.exception.RegraNegocioException;
import com.asgasd.digital.model.Carrinho;
import com.asgasd.digital.model.Endereco;
import com.asgasd.digital.model.Encomenda;
import com.asgasd.digital.model.ItemCarrinho;
import com.asgasd.digital.model.ItemEncomenda;
import com.asgasd.digital.model.MetodoPagamento;
import com.asgasd.digital.model.Pagamento;
import com.asgasd.digital.model.Produto;
import com.asgasd.digital.model.StatusEncomenda;
import com.asgasd.digital.model.StatusPagamento;
import com.asgasd.digital.model.Usuario;
import com.asgasd.digital.repository.CarrinhoRepository;
import com.asgasd.digital.repository.EncomendaRepository;
import com.asgasd.digital.repository.EnderecoRepository;
import com.asgasd.digital.repository.MetodoPagamentoRepository;
import com.asgasd.digital.repository.ProdutoRepository;
import com.asgasd.digital.repository.UsuarioRepository;
import com.asgasd.digital.util.SlugUtil;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class EncomendaService {

    private final EncomendaRepository encomendas;
    private final ProdutoRepository produtos;
    private final MetodoPagamentoRepository metodos;
    private final CarrinhoRepository carrinhos;
    private final EnderecoRepository enderecos;
    private final UsuarioRepository usuarios;

    @Value("${app.envio.custo}")
    private BigDecimal custoEnvioPadrao;

    @Value("${app.envio.gratis-a-partir-de}")
    private BigDecimal envioGratisAPartirDe;

    // ------------------------------------------------------------------ leitura

    @Transactional(readOnly = true)
    public Encomenda obter(Long id) {
        return encomendas.findByIdComItens(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Encomenda nao encontrada."));
    }

    @Transactional(readOnly = true)
    public Encomenda obterDoUsuario(Long id, Long usuarioId) {
        return encomendas.findByIdAndUsuarioIdComItens(id, usuarioId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Encomenda nao encontrada."));
    }

    @Transactional(readOnly = true)
    public Encomenda porReferencia(String referencia) {
        return encomendas.findByReferenciaComItens(referencia == null ? "" : referencia.trim().toUpperCase())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Encomenda nao encontrada."));
    }

    /** Consulta publica: exige a referencia e o email usado na encomenda. */
    @Transactional(readOnly = true)
    public Encomenda consultarPorReferencia(String referencia, String email) {
        Encomenda encomenda = porReferencia(referencia);
        if (email == null || !encomenda.getEmailContacto().equalsIgnoreCase(email.trim())) {
            throw new RecursoNaoEncontradoException("Encomenda nao encontrada para os dados indicados.");
        }
        return encomenda;
    }

    @Transactional(readOnly = true)
    public List<Encomenda> doUsuario(Long usuarioId) {
        return encomendas.findByUsuarioIdOrderByCriadoEmDesc(usuarioId);
    }

    @Transactional(readOnly = true)
    public Page<Encomenda> listar(StatusEncomenda status, Pageable pageable) {
        return status == null
                ? encomendas.findAllByOrderByCriadoEmDesc(pageable)
                : encomendas.findByStatusOrderByCriadoEmDesc(status, pageable);
    }

    @Transactional(readOnly = true)
    public BigDecimal calcularCustoEnvio(BigDecimal subtotal) {
        if (subtotal == null || subtotal.compareTo(BigDecimal.ZERO) <= 0) {
            return custoEnvioPadrao;
        }
        return subtotal.compareTo(envioGratisAPartirDe) >= 0 ? BigDecimal.ZERO : custoEnvioPadrao;
    }

    // ------------------------------------------------------------- criacao

    @Transactional
    public Encomenda criar(Usuario usuario, Carrinho carrinho, CheckoutDto dto) {
        if (carrinho == null || carrinho.isVazio()) {
            throw new RegraNegocioException("O carrinho esta vazio. Adicione produtos antes de finalizar.");
        }
        MetodoPagamento metodo = metodos.findById(dto.getMetodoPagamentoId())
                .filter(MetodoPagamento::isAtivo)
                .orElseThrow(() -> new RegraNegocioException("Escolha um metodo de pagamento valido."));

        Encomenda encomenda = new Encomenda();
        encomenda.setUsuario(usuarios.getReferenceById(usuario.getId()));
        encomenda.setStatus(StatusEncomenda.PENDENTE);
        encomenda.setNomeContacto(dto.getNomeContacto().trim());
        encomenda.setTelefoneContacto(dto.getTelefoneContacto().trim());
        encomenda.setEmailContacto(dto.getEmailContacto().trim().toLowerCase());
        encomenda.setEnderecoEntrega(dto.enderecoFormatado());
        encomenda.setNotas(dto.getNotas());

        BigDecimal subtotal = BigDecimal.ZERO;
        for (ItemCarrinho item : carrinho.getItens()) {
            Produto produto = produtos.findByIdComLock(item.getProduto().getId())
                    .orElseThrow(() -> new RegraNegocioException("Um dos produtos ja nao existe."));
            if (!produto.isAtivo()) {
                throw new RegraNegocioException("O produto \"" + produto.getNome() + "\" ja nao esta disponivel.");
            }
            if (produto.getStock() < item.getQuantidade()) {
                throw new RegraNegocioException("Stock insuficiente para \"" + produto.getNome()
                        + "\". Disponivel: " + produto.getStock() + " unidade(s).");
            }
            produto.setStock(produto.getStock() - item.getQuantidade());
            produtos.save(produto);

            ItemEncomenda linha = ItemEncomenda.de(produto, item.getQuantidade());
            encomenda.adicionarItem(linha);
            subtotal = subtotal.add(linha.getSubtotal());
        }

        BigDecimal custoEnvio = calcularCustoEnvio(subtotal);
        encomenda.setSubtotal(subtotal);
        encomenda.setCustoEnvio(custoEnvio);
        encomenda.setTotal(subtotal.add(custoEnvio));
        encomenda.setReferencia(gerarReferenciaUnica());

        Pagamento pagamento = new Pagamento();
        pagamento.setMetodoPagamento(metodo);
        pagamento.setStatus(StatusPagamento.PENDENTE);
        pagamento.setValor(encomenda.getTotal());
        pagamento.setInstrucoes(metodo.getInstrucoes());
        encomenda.definirPagamento(pagamento);

        Encomenda salva = encomendas.save(encomenda);

        carrinho.getItens().clear();
        carrinhos.save(carrinho);

        if (dto.isGuardarEndereco()) {
            guardarEndereco(usuario, dto);
        }

        log.info("Encomenda {} criada para {} ({} MT)", salva.getReferencia(), salva.getEmailContacto(), salva.getTotal());
        return salva;
    }

    private void guardarEndereco(Usuario usuario, CheckoutDto dto) {
        Usuario dono = usuarios.findById(usuario.getId()).orElseThrow();
        boolean jaExiste = enderecos.findByUsuarioIdOrderByPrincipalDesc(dono.getId()).stream()
                .anyMatch(e -> e.getResumo().equalsIgnoreCase(dto.enderecoFormatado()));
        if (jaExiste) {
            return;
        }
        Endereco endereco = new Endereco();
        endereco.setUsuario(dono);
        endereco.setProvincia(dto.getProvincia().trim());
        endereco.setCidade(dto.getCidade().trim());
        endereco.setBairro(dto.getBairro().trim());
        endereco.setRua(dto.getRua().trim());
        endereco.setReferencia(dto.getReferencia());
        endereco.setPrincipal(dono.getEnderecos().isEmpty());
        enderecos.save(endereco);
    }

    private String gerarReferenciaUnica() {
        for (int tentativa = 0; tentativa < 10; tentativa++) {
            String referencia = SlugUtil.gerarReferenciaEncomenda();
            if (!encomendas.existsByReferencia(referencia)) {
                return referencia;
            }
        }
        throw new RegraNegocioException("Nao foi possivel gerar a referencia da encomenda. Tente novamente.");
    }

    // -------------------------------------------------------------- estados

    @Transactional
    public Encomenda atualizarStatus(Long encomendaId, StatusEncomenda novoStatus) {
        Encomenda encomenda = obter(encomendaId);
        if (encomenda.getStatus() == novoStatus) {
            return encomenda;
        }
        if (encomenda.getStatus() == StatusEncomenda.CANCELADA) {
            throw new RegraNegocioException("Uma encomenda cancelada nao pode voltar a ser processada.");
        }
        if (novoStatus == StatusEncomenda.CANCELADA) {
            reporStock(encomenda);
            if (encomenda.getPagamento() != null
                    && encomenda.getPagamento().getStatus() == StatusPagamento.CONFIRMADO) {
                encomenda.getPagamento().setStatus(StatusPagamento.REEMBOLSADO);
            }
        }
        encomenda.setStatus(novoStatus);
        return encomendas.save(encomenda);
    }

    @Transactional
    public void cancelarPeloCliente(Long encomendaId, Usuario usuario) {
        Encomenda encomenda = obterDoUsuario(encomendaId, usuario.getId());
        if (!encomenda.isCancelavel()) {
            throw new RegraNegocioException("Esta encomenda ja nao pode ser cancelada. Contacte-nos.");
        }
        reporStock(encomenda);
        if (encomenda.getPagamento() != null
                && encomenda.getPagamento().getStatus() == StatusPagamento.CONFIRMADO) {
            encomenda.getPagamento().setStatus(StatusPagamento.REEMBOLSADO);
        }
        encomenda.setStatus(StatusEncomenda.CANCELADA);
        encomendas.save(encomenda);
        log.info("Encomenda {} cancelada pelo cliente {}", encomenda.getReferencia(), usuario.getEmail());
    }

    @Transactional
    public void confirmarPagamento(Long encomendaId) {
        Encomenda encomenda = obter(encomendaId);
        Pagamento pagamento = encomenda.getPagamento();
        if (pagamento == null) {
            throw new RegraNegocioException("Esta encomenda nao tem pagamento associado.");
        }
        pagamento.setStatus(StatusPagamento.CONFIRMADO);
        pagamento.setConfirmadoEm(LocalDateTime.now());
        if (encomenda.getStatus() == StatusEncomenda.PENDENTE) {
            encomenda.setStatus(StatusEncomenda.CONFIRMADA);
        }
        encomendas.save(encomenda);
        log.info("Pagamento da encomenda {} confirmado", encomenda.getReferencia());
    }

    @Transactional
    public void rejeitarPagamento(Long encomendaId, String motivo) {
        Encomenda encomenda = obter(encomendaId);
        Pagamento pagamento = encomenda.getPagamento();
        if (pagamento == null) {
            throw new RegraNegocioException("Esta encomenda nao tem pagamento associado.");
        }
        pagamento.setStatus(StatusPagamento.REJEITADO);
        if (motivo != null && !motivo.isBlank()) {
            pagamento.setReferenciaExterna(motivo.trim());
        }
        encomendas.save(encomenda);
    }

    /** O cliente pode indicar a referencia do comprovativo de pagamento manual. */
    @Transactional
    public void registarComprovativo(Long encomendaId, Usuario usuario, String referencia) {
        Encomenda encomenda = obterDoUsuario(encomendaId, usuario.getId());
        Pagamento pagamento = encomenda.getPagamento();
        if (pagamento == null) {
            throw new RegraNegocioException("Esta encomenda nao tem pagamento associado.");
        }
        if (pagamento.getStatus() != StatusPagamento.PENDENTE) {
            throw new RegraNegocioException("O pagamento desta encomenda ja foi processado.");
        }
        if (referencia == null || referencia.isBlank()) {
            throw new RegraNegocioException("Indique a referencia do comprovativo.");
        }
        if (referencia.trim().length() > 120) {
            throw new RegraNegocioException("A referencia do comprovativo e demasiado longa.");
        }
        pagamento.setReferenciaExterna(referencia.trim());
        encomendas.save(encomenda);
    }

    private void reporStock(Encomenda encomenda) {
        for (ItemEncomenda item : encomenda.getItens()) {
            Produto produto = item.getProduto();
            if (produto != null) {
                produtos.findByIdComLock(produto.getId()).ifPresent(p -> {
                    p.setStock(p.getStock() + item.getQuantidade());
                    produtos.save(p);
                });
            }
        }
    }

    @Transactional(readOnly = true)
    public Optional<Encomenda> ultimaDoUsuario(Long usuarioId) {
        List<Encomenda> lista = encomendas.findByUsuarioIdOrderByCriadoEmDesc(usuarioId);
        return lista.isEmpty() ? Optional.empty() : Optional.of(lista.get(0));
    }
}
