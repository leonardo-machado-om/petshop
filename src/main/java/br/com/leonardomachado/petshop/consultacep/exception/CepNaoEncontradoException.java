package br.com.leonardomachado.petshop.consultacep.exception;

public class CepNaoEncontradoException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	private final String cep;

	public CepNaoEncontradoException(String cep) {
		super("CEP " + cep + " não encontrado.");
		this.cep = cep;
	}

	public String getCep() {
		return this.cep;
	}
}