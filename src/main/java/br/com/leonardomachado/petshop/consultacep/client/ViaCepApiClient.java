package br.com.leonardomachado.petshop.consultacep.client;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

import com.fasterxml.jackson.databind.JsonNode;

import br.com.leonardomachado.petshop.consultacep.exception.ConsultaCepApiException;

@Component
public class ViaCepApiClient implements CepApiClient {

	private final RestClient restClient;
	
	public ViaCepApiClient(
	        @Qualifier("cepApiRestClient") RestClient restClient) {
	    this.restClient = restClient;
	}

	@Override
	public ResponseEntity<JsonNode> consultar(String cep) {
	    try {
	        ResponseEntity<JsonNode> response = restClient.get()
	                .uri("/ws/{cep}/json", cep)
	                .accept(MediaType.APPLICATION_JSON)
	                .retrieve()
	                .toEntity(JsonNode.class);

	        JsonNode jsonRetorno = response.getBody();

	        if (jsonRetorno == null || jsonRetorno.isNull()) {
	            throw new ConsultaCepApiException(
	                    "A API externa retornou uma resposta vazia.");
	        }

	        return response;

	    } catch (RestClientResponseException exception) {
	        throw new ConsultaCepApiException(
	                "A API externa retornou um erro.",
	                exception.getStatusCode().value(),
	                exception);

	    } catch (RestClientException exception) {
	        throw new ConsultaCepApiException(
	                "Não foi possível consultar o serviço externo de CEP.",
	                exception);
	    }
	}
}