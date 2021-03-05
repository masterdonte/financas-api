package com.donte.financas.model.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.donte.financas.model.entity.Lancamento;

public interface LancamentoRepository extends JpaRepository<Lancamento, Long> {

}
