# Asgasd Digital

Loja online completa da **Asgasd Digital** — tecnologia, acessórios e serviços digitais em Moçambique.
Aplicação web monolítica em **Java 21 + Spring Boot 3.3 + Thymeleaf + PostgreSQL**, sem frameworks JavaScript de frontend.

> Estado: aplicação funcional de ponta a ponta (catálogo, carrinho, encomendas, conta de cliente e painel administrativo), validada com 40 testes de integração contra PostgreSQL real e com um teste manual de todos os fluxos HTTP.

---

## 1. Funcionalidades

### Loja (público)
- **Homepage** com destaque, categorias, produtos em promoção, vantagens da loja e chamada de contacto.
- **Catálogo** (`/produtos`) com pesquisa por nome/descrição, filtro por categoria, faixa de preço, disponibilidade em stock, ordenação (relevância, preço, novidade, nome) e paginação.
- **Página de produto** com galeria, descrição, stock em tempo real, produtos relacionados e botão de adicionar ao carrinho.
- **Carrinho** persistido em base de dados: funciona para visitantes (associado à sessão) e é automaticamente fundido com o carrinho da conta após o login.
- **Contacto** com formulário validado, links de WhatsApp/telefone/email e morada, a partir das configurações da loja.
- **Consulta de encomenda** por referência + email, sem necessidade de conta.

### Cliente
- Registo, login, logout, recuperação e redefinição de palavra-passe (token com validade).
- Perfil (dados pessoais e endereços) e alteração de palavra-passe.
- Checkout com dados de entrega, escolha de método de pagamento e cálculo de portes (250 MT, grátis acima de 5 000 MT).
- Histórico de encomendas com referência e estado, detalhe da encomenda, registo de comprovativo de pagamento e cancelamento (com reposição de stock).

### Administração (`/admin`, apenas ROLE_ADMIN)
- Painel com indicadores: vendas, encomendas, clientes, produtos com stock baixo e mais vendidos.
- CRUD de produtos (com upload de imagem), CRUD de categorias (com proteção contra apagar categorias com produtos).
- Gestão de encomendas: filtro por estado, alteração de estado, **confirmação manual de pagamentos**.
- Ficha de cliente com histórico e totais; mensagens de contacto com marcação de lida/não lida.
- Configurações da loja (nome, contactos, WhatsApp, morada, portes, redes sociais) e métodos de pagamento.

### Pagamentos
Não existe integração com gateway: os pagamentos são **manuais e configuráveis** (transferência bancária, M-Pesa/e-Mola, pagamento na entrega). O cliente registra o comprovativo e o administrador confirma o pagamento. Nada é simulado como se fosse um pagamento real.

---

## 2. Stack

| Camada | Tecnologia |
|---|---|
| Linguagem | Java 21 |
| Framework | Spring Boot 3.3.5 (MVC, Data JPA/Hibernate, Security, Validation, Mail) |
| Vistas | Thymeleaf + Bootstrap 5.3.3 + Bootstrap Icons (via WebJars, sem CDN) |
| Base de dados | PostgreSQL 16, migrações com Flyway (`ddl-auto: validate`) |
| Build | Maven |
| Testes | JUnit 5, Spring Boot Test, MockMvc, AssertJ (PostgreSQL real) |

---

## 3. Arquitetura

```
src/main/java/com/asgasd/digital/
├── config/       Segurança (BCrypt, CSRF, rotas), WebConfig, DataSeeder
├── model/        Entidades JPA e enums (Usuario, Role, Produto, Categoria, Carrinho,
│                 ItemCarrinho, Encomenda, ItemEncomenda, Pagamento, MetodoPagamento,
│                 Endereco, Contacto, Configuracao, PasswordResetToken, ...)
├── repository/   Spring Data JPA
├── service/      Regras de negócio (Carrinho, Encomenda, Produto, Usuario, ...)
├── dto/          Formulários e filtros validados
├── exception/    RegraNegocioException, RecursoNaoEncontradoException + handler global
├── util/         Slug, formatação de moeda
└── web/          Controllers públicos + web/admin (painel)
```

Entidades mínimas implementadas: `Usuario`, `Role`, `Produto`, `Categoria`, `Carrinho`, `ItemCarrinho`, `Encomenda`, `ItemEncomenda`, `Pagamento`, `Endereco`, `Contacto` (mais `MetodoPagamento`, `Configuracao` e `PasswordResetToken`).

Esquema gerido por Flyway:
- `V1__esquema_inicial.sql` — tabelas, chaves e restrições
- `V2__indices_desempenho.sql` — índices de pesquisa e listagens

---

## 4. Requisitos

- JDK 21
- Maven 3.9+
- PostgreSQL 14+ (testado com 16)

---

## 5. Configuração

Copie o exemplo e ajuste os valores:

```bash
cp .env.example .env
```

| Variável | Descrição | Obrigatória |
|---|---|---|
| `DATABASE_URL` | URL JDBC (`jdbc:postgresql://…`) **ou** URI `postgres://utilizador:password@host/base`, convertida automaticamente | sim |
| `DATABASE_USERNAME` / `DATABASE_PASSWORD` | Credenciais da base de dados | sim |
| `PORT` | Porta HTTP (predefinido 8080) | não |
| `APP_BASE_URL` | URL pública usada nos links de email | recomendada |
| `UPLOAD_DIR` | Pasta das imagens carregadas no painel | não |
| `ADMIN_EMAIL` / `ADMIN_PASSWORD` | Administrador criado no primeiro arranque | recomendada |
| `CLIENTE_EMAIL` / `CLIENTE_PASSWORD` | Cliente demonstrativo | não |
| `SEED_DEMO` | Semear catálogo de demonstração (`true`/`false`) | não |
| `MAIL_*` | SMTP para recuperação de palavra-passe (opcional) | não |

Sem SMTP configurado, o link de recuperação é escrito no log da aplicação — a recuperação continua utilizável.

**Nunca faça commit de credenciais.** O `.gitignore` exclui `.env*` (exceto `.env.example`).

### Base de dados

```sql
CREATE DATABASE asgasd;
CREATE DATABASE asgasd_test;  -- usada pelos testes
CREATE USER asgasd WITH PASSWORD 'a-sua-password';
GRANT ALL PRIVILEGES ON DATABASE asgasd TO asgasd;
GRANT ALL PRIVILEGES ON DATABASE asgasd_test TO asgasd;
```

O Flyway cria as tabelas automaticamente no primeiro arranque.

---

## 6. Execução local

```bash
# build
mvn clean package

# executar
java -jar target/asgasd-digital.jar
# ou
mvn spring-boot:run
```

Aplicação em **http://localhost:8080**.

### Credenciais de demonstração (apenas ambiente local)

| Perfil | Email | Palavra-passe |
|---|---|---|
| Administrador | `admin@asgasd.co.mz` | `Admin@2026` |
| Cliente | `cliente@exemplo.co.mz` | `Cliente@2026` |

Em produção defina `ADMIN_PASSWORD`/`CLIENTE_PASSWORD` e `SEED_DEMO=false` depois de semear o catálogo pretendido.

---

## 7. Testes

Os testes correm contra um PostgreSQL real (não H2), validando o mesmo esquema e as mesmas migrações da produção.

```bash
export TEST_DATABASE_URL=jdbc:postgresql://localhost:5432/asgasd_test
export TEST_DATABASE_USERNAME=asgasd
export TEST_DATABASE_PASSWORD=a-sua-password
mvn test
```

Resultado atual: **40 testes, 0 falhas** — autenticação e segurança, catálogo e filtros, carrinho e checkout, encomendas (stock, cancelamento, pagamento), administração, contacto e recuperação de palavra-passe.

---

## 8. Docker

```bash
docker compose up --build
```

Sobe PostgreSQL 16 + aplicação em `http://localhost:8080` (perfil `prod`). A base de dados é persistida no volume `asgasd-db-data`.

Apenas a aplicação:

```bash
docker build -t asgasd-digital .
docker run -p 8080:8080 \
  -e DATABASE_URL=jdbc:postgresql://host:5432/asgasd \
  -e DATABASE_USERNAME=asgasd -e DATABASE_PASSWORD=... \
  -e APP_BASE_URL=https://o-seu-dominio \
  asgasd-digital
```

O contentor corre como utilizador não-root e expõe `PORT` (predefinido 8080).

---

## 9. Deploy

### Render (Blueprint)
`render.yaml` descreve o serviço web (Docker) e uma base PostgreSQL. O Render injeta `DATABASE_URL` como URI `postgres://…` (`fromDatabase: connectionString`) e a aplicação converte-a para JDBC no arranque — não é preciso montar a URL à mão.

1. No Render: **New → Blueprint** e escolha o repositório `hersonbilajr/asgasd-digital`.
2. Preencha `APP_BASE_URL` com o URL do serviço (ex. `https://asgasd-digital.onrender.com`).
3. `ADMIN_PASSWORD` e `CLIENTE_PASSWORD` são gerados automaticamente; veja-os em **Environment** e altere-os depois do primeiro login.

### Neon (PostgreSQL gratuito) + qualquer host de contentores
1. Crie um projeto no Neon e copie a connection string (`postgresql://…?sslmode=require`).
2. Defina `DATABASE_URL` com essa URI tal como está — é convertida automaticamente.
3. Defina também `APP_BASE_URL` e `SPRING_PROFILES_ACTIVE=prod`.

### Servidor próprio
`java -jar target/asgasd-digital.jar --spring.profiles.active=prod` atrás de um reverse proxy com HTTPS (`server.forward-headers-strategy=framework` já está ativo).

---

## 10. Segurança

- Palavras-passe com **BCrypt** (custo 10); nunca em texto simples.
- **CSRF** ativo em todos os formulários; tokens incluídos nos templates.
- `/admin/**` restrito a `ROLE_ADMIN`; `/checkout`, `/conta/**` e `/encomenda/**` exigem autenticação.
- Utilizador autenticado resolvido sempre pela sessão/email — nunca por parâmetros do pedido.
- Consultas de encomendas sempre filtradas pelo proprietário (isolamento entre contas).
- Credenciais apenas por variáveis de ambiente; mensagens de erro genéricas em produção.
- Sessão invalidada no logout; cookies `HttpOnly`; uploads limitados a 3 MB e validados por tipo.

---

## 11. Limitações conhecidas

- Pagamentos são manuais (por decisão de projeto): não há gateway real nem simulação de cobrança.
- Envio de email depende de SMTP configurado; sem ele os links são registados no log.
- Imagens carregadas ficam no sistema de ficheiros (`UPLOAD_DIR`); em vários nós seria necessário armazenamento de objetos.

---

## 12. Licença

Projeto desenvolvido para a Asgasd Digital. Uso interno.
