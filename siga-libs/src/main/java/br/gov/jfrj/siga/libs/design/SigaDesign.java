package br.gov.jfrj.siga.libs.design;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.mvel2.templates.CompiledTemplate;
import org.mvel2.templates.TemplateCompiler;
import org.mvel2.templates.TemplateRuntime;

import br.gov.jfrj.siga.cp.bl.Cp;
import br.gov.jfrj.siga.cp.bl.CpConfiguracaoBL;
import br.gov.jfrj.siga.libs.util.SigaLibsEL;

public class SigaDesign {

	static CompiledTemplate tCabecalho = template("cabecalho.html");
	static CompiledTemplate tRodape = template("rodape.html");
	static SigaLibsEL sigalibsel = new SigaLibsEL();

	private static CompiledTemplate template(String file) {
		String template;
		try {
			InputStream stream = SigaDesign.class
					.getClassLoader()
					.getResourceAsStream("br/gov/jfrj/siga/libs/design/" + file);
			template = IOUtils.toString(stream);
			CompiledTemplate compiled = TemplateCompiler
					.compileTemplate(template);
			return compiled;
		} catch (IOException e) {
			return null;
		}
	}

	public static void main(String[] args) {
		// <c:set var="ambiente">
		// <c:if
		// test="${f:resource('isVersionTest') or f:resource('isBaseTest')}">
		// <c:if test="${f:resource('isVersionTest')}">SISTEMA</c:if>
		// <c:if
		// test="${f:resource('isVersionTest') and f:resource('isBaseTest')}"> E
		// </c:if>
		// <c:if test="${f:resource('isBaseTest')}">BASE</c:if> DE TESTES
		// </c:if>
		// </c:set>
		// <c:if test="${not empty ambiente}">
		// <c:set var="env" scope="request">${ambiente}</c:set>
		// </c:if>

		cabecalho("titulo", null, null, null, null, false, false, false);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static String cabecalho(String titulo, String ambiente, String meta,
			String js, String onLoad, boolean popup, boolean desabilitarBusca,
			boolean desabilitarMenu) {

		Map vars = new HashMap();
		vars.put("titulo_pagina", titulo);
		vars.put("meta", meta);
		vars.put("incluir_js", js);
		vars.put("on_load", onLoad);

		vars.put("popup", popup);
		vars.put("desabilitar_busca", desabilitarBusca);
		vars.put("desabilitar_menu", desabilitarMenu);

		vars.put("cadastrante", null);
		vars.put("titular", null);
		vars.put("meus_titulares", null);
		vars.put("f", sigalibsel);

		vars.put("pagina_de_erro", false);
		vars.put("menu_principal", menuPrincipal());

		String output = (String) TemplateRuntime.execute(tCabecalho, vars);
		System.out.println(output);
		return output;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static String rodape(boolean popup, boolean paginaDeErro) {

		Map vars = new HashMap();
		vars.put("popup", popup);
		vars.put("pagina_de_erro", paginaDeErro);
		vars.put("f", sigalibsel);

		String output = (String) TemplateRuntime.execute(tRodape, vars);
		System.out.println(output);

		return output;
	}

	public static List<Menu> menuPrincipal() {
		return new Menu.MenuBuilder()
				.menu("sitemap", "M�dulos", null, true)

				.item("cube", "Documentos",
						"/sigaex/app/expediente/doc/listar?primeiraVez=sim",
						"SIGA;DOC:M�dulo de Documentos")
				.item("cube", "Workflow", "/sigawf/app/resumo",
						"SIGA;WF:M�dulo de Workflow")
				.item("cube", "Servi�os", "/sigasr/", "SIGA;SR")
				.item("cube", "Gest�o de Conhecimento",
						"/sigagc/app/estatisticaGeral",
						"SIGA;GC:M�dulo de Gest�o de Conhecimento")

				.item("plus-square", "Pessoas", null, true)

				.subitem("cube", "AQ", SigaLibsEL.getURLSistema("siga.sgp.aq"),
						"SIGA;AQ: M�dulo de Adicional de Qualifica��o")

				.subitem("cube", "Benef�cios",
						SigaLibsEL.getURLSistema("siga.sgp.bnf"),
						"SIGA;BNF: M�dulo de Benef�cios")

				.subitem("cube", "Cadastro",
						SigaLibsEL.getURLSistema("siga.sgp.cad"),
						"SIGA;CAD: M�dulo de Cadastro")

				.subitem("cube", "Consultas",
						SigaLibsEL.getURLSistema("siga.sgp.cst"),
						"SIGA;CST: M�dulo de Consultas")

				.subitem("cube", "Lota��o",
						SigaLibsEL.getURLSistema("siga.sgp.lot"),
						"SIGA;LOT: M�dulo de Lota��o")

				.subitem("cube", "Treinamento",
						SigaLibsEL.getURLSistema("siga.sgp.trn"),
						"SIGA;TRN: M�dulo de Treinamento")

				.item("cube", "Agenda de Per�cias M�dicas", "/sigapp/",
						"SIGA;PP:Agendamento de per�cias do INSS")

				.item("cube", "Transportes", "/sigatp/",
						"SIGA;TP:M�dulo de Transportes")

				.menu("user",
						"Gest�o de Identidade",
						null,
						"SIGA:Sistema Integrado de Gest�o Administrativa;GI:M�dulo de Gest�o de Identidade")

				.item("cube", "Identidade",
						"/siga/gi/identidade/listar.action",
						"SIGA;GI;ID:Gerenciar identidades")
				.item("cube", "Configurar Permiss�es",
						"/siga/gi/acesso/listar.action",
						"SIGA;GI;PERMISSAO:Gerenciar permiss�es")
				.item("cube", "Perfil de Acesso",
						"/siga/gi/perfil/listar.action",
						"SIGA;GI;PERFIL:Gerenciar perfis de acesso")
				.item("cube", "Perfil de Acesso do JEE",
						"/siga/gi/perfiljee/listar.action",
						"SIGA;GI;PERFILJEE:Gerenciar perfis do JEE")
				.item("cube", "Grupo de Distribui��o",
						"/siga/gi/email/listar.action",
						"SIGA;GI;GDISTR:Gerenciar grupos de distribui��o")
				.item("cube", "Acesso a Servi�os",
						"/siga/gi/servico/acesso.action",
						"SIGA;GI;SELFSERVICE:Gerenciar servi�os da pr�pria lota��o")

				.item("dashboard", "Relat�rios",
						null,
						"SIGA;GI;REL:Gerar relat�rios")
				.subitem("cube", "Acesso aos Servi�os",
						"/siga/gi/relatorio/selecionar_acesso_servico.action",
						true)
				.subitem(
						"cube",
						"Permiss�es de Usu�rio",
						"/siga/gi/relatorio/selecionar_permissao_usuario.action",
						true)
				.subitem(
						"cube",
						"Altera��o de Direitos",
						"/siga/gi/relatorio/selecionar_alteracao_direitos.action",
						true)
				.subitem(
						"cube",
						"Hist�rico de Usu�rio",
						"/siga/gi/relatorio/selecionar_historico_usuario.action",
						true)

				.menu("cog", "Ferramentas", null,
						"SIGA:Sistema Integrado de Gest�o Administrativa;FE:Ferramentas")
				.item("cube", "Cadastro de modelos",
						"/siga/modelo/listar.action",
						"SIGA;FE;MODVER:Visualizar modelos")
				.item("cube", "Cadastro de �rg�os Externos",
						"/siga/orgao/listar.action",
						"SIGA;FE;CAD_ORGAO:Cadastrar �rg�os")
				.item("cube", "Administrar SIGA WF",
						"/sigawf/administrar.action",
						"SIGA;FE;WF_ADMIN:Administrar SIGAWF")
				.item("cube", "Cadastro de Feriados",
						"/siga/feriado/listar.action",
						"SIGA;FE;CAD_FERIADO:Cadastrar Feriados")

				.build();
	}

}
