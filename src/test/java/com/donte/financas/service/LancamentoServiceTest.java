package com.donte.financas.service;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.RandomUtils;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.data.domain.Example;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.donte.financas.exception.RegraNegocioException;
import com.donte.financas.model.entity.Lancamento;
import com.donte.financas.model.entity.Usuario;
import com.donte.financas.model.enums.StatusLancamento;
import com.donte.financas.model.repository.LancamentoRepository;
import com.donte.financas.model.repository.LancamentoRepositoryTest;
import com.donte.financas.service.impl.LancamentoServiceImpl;

@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
public class LancamentoServiceTest {

	@SpyBean
	LancamentoServiceImpl service;

	@MockBean
	LancamentoRepository repository;

	@Test
	public void deveSalvarUmLancamento() {
		//cenário
		Lancamento lancamentoASalvar = LancamentoRepositoryTest.criarLancamento();
		Mockito.doNothing().when(service).validar(lancamentoASalvar);

		Lancamento lancamentoSalvo = LancamentoRepositoryTest.criarLancamento();
		lancamentoSalvo.setId(1L);
		lancamentoSalvo.setStatus(StatusLancamento.PENDENTE);
		Mockito.when(repository.save(lancamentoASalvar)).thenReturn(lancamentoSalvo);

		//execucao
		Lancamento lancamento = service.salvar(lancamentoASalvar);

		//verificacao
		Assertions.assertThat(lancamento.getId()).isEqualTo(lancamentoSalvo.getId());
		Assertions.assertThat(lancamento.getStatus()).isEqualTo(lancamentoSalvo.getStatus());
	}

	@Test
	public void naoDeveSalvarUmLancamentoQuandoHouverErroDeValidacao() {
		//cenário
		Lancamento lancamentoASalvar = LancamentoRepositoryTest.criarLancamento();
		Mockito.doThrow(RegraNegocioException.class).when(service).validar(lancamentoASalvar);

		//execução e verificação
		Assertions.catchThrowableOfType(() -> service.salvar(lancamentoASalvar), RegraNegocioException.class);
		Mockito.verify(repository, Mockito.never()).save(lancamentoASalvar);
	}
	
	@Test
	public void deveAtualizarUmLancamento() {
		//cenário
		Lancamento lancamentoSalvo = LancamentoRepositoryTest.criarLancamento();
		lancamentoSalvo.setId(1L);
		lancamentoSalvo.setStatus(StatusLancamento.PENDENTE);
		Mockito.doNothing().when(service).validar(lancamentoSalvo);
		Mockito.when(repository.save(lancamentoSalvo)).thenReturn(lancamentoSalvo);

		//execucao
		service.salvar(lancamentoSalvo);

		//verificacao
		Mockito.verify(repository, Mockito.times(1)).save(lancamentoSalvo);
	}
	
	@Test
	public void deveLancarErroAoAtualizarUmLancamentoNaoSalvo() {
		//cenário
		Lancamento lancamentoASalvar = LancamentoRepositoryTest.criarLancamento();

		//execução e verificação
		Assertions.catchThrowableOfType(() -> service.atualizar(lancamentoASalvar), NullPointerException.class);
		Mockito.verify(repository, Mockito.never()).save(lancamentoASalvar);
	}
	
	@Test
	public void deveDeletarUmLancamento() {
		//cenário
		Lancamento lancamento = LancamentoRepositoryTest.criarLancamento();
		lancamento.setId(1L);

		//execucao
		service.deletar(lancamento);

		//verificacao
		Mockito.verify(repository).delete(lancamento);
	}
	
	@Test
	public void deveLancarErroAoDeletarUmLancamentoNaoSalvo() {
		//cenário
		Lancamento lancamento = LancamentoRepositoryTest.criarLancamento();
		
		//execução e verificação
		Assertions.catchThrowableOfType(() -> service.deletar(lancamento), NullPointerException.class);
		Mockito.verify(repository, Mockito.never()).delete(lancamento);
	}
	
	@Test
	@SuppressWarnings("unchecked")
	public void deveFiltrarUmLancamento() {
		//cenário
		Lancamento lancamento = LancamentoRepositoryTest.criarLancamento();
		List<Lancamento> lista = Arrays.asList(lancamento);
		Mockito.when( repository.findAll(Mockito.any(Example.class)) ).thenReturn(lista);
		
		//execução
		List<Lancamento> resultado = service.buscar(lancamento);
		
		//execução e verificação
		Assertions.assertThat(resultado).isNotEmpty().hasSize(1).contains(lancamento);
	}
	
	@Test
	public void deveAtualizarStatusDeUmLancamento() {
		//cenário
		Lancamento lancamento = LancamentoRepositoryTest.criarLancamento();
		lancamento.setId(1L);
		lancamento.setStatus(StatusLancamento.PENDENTE);
		
		StatusLancamento status = StatusLancamento.EFETIVADO;
		Mockito.doReturn(lancamento).when(service).atualizar(lancamento);

		//execucao
		service.atualizarStatus(lancamento, status);

		//verificacao
		Assertions.assertThat(lancamento.getStatus()).isEqualTo(status);
		Mockito.verify(service).atualizar(lancamento);
	}
	
	@Test
	public void deveObterUmLancamentoPorId() {
		//cenário
		Long id = 1l;
		Lancamento lancamento = LancamentoRepositoryTest.criarLancamento();
		lancamento.setId(id);
		//Mockito.doReturn(Optional.of(Lancamento.class)).when(service).obterPorId(id);
		Mockito.when(repository.findById(id)).thenReturn(Optional.of(lancamento));
		
		//execucao
		Optional<Lancamento> optional = service.obterPorId(id);

		//verificação
		Assertions.assertThat(optional.isPresent()).isTrue();
	}
	
	@Test
	public void deveRetornarVazioQuandoOLancamentoNaoExiste() {
		//cenário
		Long id = 1l;
		Lancamento lancamento = LancamentoRepositoryTest.criarLancamento();
		lancamento.setId(id);
		
		Mockito.when(repository.findById(id)).thenReturn(Optional.empty());
		
		//execucao
		Optional<Lancamento> optional = service.obterPorId(id);

		//verificação
		Assertions.assertThat(optional.isPresent()).isFalse();
	}
	
	@Test
	public void deveLancarErroAoValidarUmLancamentoSemDescricaoExemplo() {
		//cenário
		String error = "Informe uma descrição válida";
		Lancamento lancamento = LancamentoRepositoryTest.criarLancamento();
		lancamento.setId(1l);
		lancamento.setDescricao("");
		
		//execução
		String message = Assertions.catchThrowableOfType(() ->service.validar(lancamento), RegraNegocioException.class).getMessage();
		
		//verificação
		Assertions.assertThat(message).isEqualTo(error);
	}
	
	@Test
	public void deveLancarErrosAoValidarUmLancamento() {
		//cenário
		Lancamento lancamento = new Lancamento();
		Throwable throwable = Assertions.catchThrowable(() ->service.validar(lancamento));
		Assertions.assertThat(throwable).isInstanceOf(RegraNegocioException.class).hasMessage("Informe uma descrição válida");
		
		lancamento.setDescricao("");
		throwable = Assertions.catchThrowable(() ->service.validar(lancamento));
		Assertions.assertThat(throwable).isInstanceOf(RegraNegocioException.class).hasMessage("Informe uma descrição válida");
		
		lancamento.setDescricao("Qualquer descrição");
		throwable = Assertions.catchThrowable(() ->service.validar(lancamento));
		Assertions.assertThat(throwable).isInstanceOf(RegraNegocioException.class).hasMessage("Informe um mês válido");
		
		lancamento.setMes(RandomUtils.nextInt(1, 13));
		throwable = Assertions.catchThrowable(() ->service.validar(lancamento));
		Assertions.assertThat(throwable).isInstanceOf(RegraNegocioException.class).hasMessage("Informe um ano válido");
		
		lancamento.setAno(RandomUtils.nextInt(2000, 2031));
		throwable = Assertions.catchThrowable(() ->service.validar(lancamento));
		Assertions.assertThat(throwable).isInstanceOf(RegraNegocioException.class).hasMessage("Informe um usuário");
		
		lancamento.setUsuario(Usuario.builder().id(1L).build());
		throwable = Assertions.catchThrowable(() ->service.validar(lancamento));
		Assertions.assertThat(throwable).isInstanceOf(RegraNegocioException.class).hasMessage("Informe um valor válido");
		
		lancamento.setValor(BigDecimal.valueOf(RandomUtils.nextInt(200, 500)));
		throwable = Assertions.catchThrowable(() ->service.validar(lancamento));
		Assertions.assertThat(throwable).isInstanceOf(RegraNegocioException.class).hasMessage("Informe um tipo de lançamento");
		/*
		lancamento.setMes(RandomUtils.nextInt(1, 13));
		throwable = Assertions.catchThrowable(() ->service.validar(lancamento));
		Assertions.assertThat(throwable).isInstanceOf(RegraNegocioException.class).hasMessage("Informe um ano válido");*/
	}

}
