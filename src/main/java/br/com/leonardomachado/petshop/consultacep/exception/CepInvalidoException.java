package br.com.leonardomachado.petshop.consultacep.exception;

public class CepInvalidoException extends RuntimeException {

	private static final long serialVersionUID = -7861318453302674173L;

	public CepInvalidoException(String message) {
        super(message);
    }
}
