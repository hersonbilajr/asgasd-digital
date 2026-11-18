package com.asgasd.digital.exception;

/** Lancada quando um recurso pedido nao existe. */
public class RecursoNaoEncontradoException extends RuntimeException {

    public RecursoNaoEncontradoException(String mensagem) {
        super(mensagem);
    }
}
