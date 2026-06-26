package br.com.leonardomachado.petshop.consultacep.entity;

import java.time.Instant;
import java.util.Objects;

import com.fasterxml.jackson.databind.JsonNode;

import br.com.leonardomachado.petshop.consultacep.exception.CepInvalidoException;

public final class LogConsultaCepFactory {

	private LogConsultaCepFactory() {
        throw new IllegalStateException(
                "Classe utilitária não deve ser instanciada.");
    }

	public static LogConsultaCep criarSucesso(String cep, Instant dataHoraConsulta, Integer statusHttp,
			JsonNode jsonRetorno) {

		return new LogConsultaCep(validarCep(cep),
				Objects.requireNonNull(dataHoraConsulta, "A data da consulta é obrigatória."),
				StatusConsultaCep.SUCESSO, Objects.requireNonNull(statusHttp, "O status HTTP é obrigatório."),
				Objects.requireNonNull(jsonRetorno, "O retorno da API é obrigatório."), null);
	}

	public static LogConsultaCep criarCepNaoEncontrado(String cep, Instant dataHoraConsulta, Integer statusHttp,
			JsonNode jsonRetorno) {

		return new LogConsultaCep(validarCep(cep),
				Objects.requireNonNull(dataHoraConsulta, "A data da consulta é obrigatória."),
				StatusConsultaCep.CEP_NAO_ENCONTRADO,
				Objects.requireNonNull(statusHttp, "O status HTTP é obrigatório."),
				Objects.requireNonNull(jsonRetorno, "O retorno da API é obrigatório."), null);
	}

	public static LogConsultaCep criarErroApiExterna(String cep, Instant dataHoraConsulta, Integer statusHttp,
			String descricaoErro) {

		return new LogConsultaCep(validarCep(cep),
				Objects.requireNonNull(dataHoraConsulta, "A data da consulta é obrigatória."),
				StatusConsultaCep.ERRO_API_EXTERNA, statusHttp, null,
				Objects.requireNonNull(descricaoErro, "A descrição do erro é obrigatória."));
	}

	private static String validarCep(String cep) {
		String cepNormalizado = Objects.requireNonNull(cep, "O CEP é obrigatório.").replaceAll("\\D", "");

		if (cepNormalizado.length() != 8) {
			throw new CepInvalidoException("O CEP deve possuir 8 dígitos.", cep);
		}

		return cepNormalizado;
	}
}