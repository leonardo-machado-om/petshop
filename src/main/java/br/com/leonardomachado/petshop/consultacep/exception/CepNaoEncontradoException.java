package br.com.leonardomachado.petshop.consultacep.exception;

public class CepNaoEncontradoException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public CepNaoEncontradoException(String cep) {
        super("CEP " + cep + " não encontrado.");
    }
}
