package br.com.leonardomachado.petshop.consultacep.handler;

import java.net.URI;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import br.com.leonardomachado.petshop.consultacep.exception.ConsultaCepApiException;

@RestControllerAdvice
public class ConsultaCepExceptionHandler {

    @ExceptionHandler(ConsultaCepApiException.class)
    public ProblemDetail tratarConsultaCepApi(ConsultaCepApiException exception) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail( HttpStatus.SERVICE_UNAVAILABLE, "Não foi possível consultar o serviço externo de CEP.");
        problemDetail.setTitle("Serviço de CEP indisponivel no momento, tente mais tarde.");
        problemDetail.setType(
                URI.create("https://petshop-cep-api.dev/errors/cep-provider-unavailable"));
        problemDetail.setProperty("codigo", "CEP_PROVIDER_UNAVAILABLE");
        return problemDetail;
    }
}