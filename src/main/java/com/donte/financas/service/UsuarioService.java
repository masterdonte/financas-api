package com.donte.financas.service;

import java.util.List;
import java.util.Optional;

import com.donte.financas.model.entity.Usuario;

public interface UsuarioService {
	
	Usuario autenticar(String email, String senha);
	
	Usuario salvarUsuario(Usuario usuario);
	
	void validarEmail(String email);

	Optional<Usuario> obterPorId(Long id);
	
	List<Usuario> obterTodos();

}
