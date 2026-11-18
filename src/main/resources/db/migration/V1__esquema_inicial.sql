-- ===========================================================================
-- Asgasd Digital - esquema inicial (V1)
-- Gerido por Flyway. As entidades JPA (ddl-auto=validate) validam este esquema.
-- ===========================================================================

CREATE TABLE roles (
    id       BIGSERIAL PRIMARY KEY,
    nome     VARCHAR(40) NOT NULL UNIQUE
);

CREATE TABLE usuarios (
    id           BIGSERIAL PRIMARY KEY,
    nome         VARCHAR(120) NOT NULL,
    email        VARCHAR(160) NOT NULL UNIQUE,
    senha        VARCHAR(100) NOT NULL,
    telefone     VARCHAR(30),
    ativo        BOOLEAN      NOT NULL DEFAULT TRUE,
    criado_em    TIMESTAMP    NOT NULL,
    atualizado_em TIMESTAMP   NOT NULL
);

CREATE TABLE usuario_roles (
    usuario_id BIGINT NOT NULL REFERENCES usuarios (id) ON DELETE CASCADE,
    role_id    BIGINT NOT NULL REFERENCES roles (id) ON DELETE CASCADE,
    PRIMARY KEY (usuario_id, role_id)
);

CREATE TABLE enderecos (
    id            BIGSERIAL PRIMARY KEY,
    usuario_id    BIGINT       NOT NULL REFERENCES usuarios (id) ON DELETE CASCADE,
    provincia     VARCHAR(60),
    cidade        VARCHAR(60),
    bairro        VARCHAR(80),
    rua           VARCHAR(160),
    referencia    VARCHAR(160),
    codigo_postal VARCHAR(20),
    principal     BOOLEAN      NOT NULL DEFAULT FALSE,
    criado_em     TIMESTAMP    NOT NULL,
    atualizado_em TIMESTAMP    NOT NULL
);

CREATE TABLE categorias (
    id            BIGSERIAL PRIMARY KEY,
    nome          VARCHAR(80)  NOT NULL UNIQUE,
    slug          VARCHAR(90)  NOT NULL UNIQUE,
    descricao     VARCHAR(255),
    ativo         BOOLEAN      NOT NULL DEFAULT TRUE,
    criado_em     TIMESTAMP    NOT NULL,
    atualizado_em TIMESTAMP    NOT NULL
);

CREATE TABLE produtos (
    id                BIGSERIAL PRIMARY KEY,
    nome              VARCHAR(150)   NOT NULL,
    slug              VARCHAR(170)   NOT NULL UNIQUE,
    descricao_curta   VARCHAR(300),
    descricao         VARCHAR(2000),
    preco             NUMERIC(12, 2) NOT NULL,
    preco_promocional NUMERIC(12, 2),
    sku               VARCHAR(40)    UNIQUE,
    imagem_url        VARCHAR(500),
    stock             INTEGER        NOT NULL DEFAULT 0,
    destaque          BOOLEAN        NOT NULL DEFAULT FALSE,
    ativo             BOOLEAN        NOT NULL DEFAULT TRUE,
    categoria_id      BIGINT         REFERENCES categorias (id),
    criado_em         TIMESTAMP      NOT NULL,
    atualizado_em     TIMESTAMP      NOT NULL
);

CREATE TABLE carrinhos (
    id            BIGSERIAL PRIMARY KEY,
    usuario_id    BIGINT UNIQUE REFERENCES usuarios (id) ON DELETE CASCADE,
    token         VARCHAR(64) UNIQUE,
    criado_em     TIMESTAMP NOT NULL,
    atualizado_em TIMESTAMP NOT NULL,
    CONSTRAINT carrinho_dono_obrigatorio CHECK (usuario_id IS NOT NULL OR token IS NOT NULL)
);

CREATE TABLE itens_carrinho (
    id              BIGSERIAL PRIMARY KEY,
    carrinho_id     BIGINT         NOT NULL REFERENCES carrinhos (id) ON DELETE CASCADE,
    produto_id      BIGINT         NOT NULL REFERENCES produtos (id),
    quantidade      INTEGER        NOT NULL CHECK (quantidade > 0),
    preco_unitario  NUMERIC(12, 2) NOT NULL,
    criado_em       TIMESTAMP      NOT NULL,
    atualizado_em   TIMESTAMP      NOT NULL,
    CONSTRAINT item_carrinho_unico UNIQUE (carrinho_id, produto_id)
);

CREATE TABLE encomendas (
    id                 BIGSERIAL PRIMARY KEY,
    referencia         VARCHAR(30)    NOT NULL UNIQUE,
    usuario_id         BIGINT         REFERENCES usuarios (id),
    status             VARCHAR(20)    NOT NULL,
    subtotal           NUMERIC(12, 2) NOT NULL,
    custo_envio        NUMERIC(12, 2) NOT NULL,
    total              NUMERIC(12, 2) NOT NULL,
    nome_contacto      VARCHAR(120)   NOT NULL,
    telefone_contacto  VARCHAR(30)    NOT NULL,
    email_contacto     VARCHAR(160)   NOT NULL,
    endereco_entrega   VARCHAR(600)   NOT NULL,
    notas              VARCHAR(500),
    criado_em          TIMESTAMP      NOT NULL,
    atualizado_em      TIMESTAMP      NOT NULL
);

CREATE TABLE itens_encomenda (
    id              BIGSERIAL PRIMARY KEY,
    encomenda_id    BIGINT         NOT NULL REFERENCES encomendas (id) ON DELETE CASCADE,
    produto_id      BIGINT         REFERENCES produtos (id),
    nome_produto    VARCHAR(150)   NOT NULL,
    preco_unitario  NUMERIC(12, 2) NOT NULL,
    quantidade      INTEGER        NOT NULL CHECK (quantidade > 0),
    subtotal        NUMERIC(12, 2) NOT NULL,
    criado_em       TIMESTAMP      NOT NULL,
    atualizado_em   TIMESTAMP      NOT NULL
);

CREATE TABLE metodos_pagamento (
    id            BIGSERIAL PRIMARY KEY,
    nome          VARCHAR(80) NOT NULL UNIQUE,
    descricao     VARCHAR(255),
    instrucoes    VARCHAR(1200),
    ativo         BOOLEAN     NOT NULL DEFAULT TRUE,
    ordem         INTEGER     NOT NULL DEFAULT 0,
    criado_em     TIMESTAMP   NOT NULL,
    atualizado_em TIMESTAMP   NOT NULL
);

CREATE TABLE pagamentos (
    id                  BIGSERIAL PRIMARY KEY,
    encomenda_id        BIGINT         NOT NULL UNIQUE REFERENCES encomendas (id) ON DELETE CASCADE,
    metodo_pagamento_id BIGINT         REFERENCES metodos_pagamento (id),
    status              VARCHAR(20)    NOT NULL,
    valor               NUMERIC(12, 2) NOT NULL,
    referencia_externa  VARCHAR(120),
    instrucoes          VARCHAR(1200),
    confirmado_em       TIMESTAMP,
    criado_em           TIMESTAMP      NOT NULL,
    atualizado_em       TIMESTAMP      NOT NULL
);

CREATE TABLE contactos (
    id            BIGSERIAL PRIMARY KEY,
    nome          VARCHAR(120) NOT NULL,
    email         VARCHAR(160) NOT NULL,
    telefone      VARCHAR(30),
    assunto       VARCHAR(150) NOT NULL,
    mensagem      VARCHAR(2000) NOT NULL,
    lida          BOOLEAN      NOT NULL DEFAULT FALSE,
    usuario_id    BIGINT       REFERENCES usuarios (id) ON DELETE SET NULL,
    criado_em     TIMESTAMP    NOT NULL,
    atualizado_em TIMESTAMP    NOT NULL
);

CREATE TABLE configuracoes (
    id            BIGSERIAL PRIMARY KEY,
    chave         VARCHAR(60) NOT NULL UNIQUE,
    valor         VARCHAR(1000),
    descricao     VARCHAR(200),
    criado_em     TIMESTAMP   NOT NULL,
    atualizado_em TIMESTAMP   NOT NULL
);

CREATE TABLE password_reset_tokens (
    id            BIGSERIAL PRIMARY KEY,
    usuario_id    BIGINT      NOT NULL REFERENCES usuarios (id) ON DELETE CASCADE,
    token         VARCHAR(64) NOT NULL UNIQUE,
    expira_em     TIMESTAMP   NOT NULL,
    usado_em      TIMESTAMP,
    criado_em     TIMESTAMP   NOT NULL,
    atualizado_em TIMESTAMP   NOT NULL
);
