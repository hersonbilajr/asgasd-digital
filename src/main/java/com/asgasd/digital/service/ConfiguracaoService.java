package com.asgasd.digital.service;

import com.asgasd.digital.model.Configuracao;
import com.asgasd.digital.repository.ConfiguracaoRepository;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Dados de contacto e redes sociais configuraveis pelo administrador. */
@Service
@RequiredArgsConstructor
public class ConfiguracaoService {

    public static final String TELEFONE = "contacto.telefone";
    public static final String EMAIL = "contacto.email";
    public static final String ENDERECO = "contacto.endereco";
    public static final String WHATSAPP = "contacto.whatsapp";
    public static final String HORARIO = "contacto.horario";
    public static final String FACEBOOK = "social.facebook";
    public static final String INSTAGRAM = "social.instagram";
    public static final String LINKEDIN = "social.linkedin";

    private final ConfiguracaoRepository repository;

    @Transactional(readOnly = true)
    public Map<String, String> todas() {
        List<Configuracao> lista = repository.findAll();
        Map<String, String> mapa = new LinkedHashMap<>();
        lista.forEach(c -> mapa.put(c.getChave(), c.getValor()));
        return mapa;
    }

    @Transactional(readOnly = true)
    public String obter(String chave, String padrao) {
        return repository.findByChave(chave)
                .map(Configuracao::getValor)
                .filter(v -> v != null && !v.isBlank())
                .orElse(padrao);
    }

    @Transactional
    public void definir(String chave, String valor, String descricao) {
        Configuracao config = repository.findByChave(chave)
                .orElseGet(() -> new Configuracao(chave, valor, descricao));
        config.setValor(valor);
        if (descricao != null) {
            config.setDescricao(descricao);
        }
        repository.save(config);
    }

    @Transactional
    public void guardar(Map<String, String> valores) {
        valores.forEach((chave, valor) -> {
            if (chave != null && chave.startsWith("cfg.")) {
                definir(chave.substring(4), valor, null);
            }
        });
    }

    /** Link de WhatsApp pronto a usar (apenas digitos, com indicativo). */
    public String getLinkWhatsapp() {
        String numero = obter(WHATSAPP, "258840000000").replaceAll("[^0-9]", "");
        return "https://wa.me/" + numero;
    }
}
