package com.donte.financas.model.repository;

import java.math.BigDecimal;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.donte.financas.model.entity.Lancamento;
import com.donte.financas.model.enums.TipoLancamento;

public interface LancamentoRepository extends JpaRepository<Lancamento, Long> {
	
	@Query("select sum(l.valor) from Lancamento l inner join l.usuario u where u.id = :id and l.tipo = :tipo")
	public BigDecimal obterTotalPorTipoAndUsuario(@Param("tipo") TipoLancamento tipo , @Param("id") Long usuarioId);

}
