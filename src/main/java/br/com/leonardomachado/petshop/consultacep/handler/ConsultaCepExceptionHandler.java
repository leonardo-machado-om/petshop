package br.com.leonardomachado.petshop.consultacep.handler;

import java.net.URI;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import br.com.leonardomachado.petshop.consultacep.exception.CepInvalidoException;
import br.com.leonardomachado.petshop.consultacep.exception.CepNaoEncontradoException;
import br.com.leonardomachado.petshop.consultacep.exception.ConsultaCepApiException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestControllerAdvice
public class ConsultaCepExceptionHandler {

	@ExceptionHandler(CepInvalidoException.class)
	public ProblemDetail tratarCepInvalido(CepInvalidoException exception, HttpServletRequest request) {

		log.warn("CEP inválido informado. cep = {}, uri={}", exception.getCep(), request.getRequestURI());

		ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, exception.getMessage());

		problemDetail.setTitle("CEP inválido");
		problemDetail.setType(URI.create("urn:petshop:cep-errors:cep-invalido"));
		problemDetail.setProperty("codigo", "CEP_INVALIDO");

		return problemDetail;
	}

	@ExceptionHandler(ConsultaCepApiException.class)
	public ProblemDetail tratarConsultaCepApi(ConsultaCepApiException exception, HttpServletRequest request) {

		log.warn("Falha na consulta da API externa de CEP. uri={}, statusHttp={}, mensagem={}", request.getRequestURI(),
				exception.getStatusHttp(), exception.getMessage());

		ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.SERVICE_UNAVAILABLE,
				"Não foi possível consultar o serviço externo de CEP.");
		problemDetail.setTitle("Serviço de CEP indisponível no momento, tente mais tarde.");
		problemDetail.setType(URI.create("urn:petshop:cep-errors:servidor-indisponivel"));
		problemDetail.setProperty("codigo", "CEP_PROVIDER_UNAVAILABLE");
		return problemDetail;
	}

	@ExceptionHandler(CepNaoEncontradoException.class)
	public ProblemDetail tratarCepNaoEncontrado(CepNaoEncontradoException exception, HttpServletRequest request) {

		log.info("CEP não encontrado. cep={}, uri={}", exception.getCep(), request.getRequestURI());

		ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, exception.getMessage());

		problemDetail.setTitle("CEP não encontrado");
		problemDetail.setType(URI.create("urn:petshop:cep-errors:cep-nao-encontrado"));
		problemDetail.setProperty("codigo", "CEP_NAO_ENCONTRADO");

		return problemDetail;
	}

	@ExceptionHandler(NoResourceFoundException.class)
	public ResponseEntity<Void> tratarRecursoNaoEncontrado(NoResourceFoundException exception) {

		return ResponseEntity.notFound().build();
	}

	@ExceptionHandler(Exception.class)
	public ProblemDetail tratarErroInesperado(Exception exception, HttpServletRequest request) {

		log.error("Erro inesperado ao processar requisição. uri={}", request.getRequestURI(), exception);

		ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR,
				"Ocorreu um erro interno. Tente novamente mais tarde.");

		problemDetail.setTitle("Erro interno do servidor");
		problemDetail.setType(URI.create("urn:petshop:cep-errors:internal-server-error"));

		return problemDetail;
	}
}