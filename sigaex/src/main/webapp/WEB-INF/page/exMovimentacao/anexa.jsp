<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ page language="java" contentType="text/html; charset=UTF-8" buffer="64kb"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://localhost/customtag" prefix="tags"%>
<%@ taglib uri="http://localhost/sigatags" prefix="siga"%>
<%@ taglib uri="http://localhost/functiontag" prefix="f"%>


<siga:pagina titulo="Movimentação" onLoad="javascript: TestarAssinaturaDigital();">

	<c:if test="${not mob.doc.eletronico}">
		<script type="text/javascript">
			$("html").addClass("fisico");
		</script>
	</c:if>
	<c:if
		test="${f:podeUtilizarServicoPorConfiguracao(titular,lotaTitular,'SIGA:Sistema Integrado de Gestão Administrativa;DOC:Módulo de Documentos;ASS:Assinatura digital;VBS:VBScript e CAPICOM')}">
		<c:import url="/WEB-INF/page/exMovimentacao/inc_assina_js.jsp" />
	</c:if>

	<script type="text/javascript" language="Javascript1.1">
		var frm = document.getElementById('frm');
		function sbmt() {
			ExMovimentacaoForm.page.value = '';
			ExMovimentacaoForm.acao.value = 'aAnexar';
			ExMovimentacaoForm.submit();
		}

		function testpdf(x) {
			padrao = /\.pdf/;
			a = x.arquivo.value;
			OK = padrao.exec(a);
			if (a != '' && !OK) {
				window.alert("Somente é permitido anexar arquivo PDF!");
				x.arquivo.value = '';
				x.arquivo.focus();
			}
		}

		function checkUncheckAll(theElement) {
			var theForm = theElement.form, z = 0;
			for (z = 0; z < theForm.length; z++) {
				if (theForm[z].type == 'checkbox'
						&& theForm[z].name != 'checkall') {
					theForm[z].checked = !(theElement.checked);
					theForm[z].click();
				}
			}
		}

		function montaTableAssinados(carregaDiv) {
			if (carregaDiv == true) {
				$('#tableAssinados').html('Carregando...');
				$
						.ajax({
							url : '/sigaex/app/expediente/mov/mostrar_anexos_assinados?sigla=${mobilVO.sigla}',
							success : function(data) {
								$('#tableAssinados').html(data);
							}
						});
			} else
				($('#tableAssinados').html(''));
		}

		/**
		 * Valida se o anexo foi selecionado ao clicar em OK
		 */
		function validaSelecaoAnexo(form) {
			var result = true;
			var arquivo = form.arquivo;
			if (arquivo == null || arquivo.value == '') {
				alert("O arquivo a ser anexado não foi selecionado!");
				result = false;
			}
			return result;
		}
	</script>

	<div class="gt-bd clearfix">
		<div class="gt-content clearfix">
			<c:if test="${!assinandoAnexosGeral}">
				<h2>Anexação de Arquivo - ${mob.siglaEDescricaoCompleta}</h2>
				<div class="gt-content-box gt-for-table">
					<form action="anexar_gravar" method="POST" enctype="multipart/form-data" class="form">

						<input type="hidden" name="postback" value="1" /> 
						<input type="hidden" name="sigla" value="${sigla}"/>

						<table class="gt-form-table">
							<tr class="header">
								<td colspan="2">Dados do Arquivo</td>
							</tr>
							<tr>
								<td></td>
							</tr>
							<tr>
								<td><label>Data:</label></td>
								<td><input type="text" name="dtMovString"
									onblur="javascript:verifica_data(this, true);" /></td>
							</tr>
							<tr>
								<td>Responsável:</td>
								<td><siga:selecao tema="simple" propriedade="subscritor" modulo="siga" /> &nbsp;&nbsp;
								<input type="checkbox" name="substituicao" onclick="javascript:displayTitular(this);" /> Substituto</td>
							</tr>
							<c:choose>
								<c:when test="${!substituicao}">
									<tr id="tr_titular" style="display: none">
								</c:when>
								<c:otherwise>
									<tr id="tr_titular" style="">
								</c:otherwise>
							</c:choose>
	
								<td>Titular:</td>
								<input type="hidden" name="campos" value="titularSel.id" />
								<td colspan="3"><siga:selecao propriedade="titular" tema="simple" modulo="siga" /></td>
							</tr>
							<tr>
								<td>Descrição:</td>
								<td><input type="text" name="descrMov" maxlength="80" size="80" value="${descrMov}" /></td>
							</tr>
							<tr>
								<td><label>Arquivo:</label></td>
								<td><input type="file" name="arquivo" accept="application/pdf" onchange="testpdf(this.form)" /></td>
							</tr>

							<c:set var="pendencias" value="${false}" />
							<c:forEach var="mov" items="${mobilCompletoVO.movs}">
								<c:if test="${(not mov.cancelada) and (mov.idTpMov eq 57)}">
									<c:set var="pendencias" value="${true}" />
								</c:if>
							</c:forEach>
							<c:if test="${pendencias}">
								<tr class="header">
									<td colspan="2">Pendencias de Anexação</td>
								</tr>
								<tr>
									<td colspan="2">
										<div class="gt-form">
											<label>A anexação deste arquivo resolve as seguintes pendências:</label>
											<c:forEach var="mov" items="${mobilCompletoVO.movs}">
												<c:if test="${(not mov.cancelada) and (mov.idTpMov eq 57)}">
		     										<c:set var="checkBoxSelecionada" value=""/>
		     										
		     										<c:if test="${(empty idMovPendencia)}">
													   <c:set var="checkBoxSelecionada" value="checked"/>
		     										</c:if>
		     										
													<c:if test="${(mov.idMov == idMovPendencia)}">
													   <c:set var="checkBoxSelecionada" value="checked"/>
													</c:if>
												
													<label class="gt-form-element-label"><input type="checkbox"
														class="gt-form-checkbox" name="pendencia_de_anexacao" value="${mov.idMov}" ${checkBoxSelecionada}/>
															${mov.descricao}</label>
												</c:if>
											</c:forEach>
										</div>
									</td>
								</tr>
							</c:if>

							<tr>
								<td colspan="2">
									<input type="submit" value="Ok" class="gt-btn-medium gt-btn-left"
										onclick="javascript: return validaSelecaoAnexo( this.form );" /> 
									<input type="button"
										value="Voltar"
										onclick="javascript:window.location.href='/sigaex/app/expediente/doc/exibir?sigla=${mobilVO.sigla}'"
										class="gt-btn-medium gt-btn-left" /> 
									<br /> 
									<input type="checkbox" name="check" onclick="javascript:montaTableAssinados(check.checked);" /> 
										<b>Exibir anexos assinados</b>
								</td>
							</tr>
						</table>
					</form>
				</div>
			</c:if>

			<c:choose>
				<c:when test="${(not empty mobilVO.movs)}">
					<c:if test="${assinandoAnexosGeral}">
						<input style="display: inline" type="checkbox" name="check"
							onclick="javascript:montaTableAssinados(check.checked);" />
						<b>Exibir anexos assinados</b>
						<br />
					</c:if>
					<br />
					<h2> Anexos Pendentes de Assinatura
					<c:if test="${assinandoAnexosGeral}">
					      - ${mob.siglaEDescricaoCompleta}
					</c:if>
					</h2>
					<div class="gt-content-box gt-for-table">
						<form name="frm_anexo" id="frm_anexo" class="form">
							<input type="hidden" name="popup" value="true" /> 
							<input type="hidden" name="copia" id="copia" value="false" />
		
							<table class="gt-table mov">
								<thead>
									<tr>
										<td></td>
										<th align="center" rowspan="2">&nbsp;&nbsp;&nbsp;Data</th>
										<th colspan="2" align="center">Cadastrante</th>
										<th colspan="2" align="center">Atendente</th>
										<th rowspan="2">Descrição</th>
									</tr>
									<tr>
										<c:choose>
											<c:when
												test="${f:podeUtilizarServicoPorConfiguracao(titular,lotaTitular,'SIGA:Sistema Integrado de Gestão Administrativa;DOC:Módulo de Documentos;ASS:Assinatura digital;EXT:Extensão')}">
												<td align="center"><input type="checkbox" name="checkall"
													onclick="checkUncheckAll(this)" /></td>
											</c:when>
											<c:otherwise>
												<td></td>
											</c:otherwise>
										</c:choose>
										<th align="left">Lotação</th>
										<th align="left">Pessoa</th>
										<th align="left">Lotação</th>
										<th align="left">Pessoa</th>
									</tr>
								</thead>
								<c:set var="i" value="${0}" />
								<c:forEach var="mov" items="${mobilVO.movs}">
									<c:if test="${(not mov.cancelada)}">
										<tr class="${mov.classe} ${mov.disabled}">
											<c:set var="dt" value="${mov.dtRegMovDDMMYY}" />
											<c:choose>
												<c:when test="${dt == dtUlt}">
													<c:set var="dt" value="" />
												</c:when>
												<c:otherwise>
													<c:set var="dtUlt" value="${dt}" />
												</c:otherwise>
											</c:choose>
											<c:choose>
												<c:when
													test="${f:podeUtilizarServicoPorConfiguracao(titular,lotaTitular,'SIGA:Sistema Integrado de Gestão Administrativa;DOC:Módulo de Documentos;ASS:Assinatura digital;EXT:Extensão')}">
													<c:set var="x" scope="request">chk_${mov.mov.idMov}</c:set>
													<c:remove var="x_checked" scope="request" />
													<c:if test="${param[x] == 'true'}">
														<c:set var="x_checked" scope="request">checked</c:set>
													</c:if>
													<td align="center"><input type="checkbox" name="${x}" value="true" ${x_checked} /></td>
												</c:when>
												<c:otherwise>
													<td></td>
												</c:otherwise>
											</c:choose>
											<td align="center">${dt}</td>
											<td align="left"><siga:selecionado sigla="${mov.parte.lotaCadastrante.sigla}"
													descricao="${mov.parte.lotaCadastrante.descricaoAmpliada}" /></td>
											<td align="left"><siga:selecionado sigla="${mov.parte.cadastrante.nomeAbreviado}"
													descricao="${mov.parte.cadastrante.descricao} - ${mov.parte.cadastrante.sigla}" /></td>
											<td align="left"><siga:selecionado sigla="${mov.parte.lotaResp.sigla}"
													descricao="${mov.parte.lotaResp.descricaoAmpliada}" /></td>
											<td align="left"><siga:selecionado sigla="${mov.parte.resp.nomeAbreviado}"
													descricao="${mov.parte.resp.descricao} - ${mov.parte.resp.sigla}" /></td>
											<td>${mov.descricao}
												<c:if test='${mov.idTpMov != 2}'> ${mov.complemento}</c:if> 
												<c:set var="assinadopor" value="${true}" /> 
												<siga:links inline="${true}" separator="${not empty mov.descricao and mov.descricao != null}">
													<c:forEach var="acao" items="${mov.acoes}">
														<c:choose>
															<c:when test='${mov.idTpMov == 32}'>
																<c:url var="url" value="${acao.nameSpace}/${acao.acao}">
														     		<c:forEach var="p" items="${acao.params}">
															     		<c:param name="${p.key}" value="${p.value}"/>
																    </c:forEach>
															     </c:url>
															</c:when>
														    <c:otherwise>
															    <c:url var="url" value="${acao.nameSpace}/${acao.acao}">
												                    <c:forEach var="p" items="${acao.params}">
															     		<c:param name="${p.key}" value="${p.value}"/>
																    </c:forEach>
															    </c:url>
														    </c:otherwise>
														</c:choose>
														<siga:link title="${acao.nomeNbsp}" pre="${acao.pre}" pos="${acao.pos}" url="${pageContext.request.contextPath}${acao.url}"
															test="${true}" popup="${acao.popup}" confirm="${acao.msgConfirmacao}"
															ajax="${acao.ajax}" idAjax="${mov.idMov}" />
														<c:if test='${assinadopor and mov.idTpMov == 2}'> ${mov.complemento}
															    <c:set var="assinadopor" value="${false}" />
														</c:if>
													</c:forEach>
												</siga:links> 
												<c:if
												test="${f:podeUtilizarServicoPorConfiguracao(titular,lotaTitular,'SIGA:Sistema Integrado de Gestão Administrativa;DOC:Módulo de Documentos;ASS:Assinatura digital;EXT:Extensão')}">
													<input type="hidden" name="pdf${x}" value="${mov.mov.referencia}" />
													<input type="hidden" name="url${x}"
														value="/app/arquivo/exibir?arquivo=${mov.mov.nmPdf}" />
												</c:if>
											</td>
										</tr>
									</c:if>
								</c:forEach>
							</table>
						</form>
					</div>
					<br />
					<div id="dados-assinatura" style="visible: hidden">
						<c:set var="jspServer"
							value="${request.scheme}://${request.serverName}:${request.localPort}/${request.contextPath}/app/expediente/mov/assinar_mov_gravar" />
						<c:set var="nextURL"
							value="${request.scheme}://${request.serverName}:${request.localPort}/${request.contextPath}/app/expediente/doc/atualizar_marcas?sigla=${mobilVO.sigla}" />
						<c:set var="urlPath" value="/${request.contextPath}" />
		
						<input type="hidden" id="jspserver" name="jspserver" value="${jspServer}" /> <input
							type="hidden" id="nexturl" name="nextUrl" value="${nextURL}" /> <input type="hidden"
							id="urlpath" name="urlpath" value="${urlPath}" />
						<c:set var="urlBase" value="${request.scheme}://${request.serverName}:${request.serverPort}" />
						<input type="hidden" id="urlbase" name="urlbase" value="${urlBase}" />
		
						<c:set var="botao" value="ambos" />
						<c:set var="lote" value="true" />
					</div>
					<c:if
						test="${f:podeUtilizarServicoPorConfiguracao(titular,lotaTitular,'SIGA:Sistema Integrado de Gestão Administrativa;DOC:Módulo de Documentos;ASS:Assinatura digital;VBS:VBScript e CAPICOM')}">
						<div id="capicom-div">
							<a id="bot-conferir" href="#" onclick="javascript: AssinarDocumentos('true', this);"
								class="gt-btn-alternate-large gt-btn-left">Conferir Cópia em Lote</a> <a id="bot-assinar"
								href="#" onclick="javascript: AssinarDocumentos('false', this);"
								class="gt-btn-alternate-large gt-btn-left">Assinar em Lote</a>
						</div>
						<p id="ie-missing" style="display: none;">
							A assinatura digital utilizando padrão do SIGA-DOC só poderá ser realizada no Internet
							Explorer. No navegador atual, apenas a assinatura com <i>Applet Java</i> é permitida.
						</p>
						<p id="capicom-missing" style="display: none;">
							Não foi possível localizar o componente <i>CAPICOM.DLL</i>. Para realizar assinaturas digitais
							utilizando o método padrão do SIGA-DOC, será necessário instalar este componente. O <i>download</i>
							pode ser realizado clicando <a
								href="https://code.google.com/p/projeto-siga/downloads/detail?name=Capicom.zip&can=2&q=#makechanges">aqui</a>.
							Será necessário expandir o <i>ZIP</i> e depois executar o arquivo de instalação.
						</p>
						<script type="text/javascript">
							if (window.navigator.userAgent.indexOf("MSIE ") > 0
									|| window.navigator.userAgent.indexOf(" rv:11.0") > 0) {
								document.getElementById("capicom-div").style.display = "block";
								document.getElementById("ie-missing").style.display = "none";
							} else {
								document.getElementById("capicom-div").style.display = "none";
								document.getElementById("ie-missing").style.display = "block";
							}
						</script>
					</c:if>
					<c:if
						test="${f:podeUtilizarServicoPorConfiguracao(titular,lotaTitular,'SIGA:Sistema Integrado de Gestão Administrativa;DOC:Módulo de Documentos;ASS:Assinatura digital;EXT:Extensão')}">
						    ${f:obterExtensaoAssinador(lotaTitular.orgaoUsuario,request.scheme,request.serverName,request.localPort,urlPath,jspServer,nextURL,botao,lote)}						
					</c:if>
				</div>
			</c:when>
			<c:otherwise>
				<c:if test="${assinandoAnexosGeral}">
					<script language="javascript">
						montaTableAssinados(true);
					</script>
				</c:if>
			</c:otherwise>
		</c:choose>
		<div class="gt-content clearfix">
			<div id="tableAssinados"></div>
		</div>
	</div>

</siga:pagina>
