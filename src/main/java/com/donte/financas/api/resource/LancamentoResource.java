package com.donte.financas.api.resource;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.BeanUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.donte.financas.api.dto.AtualizarStatusDTO;
import com.donte.financas.api.dto.LancamentoDTO;
import com.donte.financas.exception.RegraNegocioException;
import com.donte.financas.model.entity.Lancamento;
import com.donte.financas.model.entity.Usuario;
import com.donte.financas.model.enums.StatusLancamento;
import com.donte.financas.model.enums.TipoLancamento;
import com.donte.financas.service.LancamentoService;
import com.donte.financas.service.UsuarioService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/lancamentos")
@RequiredArgsConstructor // Gera um construtor com atributos finais, o que faz com que o proprio spring ja faca a injecao de dependencia, mesmo que colocar o autowired
public class LancamentoResource {

	private final LancamentoService service;
	private final UsuarioService usuarioService;
	
	@GetMapping
	public ResponseEntity<?> buscar(
			@RequestParam(value = "descricao", required = false) String descricao,
			@RequestParam(value= "mes", required = false) Integer mes,
			@RequestParam(value= "ano", required = false) Integer ano,
			@RequestParam(value= "usuario") Long idUsuario){

		Optional<Usuario> usuarioOpt = usuarioService.obterPorId(idUsuario);
		if(!usuarioOpt.isPresent())
			return ResponseEntity.badRequest().body("Não foi possivel realizar a consulta. Usuário nao encontrado");

		Lancamento lancamentoFiltro = new Lancamento();
		lancamentoFiltro.setDescricao(descricao);
		lancamentoFiltro.setMes(mes);
		lancamentoFiltro.setAno(ano);
		lancamentoFiltro.setUsuario(usuarioOpt.get());

		List<Lancamento> lancamentos = service.buscar(lancamentoFiltro);
		return ResponseEntity.ok(lancamentos);
	}
	
	@GetMapping("{id}")
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public ResponseEntity<?> buscarPorId(@PathVariable("id") Long id){
		return service.obterPorId(id)
				.map( entity -> new ResponseEntity( converter(entity), HttpStatus.OK ) ) 
				.orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).body("Lancamento nao encontrado na base de dados"));
	}
	
	@GetMapping("/{id}/temp")
	public ResponseEntity<?> buscarPeloCodigo(@PathVariable Long id) {
		Optional<Lancamento> lancamento = service.obterPorId(id);
		return lancamento.isPresent() ? ResponseEntity.ok(converter(lancamento.get())) : ResponseEntity.notFound().build();
	}

	@PostMapping
	public ResponseEntity<?> salvar(@RequestBody LancamentoDTO dto){
		try {
			Lancamento entidade = service.salvar(converter(dto));			
			return ResponseEntity.status(HttpStatus.CREATED).body(entidade);//return ResponseEntity.ok(entidade);//return new ResponseEntity(entidade, HttpStatus.CREATED);
		}catch(RegraNegocioException e) {
			return ResponseEntity.badRequest().body(e.getMessage());
		}
	}

	@DeleteMapping("{id}")
	public ResponseEntity<?> deletar(@PathVariable("id") Long id) {
		return service.obterPorId(id).map(entity -> {
			service.deletar(entity);
			return new ResponseEntity<>(HttpStatus.NO_CONTENT); //ResponseEntity.noContent().build();
		}).orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).body("Lancamento nao encontrado na base de dados"));
	}

	@PutMapping("{id}")
	public ResponseEntity<?> atualizar(@PathVariable("id") Long codigo, @RequestBody LancamentoDTO dto){
		return service.obterPorId(codigo).map(entidade -> {
			try {
				Lancamento lancamento = converter(dto);
				lancamento.setId(codigo);
				service.atualizar(lancamento);
				return ResponseEntity.ok(lancamento);
			}catch (RegraNegocioException e) {
				return ResponseEntity.badRequest().body(e.getMessage());
			}

		}).orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).body("Lancamento nao encontrado na base de dados"));
	}
	
	@PutMapping("{id}/status1")
	public ResponseEntity<?> atualizarStatus1(@PathVariable("id") Long codigo, @RequestBody AtualizarStatusDTO dto){
		return service.obterPorId(codigo).map(entidade -> {
			try {
				StatusLancamento status = StatusLancamento.valueOf(dto.getStatus());
				if(status == null) {
					return ResponseEntity.badRequest().body("Status inválido");
				}
				entidade.setStatus(status);
				service.atualizar(entidade);
				return ResponseEntity.ok(entidade);
			}catch (RegraNegocioException | IllegalArgumentException | NullPointerException e) {
				return ResponseEntity.badRequest().body(e.getMessage());
			}

		}).orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).body("Lancamento nao encontrado na base de dados"));
	}
	
	@PutMapping("{id}/status")	
	public ResponseEntity<?> atualizarStatus(@PathVariable("id") Long codigo, @RequestBody AtualizarStatusDTO dto){
		try {
			Lancamento lancamento = service.atualizarStatus(codigo, dto);
			return ResponseEntity.ok(lancamento);
		}catch (RegraNegocioException | IllegalArgumentException | NullPointerException e) {
			return ResponseEntity.badRequest().body(e.getMessage());
		}
	}

	@PutMapping("/temp/{id}")
	public ResponseEntity<?> atualizarAux(@PathVariable("id") Long codigo, @RequestBody LancamentoDTO dto){
		try {
			Lancamento lancamentoSalvo = service.obterPorId(codigo).orElseThrow(() -> new IllegalArgumentException());
			BeanUtils.copyProperties(converter(dto), lancamentoSalvo, "id");
			service.atualizar(lancamentoSalvo);
			return ResponseEntity.ok(lancamentoSalvo);
		}catch (IllegalArgumentException e) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Lancamento nao encontrado na base de dados");
		}catch (RegraNegocioException e) {
			return ResponseEntity.badRequest().body(e.getMessage());
		}			
	}

	private Lancamento converter(LancamentoDTO dto) {
		Usuario usuario = usuarioService.obterPorId(dto.getUsuario()).orElseThrow(() -> new RegraNegocioException("Usuario nao encontrado"));
		Lancamento lanc = new Lancamento();
		lanc.setDescricao(dto.getDescricao());
		lanc.setAno(dto.getAno());
		lanc.setMes(dto.getMes());
		lanc.setValor(dto.getValor());
		lanc.setUsuario(usuario);
		if(StringUtils.hasText(dto.getStatus()))
			lanc.setStatus(StatusLancamento.valueOf(dto.getStatus()));
		if(StringUtils.hasText(dto.getTipo()))
			lanc.setTipo(TipoLancamento.valueOf(dto.getTipo()));
		return lanc;
	}
	
	private LancamentoDTO converter(Lancamento lanc) {
		return LancamentoDTO.builder()
		.id(lanc.getId())
		.descricao(lanc.getDescricao())
		.valor(lanc.getValor())
		.mes(lanc.getMes())
		.ano(lanc.getAno())
		.status(lanc.getStatus().name())
		.tipo(lanc.getTipo().name())
		.usuario(lanc.getUsuario().getId()).build();
	}

}
