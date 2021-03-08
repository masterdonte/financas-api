package com.donte.financas.model.repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.donte.financas.model.entity.Lancamento;
import com.donte.financas.model.enums.StatusLancamento;
import com.donte.financas.model.enums.TipoLancamento;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = Replace.NONE)
@ExtendWith(SpringExtension.class)
public class LancamentoRepositoryTest {

	@Autowired
	LancamentoRepository repository;

	@Autowired
	TestEntityManager manager;

	@Test
	public void deveSalvarUmLancamento() {
		Lancamento lancamento = criarLancamento();
		lancamento = repository.save(lancamento);
		Assertions.assertThat(lancamento).isNotNull();
	}

	@Test
	public void deveDeletarUmLancamento() {
		Lancamento lancamento = criarLancamentoAndPersistir();
		lancamento = manager.find(Lancamento.class, lancamento.getId());
		repository.delete(lancamento);
		Lancamento lancamentoInexistente = manager.find(Lancamento.class, lancamento.getId());
		Assertions.assertThat(lancamentoInexistente).isNull();
	}
	
	@Test
	public void deveAtualizarUmLancamento() {
		Lancamento lancamento = criarLancamentoAndPersistir();
		
		lancamento.setAno(2018);
		lancamento.setDescricao("Descricao atualizada");
		lancamento.setStatus(StatusLancamento.CANCELADO);
		
		repository.save(lancamento);
		
		Lancamento lancamentoAtualizado = manager.find(Lancamento.class, lancamento.getId());
		
		Assertions.assertThat(lancamentoAtualizado.getAno()).isEqualTo(2018);
		Assertions.assertThat(lancamentoAtualizado.getDescricao()).isEqualTo("Descricao atualizada");
		Assertions.assertThat(lancamentoAtualizado.getStatus()).isEqualTo(StatusLancamento.CANCELADO);
	}
	
	@Test
	public void deveBuscarUmLancamentoPorId() {
		Lancamento lancamento = criarLancamentoAndPersistir();
		
		Optional<Lancamento> optLanc = repository.findById(lancamento.getId());
		
		Assertions.assertThat(optLanc.isPresent()).isTrue();
	}
	
	private Lancamento criarLancamentoAndPersistir() {
		Lancamento lancamento = criarLancamento();
		return manager.persist(lancamento);
	}
	
	public static Lancamento criarLancamento() {
		return Lancamento.builder()
				.ano(2021)
				.mes(3)
				.descricao("Qualquer descricao")
				.dataCadastro(LocalDate.now())
				.valor(BigDecimal.valueOf(100))
				.tipo(TipoLancamento.RECEITA)
				.status(StatusLancamento.PENDENTE).build();
	}
}
