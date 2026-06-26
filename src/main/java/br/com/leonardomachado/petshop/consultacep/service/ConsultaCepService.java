package br.com.leonardomachado.petshop.consultacep.service;

import java.time.Clock;
import java.time.Instant;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import br.com.leonardomachado.petshop.consultacep.client.CepApiClient;
import br.com.leonardomachado.petshop.consultacep.dto.CepApiResponse;
import br.com.leonardomachado.petshop.consultacep.entity.LogConsultaCepFactory;
import br.com.leonardomachado.petshop.consultacep.exception.CepInvalidoException;
import br.com.leonardomachado.petshop.consultacep.exception.CepNaoEncontradoException;
import br.com.leonardomachado.petshop.consultacep.exception.ConsultaCepApiException;
import br.com.leonardomachado.petshop.consultacep.repository.LogConsultaCepRepository;

@Service
public class ConsultaCepService {

    private final CepApiClient cepApiClient;
    private final LogConsultaCepRepository logConsultaCepRepository;
    private final ObjectMapper objectMapper;
    private final Clock clock;

    public ConsultaCepService(
            CepApiClient cepApiClient,
            LogConsultaCepRepository logConsultaCepRepository,
            ObjectMapper objectMapper,
            Clock clock) {

        this.cepApiClient = cepApiClient;
        this.logConsultaCepRepository = logConsultaCepRepository;
        this.objectMapper = objectMapper;
        this.clock = clock;
    }

    public CepApiResponse consultar(String cepInformado) {
        String cep = validarEFormatarCep(cepInformado);
        Instant dataHoraConsulta = Instant.now(clock);

        try {
            ResponseEntity<JsonNode> response = cepApiClient.consultar(cep);

            JsonNode jsonRetorno = response.getBody();

            if (jsonRetorno == null || jsonRetorno.isNull()) {
                throw new ConsultaCepApiException(
                        "A API externa retornou uma resposta vazia.");
            }

            CepApiResponse resposta = objectMapper.treeToValue(
                    jsonRetorno,
                    CepApiResponse.class);

            if (resposta.cepNaoEncontrado()) {
                logConsultaCepRepository.save(
                        LogConsultaCepFactory.criarCepNaoEncontrado(
                                cep,
                                dataHoraConsulta,
                                response.getStatusCode().value(),
                                jsonRetorno));

                throw new CepNaoEncontradoException(cep);
            }

            logConsultaCepRepository.save(
                    LogConsultaCepFactory.criarSucesso(
                            cep,
                            dataHoraConsulta,
                            response.getStatusCode().value(),
                            jsonRetorno));

            return resposta;

        } catch (ConsultaCepApiException exception) {
            logConsultaCepRepository.save(
                    LogConsultaCepFactory.criarErroApiExterna(
                            cep,
                            dataHoraConsulta,
                            exception.getStatusHttp(),
                            exception.getMessage()));

            throw exception;

        } catch (JsonProcessingException exception) {
            ConsultaCepApiException apiException =
                    new ConsultaCepApiException(
                            "Não foi possível interpretar a resposta da API externa.",
                            null,
                            exception);

            logConsultaCepRepository.save(
                    LogConsultaCepFactory.criarErroApiExterna(
                            cep,
                            dataHoraConsulta,
                            null,
                            apiException.getMessage()));

            throw apiException;
        }
    }

    private String validarEFormatarCep(String cep) {
        if (cep == null || cep.isBlank()) {
            throw new CepInvalidoException("CEP é obrigatório.", cep);
        }

        String cepLimpo = cep.trim();

        if (!cepLimpo.matches("^\\d{5}-?\\d{3}$")) {
            throw new CepInvalidoException(
                    "O CEP deve possuir exatamente 8 dígitos.", cep);
        }

        return cepLimpo.replace("-", "");
    }

}
