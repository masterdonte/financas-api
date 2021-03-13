package com.donte.financas.service.impl;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.data.domain.ExampleMatcher.StringMatcher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.donte.financas.api.dto.AtualizarStatusDTO;
import com.donte.financas.exception.RegraNegocioException;
import com.donte.financas.model.entity.Lancamento;
import com.donte.financas.model.enums.StatusLancamento;
import com.donte.financas.model.enums.TipoLancamento;
import com.donte.financas.model.repository.LancamentoRepository;
import com.donte.financas.service.LancamentoService;

@Service
public class LancamentoServiceImpl implements LancamentoService{
	
	private LancamentoRepository repository;

	public LancamentoServiceImpl(LancamentoRepository repository) {
		this.repository = repository;
	}

	@Override
	@Transactional
	public Lancamento salvar(Lancamento lancamento) {
		validar(lancamento);
		lancamento.setStatus(StatusLancamento.PENDENTE);
		return repository.save(lancamento);
	}

	@Override
	@Transactional
	public Lancamento atualizar(Lancamento lancamento) {
		Objects.requireNonNull(lancamento.getId());
		validar(lancamento);
		return repository.save(lancamento);
	}

	@Override
	@Transactional
	public void deletar(Lancamento lancamento) {
		Objects.requireNonNull(lancamento.getId());
		repository.delete(lancamento);
	}

	@Override
	@Transactional(readOnly = true)
	public List<Lancamento> buscar(Lancamento lancamentoFiltro) {
		Example<Lancamento> example = Example.of(lancamentoFiltro, ExampleMatcher.matching().withIgnoreCase().withStringMatcher(StringMatcher.CONTAINING));
		return repository.findAll(example);
	}

	@Override
	public void validar(Lancamento lancamento) {
		if(StringUtils.isBlank(lancamento.getDescricao())) {
			throw new RegraNegocioException("Informe uma descrição válida");
		}
		
		if(lancamento.getMes() == null || lancamento.getMes() < 1 || lancamento.getMes() > 12) {
			throw new RegraNegocioException("Informe um mês válido");
		}
		
		if(lancamento.getAno() == null || lancamento.getAno().toString().length() != 4) {
			throw new RegraNegocioException("Informe um ano válido");
		}
		
		if(lancamento.getUsuario() == null  || lancamento.getUsuario().getId() == null) {
			throw new RegraNegocioException("Informe um usuário");
		}
		
		if(lancamento.getValor() == null  || lancamento.getValor().compareTo(BigDecimal.ZERO) < 1) {
			throw new RegraNegocioException("Informe um valor válido");
		}
		
		if(lancamento.getTipo() == null) {
			throw new RegraNegocioException("Informe um tipo de lançamento");
		}
	}

	@Override
	@Transactional(readOnly = true)
	public Optional<Lancamento> obterPorId(Long id) {
		return repository.findById(id);
	}
	
	@Override
	public void atualizarStatus(Lancamento lancamento, StatusLancamento status) {
		lancamento.setStatus(status);
		atualizar(lancamento);
	}

	
	@Override
	public Lancamento atualizarStatus(Long codigo, AtualizarStatusDTO dto) {
		Lancamento lancamento = repository.findById(codigo).orElseThrow(() -> new RegraNegocioException("Lançamento não encontrado"));
		StatusLancamento status = StatusLancamento.valueOf(dto.getStatus());
		lancamento.setStatus(status);
		return atualizar(lancamento);
	}

	@Override
	public BigDecimal obterSaldoPorUsuario(Long id) {
		BigDecimal receitas = repository.obterTotalPorTipoAndUsuario(TipoLancamento.RECEITA, id);
		BigDecimal despesas = repository.obterTotalPorTipoAndUsuario(TipoLancamento.DESPESA, id);
		
		if(receitas == null)
			receitas = BigDecimal.ZERO;
		
		if(despesas == null)
			despesas = BigDecimal.ZERO;
		
		return receitas.subtract(despesas);
	}

}
