package br.com.leonardomachado.petshop.consultacep.client;

import org.springframework.http.ResponseEntity;

import com.fasterxml.jackson.databind.JsonNode;

public interface CepApiClient {
    ResponseEntity<JsonNode> consultar(String cep);
}