insert into siga.ex_tipo_movimentacao values(58, 'Assinatura com senha');

insert into siga.ex_tipo_movimentacao values(59, 'Assinatura de movimenta��o com senha');

insert into siga.ex_tipo_movimentacao values(60, 'Confer�ncia de C�pia de Documento com senha');

Insert into CORPORATIVO.CP_CONFIGURACAO (ID_CONFIGURACAO,HIS_DT_INI,ID_SIT_CONFIGURACAO,ID_TP_CONFIGURACAO) values (CORPORATIVO.CP_CONFIGURACAO_SEQ.nextval,sysdate,'2','1');
Insert into SIGA.EX_CONFIGURACAO (ID_CONFIGURACAO_EX,ID_TP_MOV) values (CORPORATIVO.CP_CONFIGURACAO_SEQ.currval,59);

Insert into CORPORATIVO.CP_CONFIGURACAO (ID_CONFIGURACAO,HIS_DT_INI,ID_SIT_CONFIGURACAO,ID_TP_CONFIGURACAO) values (CORPORATIVO.CP_CONFIGURACAO_SEQ.nextval,sysdate,'2','1');
Insert into SIGA.EX_CONFIGURACAO (ID_CONFIGURACAO_EX,ID_TP_MOV) values (CORPORATIVO.CP_CONFIGURACAO_SEQ.currval,60);