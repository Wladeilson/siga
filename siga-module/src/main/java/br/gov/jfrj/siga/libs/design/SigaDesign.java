package br.gov.jfrj.siga.libs.design;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.mvel2.templates.CompiledTemplate;
import org.mvel2.templates.TemplateCompiler;
import org.mvel2.templates.TemplateRuntime;

public class SigaDesign {

	static CompiledTemplate tPagina = template("pagina.html");

	private static CompiledTemplate template(String file) {
		String template;
		try {
			InputStream stream = SigaDesign.class
					.getClassLoader()
					.getResourceAsStream("br/gov/jfrj/siga/libs/design/" + file);
			template = IOUtils.toString(stream, "UTF-8");
			CompiledTemplate compiled = TemplateCompiler
					.compileTemplate(template);
			return compiled;
		} catch (IOException e) {
			return null;
		}
	}

	public static String getJSFHeader() {

		return null;
	}

	public static void main(String[] args) {
		Map<String, Boolean> permissoes = new HashMap<>();
		List<Menu> menu = new ArrayList<>();
		List<Substituicao> substituicoes = new ArrayList<>();

		String s = pagina("titulo", null, null, null, null, false, false,
				false, "RENATO DO AMARAL CRIVANO MACHADO", "RJSESIA",
				"RENATO DO AMARAL CRIVANO MACHADO", "RJSESIA", permissoes,
				menu, substituicoes, null);

		String r = rodape(s);
		System.out.println(r);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static String pagina(String titulo, String ambiente, String meta,
			String js, String onLoad, boolean popup, boolean desabilitarBusca,
			boolean desabilitarMenu, String nomeCadastrante,
			String siglaLotaCadastrante, String nomeTitular,
			String siglaLotaTitular, Map<String, Boolean> permissoes,
			List<Menu> menu, List<Substituicao> substituicoes,
			String complementoHead) {

		if (nomeTitular != null && nomeTitular.equals(nomeCadastrante))
			nomeTitular = null;
		if (siglaLotaTitular != null
				&& siglaLotaTitular.equals(siglaLotaCadastrante))
			siglaLotaTitular = null;

		List<Menu> menus = menuPrincipal();
		if (menu != null)
			menus.addAll(menu);

		Map vars = new HashMap();
		vars.put("titulo_pagina", titulo);
		vars.put("meta", meta);
		vars.put("incluir_js", js);
		vars.put("on_load", onLoad);

		vars.put("popup", popup);
		vars.put("desabilitar_busca", desabilitarBusca);
		vars.put("desabilitar_menu", desabilitarMenu);

		vars.put("cadastrante_nome", primeiroNomeEIniciais(nomeCadastrante));
		vars.put("cadastrante_lotacao_sigla", siglaLotaCadastrante);
		vars.put("titular_nome", primeiroNomeEIniciais(nomeTitular));
		vars.put("titular_lotacao_sigla", siglaLotaTitular);
		vars.put("substituicoes", substituicoes);

		vars.put("pagina_de_erro", false);
		vars.put("menu_principal", menus);
		vars.put("head_complemento", complementoHead);

		// tCabecalho = reler("cabecalho.html");
		String output = (String) TemplateRuntime.execute(tPagina, vars);
		// System.out.println(output);
		return output;
	}

	private static String split(String pagina, String ini, String fim) {
		String s = ini == null ? pagina : pagina.substring(pagina.indexOf(ini)
				+ ini.length());
		if (fim != null)
			s = s.substring(0, s.indexOf(fim));
		return s;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static String cabecalho(String pagina) {
		return split(pagina, null, "<!-- fim cabecalho -->");
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static String rodape(String pagina) {
		return split(pagina, "<!-- inicio rodape -->", null);
	}

	public static String cabecalhoHead(String pagina) {
		String s = cabecalho(pagina);
		s = s.substring(s.indexOf("<head>") + 6);
		s = s.substring(0, s.indexOf("</head>"));

		return s;
	}

	public static String cabecalhoBody(String pagina) {
		String s = cabecalho(pagina);
		int iBody = s.indexOf("<body");
		s = s.substring(s.indexOf(">", iBody) + 1);

		return s;
	}

	public static String rodapeBody(String pagina) {
		String s = rodape(pagina);
		s = s.substring(0, s.indexOf("</body>"));
		return s;
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

				.subitem("cube", "AQ", "/sigaaq/", // SigaLibsEL.getURLSistema("siga.sgp.aq"),
						"SIGA;AQ: M�dulo de Adicional de Qualifica��o")

				.subitem("cube", "Benef�cios", "/sigabnf/", // SigaLibsEL.getURLSistema("siga.sgp.bnf"),
						"SIGA;BNF: M�dulo de Benef�cios")

				.subitem("cube", "Cadastro", "/sigacad/", // SigaLibsEL.getURLSistema("siga.sgp.cad"),
						"SIGA;CAD: M�dulo de Cadastro")

				.subitem("cube", "Consultas", "/sigacst/", // SigaLibsEL.getURLSistema("siga.sgp.cst"),
						"SIGA;CST: M�dulo de Consultas")

				.subitem("cube", "Lota��o", "/sigalot/", // SigaLibsEL.getURLSistema("siga.sgp.lot"),
						"SIGA;LOT: M�dulo de Lota��o")

				.subitem("cube", "Treinamento", "/sigatrn/", // SigaLibsEL.getURLSistema("siga.sgp.trn"),
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

				.item("dashboard", "Relat�rios", null,
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

	public static String primeiroNomeEIniciais(String nome) {
		if (nome == null)
			return "";
		String a[] = nome.split(" ");

		String nomeAbreviado = "";
		for (String n : a) {
			if (nomeAbreviado.length() == 0)
				nomeAbreviado = n.substring(0, 1).toUpperCase()
						+ n.substring(1).toLowerCase();
			else if (!"|DA|DE|DO|DAS|DOS|E|".contains("|" + n + "|"))
				nomeAbreviado += " " + n.substring(0, 1) + ".";
		}
		return nomeAbreviado;
	}

	// Ativar apenas para fins de debug, pois os templates s�o empacotados no
	// JAR e n�o s�o relidos a menos que o JAR seja recompilado. Durante as
	// altera��es, conv�m chamar essa fun��o antes de processar o template. �
	// necess�rio ajustar o path para o diret�rio onde os templates est�o sendo
	// editados na m�quina do desenvolvedor.
	protected static CompiledTemplate reler(String template) {
		byte[] encoded;
		try {
			encoded = Files
					.readAllBytes(Paths
							.get("/Users/nato/Repositories/projeto-siga/siga/siga-module/src/main/java/br/gov/jfrj/siga/libs/design/"
									+ template));
			return TemplateCompiler
					.compileTemplate(new String(encoded, "UTF-8"));
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

}
