package br.com.leonardomachado.petshop.consultacep.exception;

public class CepInvalidoException extends RuntimeException {

	private static final long serialVersionUID = -7861318453302674173L;

	private final String cep;
	
	public CepInvalidoException(String message, String cep) {
        super(message);
        this.cep = cep;
    }
	
	public String getCep() {
		return this.cep;
	}

}