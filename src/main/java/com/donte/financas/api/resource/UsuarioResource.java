package com.donte.financas.api.resource;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.donte.financas.api.dto.UsuarioDTO;
import com.donte.financas.exception.ErroAutenticacaoException;
import com.donte.financas.exception.RegraNegocioException;
import com.donte.financas.model.entity.Usuario;
import com.donte.financas.service.LancamentoService;
import com.donte.financas.service.UsuarioService;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioResource {
	
	private UsuarioService service;
	private LancamentoService lancService;

	public UsuarioResource(UsuarioService service, LancamentoService lancService) {
		this.service = service;
		this.lancService = lancService;
	}
	
	@PostMapping("/autenticar")
	public ResponseEntity<?> autenticar(@RequestBody UsuarioDTO dto){
		try {
			Usuario autenticado = service.autenticar(dto.getEmail(), dto.getSenha());
			return ResponseEntity.ok(autenticado);
		}catch (ErroAutenticacaoException e) {
			return ResponseEntity.badRequest().body(e.getMessage());
		}
	}
	
	@PostMapping
	public ResponseEntity<?> salvar(@RequestBody UsuarioDTO dto){
		Usuario usuario = Usuario.builder().nome(dto.getNome()).email(dto.getEmail()).senha(dto.getSenha()).build();
		try {
			Usuario usuarioSalvo = service.salvarUsuario(usuario);
			return ResponseEntity.status(HttpStatus.CREATED).body(usuarioSalvo);
		}catch (RegraNegocioException e) {
			return ResponseEntity.badRequest().body(e.getMessage());
		}
	}
	
	@GetMapping
	public List<Usuario> teste(){
		return service.obterTodos();
	}
	
	@GetMapping("{id}/saldo")
	public ResponseEntity<?> saldo(@PathVariable("id") Long id){
		Optional<Usuario> usuarioOpt = service.obterPorId(id);
		if(!usuarioOpt.isPresent())
			return ResponseEntity.notFound().build();
		BigDecimal saldo = lancService.obterSaldoPorUsuario(id);
		return ResponseEntity.ok(saldo);
	}
	

}
