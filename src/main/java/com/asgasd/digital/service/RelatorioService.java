package com.asgasd.digital.service;

import com.asgasd.digital.dto.IndicadoresDto;
import com.asgasd.digital.model.Encomenda;
import com.asgasd.digital.model.StatusEncomenda;
import com.asgasd.digital.repository.EncomendaRepository;
import com.asgasd.digital.repository.ProdutoRepository;
import com.asgasd.digital.repository.UsuarioRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Calcula os indicadores apresentados no painel administrativo. */
@Service
@RequiredArgsConstructor
public class RelatorioService {

    private static final List<StatusEncomenda> FATURADAS = List.of(
            StatusEncomenda.PENDENTE,
            StatusEncomenda.CONFIRMADA,
            StatusEncomenda.EM_PREPARACAO,
            StatusEncomenda.ENVIADA,
            StatusEncomenda.ENTREGUE);

    private final EncomendaRepository encomendas;
    private final ProdutoRepository produtos;
    private final UsuarioRepository usuarios;
    private final ContactoService contactos;

    @Transactional(readOnly = true)
    public IndicadoresDto indicadores() {
        BigDecimal faturado = encomendas.somarTotal(FATURADAS);
        BigDecimal entregue = encomendas.somarTotal(List.of(StatusEncomenda.ENTREGUE));
        long totalEncomendas = encomendas.count();

        Map<StatusEncomenda, Long> porStatus = new EnumMap<>(StatusEncomenda.class);
        for (StatusEncomenda status : StatusEncomenda.values()) {
            porStatus.put(status, 0L);
        }
        for (Object[] linha : encomendas.contarPorStatus()) {
            porStatus.put((StatusEncomenda) linha[0], ((Number) linha[1]).longValue());
        }

        BigDecimal ticket = totalEncomendas == 0
                ? BigDecimal.ZERO
                : faturado.divide(BigDecimal.valueOf(totalEncomendas), 2, RoundingMode.HALF_UP);

        List<Encomenda> recentes = encomendas
                .findAllByOrderByCriadoEmDesc(PageRequest.of(0, 6))
                .getContent();

        return new IndicadoresDto(
                faturado,
                entregue,
                ticket,
                totalEncomendas,
                usuarios.count(),
                produtos.count(),
                produtos.contarSemStock(),
                contactos.contarNaoLidas(),
                porStatus,
                recentes,
                produtos.maisVendidos(StatusEncomenda.CANCELADA, PageRequest.of(0, 5)),
                produtos.stockBaixo(5));
    }
}
