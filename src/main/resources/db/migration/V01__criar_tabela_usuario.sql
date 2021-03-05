CREATE TABLE financas.usuario (
  id bigserial NOT NULL PRIMARY KEY,
  nome character varying(150),
  email character varying(100),
  senha character varying(20),
  criado date default now()
);
