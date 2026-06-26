package br.com.leonardomachado.petshop.consultacep.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.leonardomachado.petshop.consultacep.entity.LogConsultaCep;

public interface LogConsultaCepRepository extends JpaRepository<LogConsultaCep, UUID> {}
