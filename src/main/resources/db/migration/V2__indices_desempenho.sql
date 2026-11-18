-- Indices para as consultas mais usadas pela loja e pelo painel administrativo.

CREATE INDEX idx_produtos_categoria ON produtos (categoria_id);
CREATE INDEX idx_produtos_ativo ON produtos (ativo);
CREATE INDEX idx_produtos_nome ON produtos (lower(nome));

CREATE INDEX idx_encomendas_usuario ON encomendas (usuario_id);
CREATE INDEX idx_encomendas_status ON encomendas (status);
CREATE INDEX idx_encomendas_criado ON encomendas (criado_em DESC);

CREATE INDEX idx_itens_encomenda_produto ON itens_encomenda (produto_id);
CREATE INDEX idx_itens_carrinho_carrinho ON itens_carrinho (carrinho_id);

CREATE INDEX idx_contactos_lida ON contactos (lida);
CREATE INDEX idx_reset_tokens_usuario ON password_reset_tokens (usuario_id);
