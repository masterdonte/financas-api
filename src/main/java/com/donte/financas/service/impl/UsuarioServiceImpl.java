package com.donte.financas.service.impl;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.donte.financas.exception.ErroAutenticacaoException;
import com.donte.financas.exception.RegraNegocioException;
import com.donte.financas.model.entity.Usuario;
import com.donte.financas.model.repository.UsuarioRepository;
import com.donte.financas.service.UsuarioService;

@Service
public class UsuarioServiceImpl implements UsuarioService{
	
	private UsuarioRepository repository;

	public UsuarioServiceImpl(UsuarioRepository repository) {
		this.repository = repository;
	}

	@Override
	public Usuario autenticar(String email, String senha) {
		Optional<Usuario> optUsuario = repository.findByEmail(email);
		if(!optUsuario.isPresent()) {
			throw new ErroAutenticacaoException("Usuario nao encontrado.");
		}
		if(!optUsuario.get().getSenha().equals(senha)) {
			throw new ErroAutenticacaoException("Senha invalida.");
		}
		return optUsuario.get();
	}

	@Override
	@Transactional
	public Usuario salvarUsuario(Usuario usuario) {
		validarEmail(usuario.getEmail());
		return repository.save(usuario);
	}

	@Override
	public void validarEmail(String email) {
		if(repository.existsByEmail(email)) {
			throw new RegraNegocioException("Já existe um usuário cadastrado com este e-mail.");
		}
	}

	@Override
	public Optional<Usuario> obterPorId(Long id) {
		return repository.findById(id);
	}

	@Override
	public List<Usuario> obterTodos() {
		return repository.findAll();
	}

}
