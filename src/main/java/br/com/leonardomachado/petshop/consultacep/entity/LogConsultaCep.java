package br.com.leonardomachado.petshop.consultacep.entity;

import java.time.Instant;
import java.util.UUID;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import com.fasterxml.jackson.databind.JsonNode;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "log_consulta_cep")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class LogConsultaCep {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 8)
    private String cep;

    @Column(name = "dh_consulta", nullable = false, updatable = false)
    private Instant dataHoraConsulta;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private StatusConsultaCep status;

    @Column(name = "nr_status_http")
    private Integer statusHttp;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "json_resposta_api", columnDefinition = "jsonb")
    private JsonNode jsonRetorno;

    @Column(name = "ds_erro", length = 500)
    private String descricaoErro;

    LogConsultaCep(
            String cep,
            Instant dataHoraConsulta,
            StatusConsultaCep status,
            Integer statusHttp,
            JsonNode jsonRetorno,
            String descricaoErro) {

        this.cep = cep;
        this.dataHoraConsulta = dataHoraConsulta;
        this.status = status;
        this.statusHttp = statusHttp;
        this.jsonRetorno = jsonRetorno;
        this.descricaoErro = descricaoErro;
    }
}
