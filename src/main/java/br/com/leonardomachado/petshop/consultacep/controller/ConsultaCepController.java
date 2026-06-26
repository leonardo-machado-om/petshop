package br.com.leonardomachado.petshop.consultacep.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.leonardomachado.petshop.consultacep.dto.CepApiResponse;
import br.com.leonardomachado.petshop.consultacep.service.ConsultaCepService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/ceps")
@Tag(name = "Consulta de CEP")
public class ConsultaCepController {

    private final ConsultaCepService consultaCepService;

    public ConsultaCepController(ConsultaCepService consultaCepService) {
        this.consultaCepService = consultaCepService;
    }

    @GetMapping("/{cep}")
    @Operation(summary = "Consulta um CEP e registra o log da operação")
    public ResponseEntity<CepApiResponse> consultar(
            @PathVariable("cep") String cep) {

        return ResponseEntity.ok(consultaCepService.consultar(cep));
    }
}
