package com.donte.financas.service;

import java.util.Optional;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.donte.financas.exception.ErroAutenticacaoException;
import com.donte.financas.exception.RegraNegocioException;
import com.donte.financas.model.entity.Usuario;
import com.donte.financas.model.repository.UsuarioRepository;
import com.donte.financas.service.impl.UsuarioServiceImpl;

@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
public class UsuarioServiceTest {
	
	@SpyBean
	UsuarioServiceImpl service;
	
	@MockBean
	UsuarioRepository repository;
	//@BeforeEach public void setUp() {service = new UsuarioServiceImpl(repository);}
	
	@Test
	public void deveSalvarUmUsuario() {
		Mockito.doNothing().when(service).validarEmail(Mockito.anyString());
		Usuario usuario = Usuario.builder().id(1L).nome("nome").email("email@email.com").senha("senha").build();
		
		Mockito.when(repository.save(Mockito.any(Usuario.class))).thenReturn(usuario);
		Usuario usuarioSalvo = service.salvarUsuario(new Usuario());
		
		Assertions.assertEquals(usuario, usuarioSalvo);
	}
	
	@Test
	public void naoDeveSalvarUsuarioComEmailJaCadastrado() {
		String email = "email@email.com";
		
		Usuario usuario = Usuario.builder().email("email@email.com").build();
		Mockito.doThrow(RegraNegocioException.class).when(service).validarEmail(email);
		
		Mockito.when(repository.save(Mockito.any(Usuario.class))).thenReturn(usuario);
		
		Assertions.assertThrows(RegraNegocioException.class, () -> {
			service.salvarUsuario(usuario);
		});
		
		Mockito.verify(repository, Mockito.never()).save(usuario);					
	}
	
	@Test
	public void deveAutenticarUmUsuarioComSucesso() {
		String email = "email@email.com";
		String senha = "senha";
		
		Usuario usuario = Usuario.builder().email(email).senha(senha).id(1L).build();
		Mockito.when(repository.findByEmail(email)).thenReturn(Optional.of(usuario));		
		//Mockito.when(repository.findByEmail(email)).thenAnswer(o -> Optional.of(usuario));
		
		Usuario result = service.autenticar(email, senha);
		Assertions.assertNotNull(result);
	}
	
	@Test
	public void deveLancarErroQuandoNaoEncontrarUsuario() {
		Mockito.when(repository.findByEmail(Mockito.anyString())).thenAnswer(o -> Optional.empty());

		String message = Assertions.assertThrows(ErroAutenticacaoException.class, () -> {
			service.autenticar("email@email.com", "email");
		}).getMessage();
		
		Assertions.assertEquals(message, "Usuario nao encontrado.");
	}
	
	@Test
	public void deveLancarErroQuandoSenhaForInvalida() {
		String email = "email@email.com";
		String senha = "senha";
		
		Usuario usuario = Usuario.builder().email(email).senha(senha).id(1L).build();
		
		Mockito.when(repository.findByEmail(Mockito.anyString())).thenAnswer(o -> Optional.of(usuario));
		
		String message = Assertions.assertThrows(ErroAutenticacaoException.class, () -> {
			service.autenticar(email, "123");
		}).getMessage();
		
		Assertions.assertEquals(message, "Senha invalida.");	
	}
	
	@Test
	public void deveValidarEmail() {
		// cenário
		Mockito.when(repository.existsByEmail(Mockito.anyString())).thenReturn(false);
		repository.deleteAll();
		// acao /execucao
		Assertions.assertDoesNotThrow(() -> {
			service.validarEmail("email@email.com");
		});			
	}
	
	@Test
	public void deveLancarErroQuandoValidarUmEmailJaCadastrado() {
		// cenário
		//Usuario usuario = Usuario.builder().nome("usuario").email("email@email.com").build(); repository.save(usuario);
		Mockito.when(repository.existsByEmail(Mockito.anyString())).thenReturn(true);
		// verificação
		Assertions.assertThrows(RegraNegocioException.class, () -> {
			service.validarEmail("email@email.com");
		});
	}
}
