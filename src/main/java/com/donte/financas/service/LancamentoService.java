package com.donte.financas.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import com.donte.financas.api.dto.AtualizarStatusDTO;
import com.donte.financas.model.entity.Lancamento;
import com.donte.financas.model.enums.StatusLancamento;

public interface LancamentoService {
	
	Lancamento salvar(Lancamento lancamento);
	Lancamento atualizar(Lancamento lancamento);
	void deletar(Lancamento lancamento);
	List<Lancamento> buscar(Lancamento lancamentoFiltro);
	void validar(Lancamento lancamento);
	Optional<Lancamento> obterPorId(Long id);
	void atualizarStatus(Lancamento lancamento, StatusLancamento status);
	Lancamento atualizarStatus(Long codigo, AtualizarStatusDTO dto);
	BigDecimal obterSaldoPorUsuario(Long id);

}
