package br.com.leonardomachado.petshop.consultacep.config;

import java.net.URI;
import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.NotNull;

@Validated
@ConfigurationProperties(prefix = "app.cep")
public record CepApiProperties(

        @NotNull
        URI baseUrl,

        @NotNull
        Duration connectTimeout,

        @NotNull
        Duration readTimeout) {
}