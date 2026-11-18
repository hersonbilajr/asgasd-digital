package com.asgasd.digital.config;

import com.asgasd.digital.model.Categoria;
import com.asgasd.digital.model.Configuracao;
import com.asgasd.digital.model.MetodoPagamento;
import com.asgasd.digital.model.Produto;
import com.asgasd.digital.model.Role;
import com.asgasd.digital.model.Usuario;
import com.asgasd.digital.repository.CategoriaRepository;
import com.asgasd.digital.repository.ConfiguracaoRepository;
import com.asgasd.digital.repository.MetodoPagamentoRepository;
import com.asgasd.digital.repository.ProdutoRepository;
import com.asgasd.digital.repository.RoleRepository;
import com.asgasd.digital.repository.UsuarioRepository;
import com.asgasd.digital.service.ConfiguracaoService;
import com.asgasd.digital.util.SlugUtil;
import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Cria os dados minimos para a loja funcionar e, opcionalmente, um catalogo
 * demonstrativo claramente identificado como tal.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DataSeeder implements ApplicationRunner {

    private final RoleRepository roles;
    private final UsuarioRepository usuarios;
    private final CategoriaRepository categorias;
    private final ProdutoRepository produtos;
    private final MetodoPagamentoRepository metodos;
    private final ConfiguracaoRepository configuracoes;
    private final PasswordEncoder encoder;

    @Value("${app.seed.demo:true}")
    private boolean semearDemo;

    @Value("${app.seed.admin-email}")
    private String adminEmail;

    @Value("${app.seed.admin-password}")
    private String adminPassword;

    @Value("${app.seed.cliente-email}")
    private String clienteEmail;

    @Value("${app.seed.cliente-password}")
    private String clientePassword;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        Role roleAdmin = roles.findByNome(Role.ADMIN).orElseGet(() -> roles.save(new Role(Role.ADMIN)));
        Role roleCliente = roles.findByNome(Role.CLIENTE).orElseGet(() -> roles.save(new Role(Role.CLIENTE)));

        criarUtilizador(adminEmail, "Administrador Asgasd", adminPassword, roleAdmin, true);
        criarUtilizador(clienteEmail, "Cliente Demonstracao", clientePassword, roleCliente, semearDemo);

        configurarContactos();
        configurarMetodosPagamento();
        if (semearDemo) {
            semearCatalogo();
        }
        log.info("Verificacao de dados iniciais concluida.");
    }

    private void criarUtilizador(String email, String nome, String senha, Role role, boolean criar) {
        if (!criar || usuarios.existsByEmailIgnoreCase(email)) {
            return;
        }
        Usuario usuario = new Usuario();
        usuario.setNome(nome);
        usuario.setEmail(email.toLowerCase());
        usuario.setSenha(encoder.encode(senha));
        usuario.setAtivo(true);
        usuario.adicionarRole(role);
        usuarios.save(usuario);
        log.info("Conta inicial criada: {} ({})", email, role.getNome());
    }

    private void configurarContactos() {
        Map<String, String[]> padroes = new LinkedHashMap<>();
        padroes.put(ConfiguracaoService.TELEFONE, new String[]{"+258 84 000 0000", "Telefone de atendimento"});
        padroes.put(ConfiguracaoService.EMAIL, new String[]{"vendas@asgasd.co.mz", "Email de vendas"});
        padroes.put(ConfiguracaoService.ENDERECO,
                new String[]{"Av. Exemplo 123, Maputo, Mocambique (endereco demonstrativo)", "Endereco da empresa"});
        padroes.put(ConfiguracaoService.WHATSAPP, new String[]{"258840000000", "Numero de WhatsApp (com indicativo)"});
        padroes.put(ConfiguracaoService.HORARIO, new String[]{"Segunda a sexta, 08h00 - 17h00", "Horario de atendimento"});
        padroes.put(ConfiguracaoService.FACEBOOK, new String[]{"https://facebook.com/asgasd", "Pagina de Facebook"});
        padroes.put(ConfiguracaoService.INSTAGRAM, new String[]{"https://instagram.com/asgasd", "Perfil de Instagram"});
        padroes.put(ConfiguracaoService.LINKEDIN, new String[]{"https://linkedin.com/company/asgasd", "Pagina de LinkedIn"});
        padroes.forEach((chave, valores) -> {
            if (configuracoes.findByChave(chave).isEmpty()) {
                configuracoes.save(new Configuracao(chave, valores[0], valores[1]));
            }
        });
    }

    private void configurarMetodosPagamento() {
        if (metodos.count() > 0) {
            return;
        }
        metodos.save(metodo("Transferencia bancaria",
                "Transferencia para a conta da empresa (dados demonstrativos).",
                "Envie a transferencia para o IBAN MZ00 0000 0000 0000 0000 0000 0 (dados demonstrativos) "
                        + "e indique a referencia da encomenda no descritivo. O pagamento e confirmado "
                        + "manualmente pela equipa Asgasd Digital apos verificacao do comprovativo.",
                1));
        metodos.save(metodo("M-Pesa / e-Mola",
                "Pagamento movel (dados demonstrativos).",
                "Envie o valor para o numero 84 000 0000 (dados demonstrativos) e envie o comprovativo "
                        + "atras da confirmacao da encomenda. O pagamento e confirmado manualmente.",
                2));
        metodos.save(metodo("Pagamento na entrega",
                "Pague em dinheiro no momento da entrega.",
                "A encomenda e entregue por um estafeta da Asgasd Digital. O pagamento e feito em dinheiro "
                        + "no momento da entrega, na cidade de Maputo e arredores.",
                3));
    }

    private MetodoPagamento metodo(String nome, String descricao, String instrucoes, int ordem) {
        MetodoPagamento metodo = new MetodoPagamento();
        metodo.setNome(nome);
        metodo.setDescricao(descricao);
        metodo.setInstrucoes(instrucoes);
        metodo.setOrdem(ordem);
        metodo.setAtivo(true);
        return metodo;
    }

    private void semearCatalogo() {
        if (produtos.count() > 0) {
            return;
        }
        Map<String, Categoria> categoriasCriadas = new LinkedHashMap<>();
        List<String[]> dadosCategorias = List.of(
                new String[]{"Informatica", "Computadores, monitores e perifericos"},
                new String[]{"Telemoveis e Tablets", "Smartphones, tablets e acessorios moveis"},
                new String[]{"Audio", "Auscultadores, colunas e equipamento de som"},
                new String[]{"Acessorios", "Perifericos e acessorios para o dia a dia"},
                new String[]{"Casa Inteligente", "Automacao e seguranca para a casa"});
        for (String[] dados : dadosCategorias) {
            Categoria categoria = new Categoria();
            categoria.setNome(dados[0]);
            categoria.setSlug(SlugUtil.slugificar(dados[0]));
            categoria.setDescricao(dados[1]);
            categoria.setAtivo(true);
            categoriasCriadas.put(dados[0], categorias.save(categoria));
        }

        seedProduto("Portatil Asgasd Book 14", "Informatica", new BigDecimal("54900.00"), null,
                "Intel Core i5, 16 GB RAM, SSD 512 GB", 8, true, "/img/produto-portatil.svg",
                "Portatil leve de 14 polegadas para trabalho e estudo, com ecra Full HD, "
                        + "teclado retroiluminado e autonomia ate 10 horas. Produto demonstrativo.");
        seedProduto("Portatil Asgasd Book Pro 15", "Informatica", new BigDecimal("89900.00"), new BigDecimal("82900.00"),
                "Intel Core i7, 16 GB RAM, SSD 1 TB", 5, true, "/img/produto-portatil.svg",
                "Estacao de trabalho movel com grafica dedicada, ideal para edicao e design. "
                        + "Produto demonstrativo.");
        seedProduto("Monitor Asgasd View 24", "Informatica", new BigDecimal("14500.00"), new BigDecimal("12900.00"),
                "24 polegadas, Full HD, 75 Hz", 12, true, "/img/produto-monitor.svg",
                "Monitor IPS de 24 polegadas com bordas finas e ligacao HDMI. Produto demonstrativo.");
        seedProduto("Rato sem fios Asgasd Click", "Acessorios", new BigDecimal("1290.00"), null,
                "Bluetooth 5.1, autonomia 6 meses", 40, true, "/img/produto-rato.svg",
                "Rato ergonomico silencioso com ligacao Bluetooth e recetor USB. Produto demonstrativo.");
        seedProduto("Teclado mecanico Asgasd Type K68", "Acessorios", new BigDecimal("3490.00"), null,
                "Layout 68 teclas, switches azuis", 25, false, "/img/produto-teclado.svg",
                "Teclado compacto com iluminacao RGB e estrutura em aluminio. Produto demonstrativo.");
        seedProduto("Webcam Asgasd Vision HD", "Acessorios", new BigDecimal("2190.00"), null,
                "1080p, microfone integrado", 22, false, "/img/produto-webcam.svg",
                "Webcam Full HD com correcao automatica de luz para reunioes. Produto demonstrativo.");
        seedProduto("Smartphone Asgasd Phone 5G", "Telemoveis e Tablets", new BigDecimal("32900.00"), null,
                "8 GB RAM, 256 GB, camara 64 MP", 15, true, "/img/produto-telemovel.svg",
                "Smartphone 5G com ecra AMOLED de 6,5 polegadas e carregamento rapido. Produto demonstrativo.");
        seedProduto("Tablet Asgasd Tab 10", "Telemoveis e Tablets", new BigDecimal("21900.00"), new BigDecimal("19900.00"),
                "10,4 polegadas, 128 GB, WiFi", 10, false, "/img/produto-tablet.svg",
                "Tablet para leitura, estudo e entretenimento, com caneta compativel. Produto demonstrativo.");
        seedProduto("Auscultadores Asgasd Sound Pro", "Audio", new BigDecimal("4990.00"), null,
                "Cancelamento de ruido, 30 h", 30, true, "/img/produto-auscultadores.svg",
                "Auscultadores over-ear com cancelamento ativo de ruido e microfone. Produto demonstrativo.");
        seedProduto("Coluna Bluetooth Asgasd Boom", "Audio", new BigDecimal("2790.00"), null,
                "20 W, resistencia IPX6", 18, false, "/img/produto-coluna.svg",
                "Coluna portatil com som estereo e resistencia a salpicos. Produto demonstrativo.");
        seedProduto("Lampada inteligente Asgasd Home", "Casa Inteligente", new BigDecimal("1690.00"), null,
                "WiFi, 16 milhoes de cores", 50, true, "/img/produto-lampada.svg",
                "Lampada LED controlada por aplicacao movel e assistentes de voz. Produto demonstrativo.");
        seedProduto("Camara de seguranca Asgasd Guard", "Casa Inteligente", new BigDecimal("5490.00"), new BigDecimal("4990.00"),
                "1080p, visao noturna, WiFi", 14, false, "/img/produto-camara.svg",
                "Camara interior com detecao de movimento e notificacoes no telemovel. Produto demonstrativo.");
        log.info("Catalogo demonstrativo criado com {} produtos.", produtos.count());
    }

    private String gerarSkuUnico(String nome) {
        String base = "ASG-" + SlugUtil.slugificar(nome).toUpperCase().replace("-", "");
        if (base.length() > 40) {
            base = base.substring(0, 40);
        }
        String candidato = base;
        int contador = 2;
        while (produtos.existsBySkuIgnoreCase(candidato)) {
            String sufixo = "-" + contador++;
            candidato = base.substring(0, Math.min(base.length(), 40 - sufixo.length())) + sufixo;
        }
        return candidato;
    }

    private void seedProduto(String nome, String categoria, BigDecimal preco, BigDecimal promocional,
            String curta, int stock, boolean destaque, String imagem, String descricao) {
        Produto produto = new Produto();
        produto.setNome(nome);
        produto.setSlug(SlugUtil.slugificar(nome));
        produto.setSku(gerarSkuUnico(nome));
        produto.setCategoria(categorias.findBySlug(SlugUtil.slugificar(categoria)).orElse(null));
        produto.setPreco(preco);
        produto.setPrecoPromocional(promocional);
        produto.setDescricaoCurta(curta);
        produto.setDescricao(descricao);
        produto.setStock(stock);
        produto.setDestaque(destaque);
        produto.setAtivo(true);
        produto.setImagemUrl(imagem);
        produtos.save(produto);
    }
}
