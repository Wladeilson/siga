ALTER SESSION SET CURRENT_SCHEMA = sigasr;

-- OS_FS0006 Itens 2 e 11: Alterando a tabela SR_ACAO para conter os novos campos
ALTER TABLE SR_ACAO ADD (
    TIPO_ACAO NUMBER(10,0),
    TIPO_EXECUCAO NUMBER(10,0),
    FORMA_ATENDIMENTO NUMBER(10,0)
);

-- OS_FS0004 Item 3: Alterando a tabela SR_LISTA para conter os novos campos
ALTER TABLE SR_LISTA ADD (
    DESCR_ABRANGENCIA CLOB, 
    DESCR_JUSTIFICATIVA CLOB, 
    DESCR_PRIORIZACAO CLOB
);

-- OS_FS0006 Item 3: Alterando a tabela SR_CONFIGURACAO para conter os novos campos
ALTER TABLE SR_CONFIGURACAO ADD (
    SLA_PRE_ATENDIMENTO_QUANT NUMBER(10,0),
    ID_UNIDADE_PRE_ATENDIMENTO NUMBER,
    SLA_ATENDIMENTO_QUANT NUMBER(10,0),
    ID_UNIDADE_ATENDIMENTO NUMBER,
    SLA_POS_ATENDIMENTO_QUANT NUMBER(10,0),
    ID_UNIDADE_POS_ATENDIMENTO NUMBER
);

ALTER TABLE SR_CONFIGURACAO
  ADD FOREIGN KEY (ID_UNIDADE_PRE_ATENDIMENTO)
  REFERENCES CORPORATIVO.CP_UNIDADE_MEDIDA (ID_UNIDADE_MEDIDA);
ALTER TABLE SR_CONFIGURACAO
  ADD FOREIGN KEY (ID_UNIDADE_ATENDIMENTO)
  REFERENCES CORPORATIVO.CP_UNIDADE_MEDIDA (ID_UNIDADE_MEDIDA);
ALTER TABLE SR_CONFIGURACAO
  ADD FOREIGN KEY (ID_UNIDADE_POS_ATENDIMENTO)
  REFERENCES CORPORATIVO.CP_UNIDADE_MEDIDA (ID_UNIDADE_MEDIDA);

-- OS_FS0006 Item 3: Alterando a tabela SR_CONFIGURACAO para conter os novos campos
ALTER TABLE SR_CONFIGURACAO ADD (
    MARGEM_SEGURANCA NUMBER(10,0),
    OBSERVACAO_SLA CLOB,
    FG_DIVULGAR_SLA CHAR(1),
	FG_NOTIFICAR_GESTOR CHAR(1),
	FG_NOTIFICAR_SOLICITANTE CHAR(1),
	FG_NOTIFICAR_CADASTRANTE CHAR(1),
	FG_NOTIFICAR_INTERLOCUTOR CHAR(1),
	FG_NOTIFICAR_ATENDENTE CHAR(1)
);

UPDATE SR_CONFIGURACAO SET 
  FG_DIVULGAR_SLA = 'N', 
  FG_NOTIFICAR_GESTOR = 'N', 
  FG_NOTIFICAR_SOLICITANTE = 'N',
  FG_NOTIFICAR_CADASTRANTE = 'N',
  FG_NOTIFICAR_INTERLOCUTOR = 'N',
  FG_NOTIFICAR_ATENDENTE = 'N';
COMMIT;

-- OS_FS0006 Item 3: Alterando a tabela CP_UNIDADE_MEDIDA
GRANT select ON CP_UNIDADE_MEDIDA TO SIGASR;
GRANT insert ON CP_UNIDADE_MEDIDA TO SIGASR;
GRANT update ON CP_UNIDADE_MEDIDA TO SIGASR;
INSERT INTO CORPORATIVO.CP_UNIDADE_MEDIDA (ID_UNIDADE_MEDIDA, DESCR_UNIDADE_MEDIDA) VALUES (4, 'Hora');
COMMIT;

-- Criando tabela da Lista de Configuração
CREATE TABLE "SIGASR"."SR_LISTA_CONFIGURACAO" 
   ("ID_LISTA_CONFIGURACAO" NUMBER(10,0) NOT NULL ENABLE, 
	"ID_CONFIGURACAO" NUMBER(10,0) NOT NULL ENABLE, 
	"ID_LISTA" NUMBER(10,0) NOT NULL ENABLE, 
	 CONSTRAINT "SR_LISTA_CONFIGURACAO_PK" PRIMARY KEY ("ID_LISTA_CONFIGURACAO")
  USING INDEX PCTFREE 10 INITRANS 2 MAXTRANS 255 COMPUTE STATISTICS 
  TABLESPACE "USERS"  ENABLE, 
	 CONSTRAINT "SR_LISTA_CONFIGURACAO_FK1" FOREIGN KEY ("ID_CONFIGURACAO")
	  REFERENCES "SIGASR"."SR_CONFIGURACAO" ("ID_CONFIGURACAO_SR") ENABLE, 
	 CONSTRAINT "SR_LISTA_CONFIGURACAO_FK2" FOREIGN KEY ("ID_LISTA")
	  REFERENCES "SIGASR"."SR_LISTA" ("ID_LISTA") ENABLE
   ) SEGMENT CREATION DEFERRED 
  PCTFREE 10 PCTUSED 40 INITRANS 1 MAXTRANS 255 
 NOCOMPRESS LOGGING
  TABLESPACE "USERS" ;

-- Sequence para a tabela Lista de Configuração
CREATE SEQUENCE  "SIGASR"."SR_LISTA_CONFIGURACAO_SEQ"
  MINVALUE 1 
  MAXVALUE 9999999999999999999999999999 
  INCREMENT BY 1 
  START WITH 1 
  CACHE 20 NOORDER NOCYCLE;