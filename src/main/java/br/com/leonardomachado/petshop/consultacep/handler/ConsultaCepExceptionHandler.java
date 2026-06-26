package br.com.leonardomachado.petshop.consultacep.handler;

import java.net.URI;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import br.com.leonardomachado.petshop.consultacep.exception.CepInvalidoException;
import br.com.leonardomachado.petshop.consultacep.exception.CepNaoEncontradoException;
import br.com.leonardomachado.petshop.consultacep.exception.ConsultaCepApiException;

@RestControllerAdvice
public class ConsultaCepExceptionHandler {

	@ExceptionHandler(CepInvalidoException.class)
	public ProblemDetail tratarCepInvalido(CepInvalidoException exception) {

	    ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
	            HttpStatus.BAD_REQUEST,
	            exception.getMessage());

	    problemDetail.setTitle("CEP inválido");
	    problemDetail.setProperty("codigo", "CEP_INVALIDO");

	    return problemDetail;
	}
	
    @ExceptionHandler(ConsultaCepApiException.class)
    public ProblemDetail tratarConsultaCepApi(ConsultaCepApiException exception) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail( HttpStatus.SERVICE_UNAVAILABLE, "Não foi possível consultar o serviço externo de CEP.");
        problemDetail.setTitle("Serviço de CEP indisponivel no momento, tente mais tarde.");
        problemDetail.setType(
        		URI.create("urn:petshop:errors:cep-provider-unavailable"));
        problemDetail.setProperty("codigo", "CEP_PROVIDER_UNAVAILABLE");
        return problemDetail;
    }
    
    @ExceptionHandler(CepNaoEncontradoException.class)
    public ProblemDetail tratarCepNaoEncontrado(
            CepNaoEncontradoException exception) {

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.NOT_FOUND,
                exception.getMessage());

        problemDetail.setTitle("CEP não encontrado");
        problemDetail.setProperty("codigo", "CEP_NAO_ENCONTRADO");

        return problemDetail;
    }
}