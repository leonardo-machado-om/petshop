package br.com.leonardomachado.petshop.consultacep.exception;

public class ConsultaCepApiException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private final Integer statusHttp;

    public ConsultaCepApiException(String message) {
        this(message, null, null);
    }

    public ConsultaCepApiException(String message, Throwable cause) {
        this(message, null, cause);
    }

    public ConsultaCepApiException(
            String message,
            Integer statusHttp,
            Throwable cause) {

        super(message, cause);
        this.statusHttp = statusHttp;
    }

    public Integer getStatusHttp() {
        return statusHttp;
    }
}