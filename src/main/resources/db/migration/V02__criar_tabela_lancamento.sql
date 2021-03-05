CREATE TABLE financas.lancamento (
  id bigserial NOT NULL PRIMARY KEY ,
  descricao character varying(100) NOT NULL,
  mes integer NOT NULL,
  ano integer NOT NULL,
  valor numeric(16,2) NOT NULL,
  tipo character varying(20) check (tipo in ('RECEITA', 'DESPESA')) NOT NULL,
  status character varying(20) check (status in ('PENDENTE', 'CANCELADO', 'EFETIVADO')) NOT NULL,
  usuarioid bigint REFERENCES financas.usuario (id) NOT NULL,
  criado date default now() 
);