<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
	buffer="64kb"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://localhost/libstag" prefix="f"%>
<%@ taglib prefix="ww" uri="/webwork"%>
<%@ taglib uri="http://localhost/sigatags" prefix="siga"%>

		<c:if test="${empty pagina_de_erro}">
			<li class="dropdown"><a href="#" class="dropdown-toggle"> <i class="fa fa-sitemap"></i> <span class="hidden-xs">M�dulos</span></a>
				<ul class="dropdown-menu">
					<c:if
						test="${f:podeUtilizarServicoPorConfiguracao(titular,lotaTitular,'SIGA;DOC:M�dulo de Documentos')}">
						<li><a
							href="/sigaex/app/expediente/doc/listar?primeiraVez=sim"><i class="fa fa-cube"></i> <span class="hidden-xs">Documentos</span></a>
						</li>
					</c:if>

					<c:if
						test="${f:podeUtilizarServicoPorConfiguracao(titular,lotaTitular,'SIGA;WF:M�dulo de Workflow')}">
						<li><a href="/sigawf/app/resumo"><i class="fa fa-cube"></i> <span class="hidden-xs">Workflow</span></a>
						</li>
					</c:if>

					<c:if
						test="${f:podeUtilizarServicoPorConfiguracao(titular,lotaTitular,'SIGA;SR')}">
						<li><a href="/sigasr/"><i class="fa fa-cube"></i> <span class="hidden-xs">Servi�os</span></a>
						</li>
					</c:if>

					<c:if
						test="${f:podeUtilizarServicoPorConfiguracao(titular,lotaTitular,'SIGA;GC:M�dulo de Gest�o de Conhecimento')}">
						<li><a href="/sigagc/app/estatisticaGeral"><i class="fa fa-cube"></i> <span class="hidden-xs">Gest�o de Conhecimento</span></a>
						</li>
					</c:if>
					

					<!-- <li><a href="/sigatr/">Treinamento</a>
					</li> -->
					<!-- <li><a href="/SigaServicos/">Servi�os</a>
					</li> -->
					<c:if
						test="${f:podeUtilizarServicoPorConfiguracao(titular,lotaTitular,'SIGA;AQ: M�dulo de Adicional de Qualifica��o') or 
						f:podeUtilizarServicoPorConfiguracao(titular,lotaTitular,'SIGA;BNF: M�dulo de Benef�cios') or 
						f:podeUtilizarServicoPorConfiguracao(titular,lotaTitular,'SIGA;CAD: M�dulo de Cadastro') or 
						f:podeUtilizarServicoPorConfiguracao(titular,lotaTitular,'SIGA;CST: M�dulo de Consultas') or 
						f:podeUtilizarServicoPorConfiguracao(titular,lotaTitular,'SIGA;LOT: M�dulo de Lota��o') or 
						f:podeUtilizarServicoPorConfiguracao(titular,lotaTitular,'SIGA;TRN: M�dulo de Treinamento')}">
						<li class="dropdown"><a href="#" class="dropdown-toggle"> <i class="fa fa-plus-square"></i> <span class="hidden-xs">Pessoas</span></a>
			 				<ul class="dropdown-menu">
								<c:if
									test="${f:podeUtilizarServicoPorConfiguracao(titular,lotaTitular,'SIGA;AQ: M�dulo de Adicional de Qualifica��o')}">
									<li><a href="${f:getURLSistema('siga.sgp.aq')}">AQ</a></li>
								</c:if>
								<c:if
									test="${f:podeUtilizarServicoPorConfiguracao(titular,lotaTitular,'SIGA;BNF: M�dulo de Benef�cios')}">
									<li><a href="${f:getURLSistema('siga.sgp.bnf')}">Benef�cios</a>
									</li>
								</c:if>
								<c:if
									test="${f:podeUtilizarServicoPorConfiguracao(titular,lotaTitular,'SIGA;CAD: M�dulo de Cadastro')}">
									<li><a href="${f:getURLSistema('siga.sgp.cad')}">Cadastro</a>
									</li>
								</c:if>
								<c:if
									test="${f:podeUtilizarServicoPorConfiguracao(titular,lotaTitular,'SIGA;CST: M�dulo de Consultas')}">
									<li><a href="${f:getURLSistema('siga.sgp.cst')}">Consultas</a>
									</li>
								</c:if>
								<c:if
									test="${f:podeUtilizarServicoPorConfiguracao(titular,lotaTitular,'SIGA;LOT: M�dulo de Lota��o')}">
									<li><a href="${f:getURLSistema('siga.sgp.lot')}">Lota��o</a>
									</li>
								</c:if>
								<c:if
									test="${f:podeUtilizarServicoPorConfiguracao(titular,lotaTitular,'SIGA;TRN: M�dulo de Treinamento')}">
									<li><a href="${f:getURLSistema('siga.sgp.trn')}">Treinamento</a>
									</li>
								</c:if>
							</ul>
						</li>
					</c:if>
					
					<c:if test="${f:podeUtilizarServicoPorConfiguracao(titular,lotaTitular,'SIGA;PP:Agendamento de per�cias do INSS')}">
						<li><a href="/sigapp/"><i class="fa fa-cube"></i> <span class="hidden-xs">Agenda de Per�cias M�dicas</span></a></li>
					</c:if>
						
					<c:if test="${f:podeUtilizarServicoPorConfiguracao(titular,lotaTitular,'SIGA;TP:M�dulo de Transportes')}">
                    	<li><a href="/sigatp/"><i class="fa fa-cube"></i> <span class="hidden-xs">Transportes</span></a>
                    </li>
                </c:if>
					
				</ul>
			</li>
			
			
<c:if
	test="${f:podeUtilizarServicoPorConfiguracao(titular,lotaTitular,'SIGA:Sistema Integrado de Gest�o Administrativa;GI:M�dulo de Gest�o de Identidade')}">
	<li class="dropdown"><a href="#" class="dropdown-toggle"><i
			class="fa fa-user"></i> <span>Gest�o de Identidade</span></a>
		<ul class="dropdown-menu">
			<c:if
				test="${f:podeUtilizarServicoPorConfiguracao(titular,lotaTitular,'SIGA;GI;ID:Gerenciar identidades')}">
				<li><ww:a href="/siga/gi/identidade/listar.action">Identidade</ww:a>
				</li>
			</c:if>
			<c:if
				test="${f:podeUtilizarServicoPorConfiguracao(titular,lotaTitular,'SIGA;GI;PERMISSAO:Gerenciar permiss�es')}">
				<li><ww:a href="/siga/gi/acesso/listar.action">Configurar Permiss�es</ww:a>
				</li>
			</c:if>
			<c:if
				test="${f:podeUtilizarServicoPorConfiguracao(titular,lotaTitular,'SIGA;GI;PERFIL:Gerenciar perfis de acesso')}">
				<li><ww:a href="/siga/gi/perfil/listar.action">Perfil de Acesso</ww:a>
				</li>
			</c:if>
			<c:if
				test="${f:podeUtilizarServicoPorConfiguracao(titular,lotaTitular,'SIGA;GI;PERFILJEE:Gerenciar perfis do JEE')}">
				<li><ww:a href="/siga/gi/perfiljee/listar.action">Perfil de Acesso do JEE</ww:a>
				</li>
			</c:if>
			<c:if
				test="${f:podeUtilizarServicoPorConfiguracao(titular,lotaTitular,'SIGA;GI;GDISTR:Gerenciar grupos de distribui��o')
							       || (f:podeGerirAlgumGrupo(titular,lotaTitular,2) && f:podeUtilizarServicoPorConfiguracao(titular,lotaTitular,'SIGA;GI;GDISTR;DELEG:Gerenciar grupos de distribui��o delegados'))}">
				<li><ww:a href="${serverAndPort}/siga/gi/email/listar.action">Grupo de Distribui��o</ww:a>
				</li>
			</c:if>
			<c:if
				test="${f:podeUtilizarServicoPorConfiguracao(titular,lotaTitular,'SIGA;GI;SELFSERVICE:Gerenciar servi�os da pr�pria lota��o')}">
				<li><ww:a href="/siga/gi/servico/acesso.action">Acesso a Servi�os</ww:a>
				</li>
			</c:if>
			<c:if
				test="${f:podeUtilizarServicoPorConfiguracao(titular,lotaTitular,'SIGA;GI;REL:Gerar relat�rios')}">
				<li class="dropdown"><a href="#" class="dropdown-toggle"> <i
						class="fa fa-dashboard"></i> <span class="hidden-xs">Relat�rios</span></a>
					<ul class="dropdown-menu">
						<li><ww:a
								href="/siga/gi/relatorio/selecionar_acesso_servico.action">Acesso aos Servi�os</ww:a>
						</li>
						<li><ww:a
								href="/siga/gi/relatorio/selecionar_permissao_usuario.action">Permiss�es de Usu�rio</ww:a>
						</li>
						<li><ww:a
								href="/siga/gi/relatorio/selecionar_alteracao_direitos.action">Altera��o de Direitos</ww:a>
						</li>
						<li><ww:a
								href="/siga/gi/relatorio/selecionar_historico_usuario.action">Hist�rico de Usu�rio</ww:a>
						</li>
					</ul></li>
			</c:if>
		</ul></li>
</c:if>
</c:if>

<c:if
	test="${f:podeUtilizarServicoPorConfiguracao(titular,lotaTitular,'SIGA:Sistema Integrado de Gest�o Administrativa;FE:Ferramentas')}">
	<li class="dropdown"><a href="#" class="dropdown-toggle"><i
			class="fa fa-cog"></i> <span>Ferramentas</span></a>
		<ul class="dropdown-menu">
			<c:if
				test="${f:podeUtilizarServicoPorConfiguracao(titular,lotaTitular,'SIGA;FE;MODVER:Visualizar modelos')}">
				<li><ww:a href="/siga/modelo/listar.action">Cadastro de modelos</ww:a>
				</li>
			</c:if>
			<c:if
				test="${f:podeUtilizarServicoPorConfiguracao(titular,lotaTitular,'SIGA;FE;CAD_ORGAO:Cadastrar �rg�os')}">
				<li><ww:a href="/siga/orgao/listar.action">Cadastro de �rg�os Externos</ww:a>
				</li>
			</c:if>
			<c:if
				test="${f:podeUtilizarServicoPorConfiguracao(titular,lotaTitular,'SIGA;FE;WF_ADMIN:Administrar SIGAWF')}">
				<li><ww:a href="/sigawf/administrar.action">Administrar SIGA WF</ww:a>
				</li>
			</c:if>
			<c:if
				test="${f:podeUtilizarServicoPorConfiguracao(titular,lotaTitular,'SIGA;FE;CAD_FERIADO:Cadastrar Feriados')}">
				<li><ww:a href="/siga/feriado/listar.action">Cadastro de Feriados</ww:a></li>
			</c:if>
		</ul></li>
</c:if>

<%--	<li><a target="_blank"
			href="/wiki/Wiki.jsp?page=${f:removeAcento(titulo)}">Ajuda</a></li>
 --%>
<!-- insert menu -->
<c:import url="/paginas/menus/menu.jsp"></c:import>

