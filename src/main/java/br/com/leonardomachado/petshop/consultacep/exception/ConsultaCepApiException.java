package br.com.leonardomachado.petshop.consultacep.exception;

public class ConsultaCepApiException extends RuntimeException {

	private static final long serialVersionUID = -7401779694599501902L;

	public ConsultaCepApiException(String message, Throwable cause) {
        super(message, cause);
    }
}