package com.asgasd.digital.exception;

/** Lancada quando uma operacao viola uma regra de negocio (stock, duplicados, etc.). */
public class RegraNegocioException extends RuntimeException {

    public RegraNegocioException(String mensagem) {
        super(mensagem);
    }
}
