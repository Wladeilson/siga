/*******************************************************************************
 * Copyright (c) 2006 - 2011 SJRJ.
 * 
 *     This file is part of SIGA.
 * 
 *     SIGA is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 * 
 *     SIGA is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 * 
 *     You should have received a copy of the GNU General Public License
 *     along with SIGA.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package br.gov.jfrj.siga.vraptor;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.persistence.EntityManager;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import br.com.caelum.vraptor.Result;
import br.gov.jfrj.siga.base.AplicacaoException;
import br.gov.jfrj.siga.cp.CpConfiguracao;
import br.gov.jfrj.siga.cp.CpSituacaoConfiguracao;
import br.gov.jfrj.siga.cp.CpTipoConfiguracao;
import br.gov.jfrj.siga.dp.dao.CpDao;
import br.gov.jfrj.siga.ex.ExClassificacao;
import br.gov.jfrj.siga.ex.ExConfiguracao;
import br.gov.jfrj.siga.ex.ExDocumento;
import br.gov.jfrj.siga.ex.ExEstadoDoc;
import br.gov.jfrj.siga.ex.ExFormaDocumento;
import br.gov.jfrj.siga.ex.ExMobil;
import br.gov.jfrj.siga.ex.ExModelo;
import br.gov.jfrj.siga.ex.ExMovimentacao;
import br.gov.jfrj.siga.ex.ExNivelAcesso;
import br.gov.jfrj.siga.ex.ExTipoDocumento;
import br.gov.jfrj.siga.ex.bl.CurrentRequest;
import br.gov.jfrj.siga.ex.bl.Ex;
import br.gov.jfrj.siga.ex.bl.ExConfiguracaoBL;
import br.gov.jfrj.siga.ex.bl.RequestInfo;
import br.gov.jfrj.siga.hibernate.ExDao;
import br.gov.jfrj.siga.libs.design.Menu;
import br.gov.jfrj.siga.util.ExProcessadorModelo;

public class ExController extends SigaController {
	
	HttpServletResponse response;
	ServletContext context;
	
	static {
		if (Ex.getInstance().getBL().getProcessadorModeloJsp() == null) {
			Ex.getInstance().getBL().setProcessadorModeloJsp(new ExProcessadorModelo());
		}
	}
	
	public ExController(HttpServletRequest request, HttpServletResponse response, ServletContext context, Result result, CpDao dao, SigaObjects so, EntityManager em) {
		super(request, result, dao, so, em);
		this.response = response;
		this.context = context;
		
		incluirAtributosComuns(result);
		
		CurrentRequest.set(new RequestInfo(context, request, response));
	}

	protected static void incluirAtributosComuns(Result result) {
		List<Menu> menus = new Menu.MenuBuilder()
		.menu("cube", "Documentos", null, true)
		.item("cube", "Novo", null, true)
		.build();
		result.include("menus", menus);
	}

	protected void verificaNivelAcesso(ExMobil mob) {
		if (!Ex.getInstance().getComp()
				.podeAcessarDocumento(getTitular(), getLotaTitular(), mob)) {
			throw new AplicacaoException("Acesso ao documento "
					+ mob.getSigla()
					+ " permitido somente a usu�rios autorizados. ("
					+ getTitular().getSigla() + "/"
					+ getLotaTitular().getSiglaCompleta() + ")");
		}
	}

	public String getNomeServidor() {
		return getRequest().getServerName();
	}

	public String getNomeServidorComPorta() {
		if (getRequest().getServerPort() > 0)
			return getRequest().getServerName() + ":"+ getRequest().getServerPort();
		return getRequest().getServerName();
	}

	public List<ExNivelAcesso> getListaNivelAcesso(ExTipoDocumento exTpDoc,
			ExFormaDocumento forma, ExModelo exMod, ExClassificacao classif) {
		List<ExNivelAcesso> listaNiveis = ExDao.getInstance()
				.listarOrdemNivel();
		ArrayList<ExNivelAcesso> niveisFinal = new ArrayList<ExNivelAcesso>();
		Date dt = ExDao.getInstance().consultarDataEHoraDoServidor();

		ExConfiguracao config = new ExConfiguracao();
		CpTipoConfiguracao exTpConfig = new CpTipoConfiguracao();
		config.setDpPessoa(getTitular());
		config.setLotacao(getLotaTitular());
		config.setExTipoDocumento(exTpDoc);
		config.setExFormaDocumento(forma);
		config.setExModelo(exMod);
		config.setExClassificacao(classif);
		exTpConfig
				.setIdTpConfiguracao(CpTipoConfiguracao.TIPO_CONFIG_NIVEL_ACESSO_MINIMO);
		config.setCpTipoConfiguracao(exTpConfig);
		int nivelMinimo = ((ExConfiguracao) Ex
				.getInstance()
				.getConf()
				.buscaConfiguracao(config,
						new int[] { ExConfiguracaoBL.NIVEL_ACESSO }, dt))
				.getExNivelAcesso().getGrauNivelAcesso();
		exTpConfig
				.setIdTpConfiguracao(CpTipoConfiguracao.TIPO_CONFIG_NIVEL_ACESSO_MAXIMO);
		config.setCpTipoConfiguracao(exTpConfig);
		int nivelMaximo = ((ExConfiguracao) Ex
				.getInstance()
				.getConf()
				.buscaConfiguracao(config,
						new int[] { ExConfiguracaoBL.NIVEL_ACESSO }, dt))
				.getExNivelAcesso().getGrauNivelAcesso();

		for (ExNivelAcesso nivelAcesso : listaNiveis) {
			if (nivelAcesso.getGrauNivelAcesso() >= nivelMinimo
					&& nivelAcesso.getGrauNivelAcesso() <= nivelMaximo)
				niveisFinal.add(nivelAcesso);
		}

		return niveisFinal;
	}
	
	public ExNivelAcesso getNivelAcessoDefault(ExTipoDocumento exTpDoc,
			ExFormaDocumento forma, ExModelo exMod, ExClassificacao classif)
			throws Exception {
		Date dt = ExDao.getInstance().consultarDataEHoraDoServidor();
		
		ExConfiguracao config = new ExConfiguracao();
		CpTipoConfiguracao exTpConfig = new CpTipoConfiguracao();
		CpSituacaoConfiguracao exStConfig = new CpSituacaoConfiguracao();
		config.setDpPessoa(getTitular());
		config.setLotacao(getLotaTitular());
		config.setExTipoDocumento(exTpDoc);
		config.setExFormaDocumento(forma);
		config.setExModelo(exMod);
		config.setExClassificacao(classif);
		exTpConfig
				.setIdTpConfiguracao(CpTipoConfiguracao.TIPO_CONFIG_NIVELACESSO);
		config.setCpTipoConfiguracao(exTpConfig);
		exStConfig
			.setIdSitConfiguracao(CpSituacaoConfiguracao.SITUACAO_DEFAULT);
		config.setCpSituacaoConfiguracao(exStConfig);
		ExConfiguracao exConfig;

/*		exConfig = ((ExConfiguracao) Ex
				.getInstance()
				.getConf()
				.buscaConfiguracao(config,
						new int[] { ExConfiguracaoBL.NIVEL_ACESSO }, dt));*/
		
		try {
			exConfig = criarExConfiguracaoPorCpConfiguracao(Ex.getInstance().getConf().buscaConfiguracao(config, new int[] {ExConfiguracaoBL.NIVEL_ACESSO}, dt));
		} catch (Exception e) {
			exConfig = null;
		}
		
		if(exConfig != null)
			return exConfig.getExNivelAcesso();
		
		return null;
	}
	
	public ExConfiguracao criarExConfiguracaoPorCpConfiguracao(CpConfiguracao configuracaoBaseParaExConfiguracao){
		ExConfiguracao exConfiguracao = new ExConfiguracao();
		
		if (configuracaoBaseParaExConfiguracao.isAtivo())
			exConfiguracao.setAtivo();
		exConfiguracao.setCargo(configuracaoBaseParaExConfiguracao.getCargo());
		exConfiguracao.setComplexo(configuracaoBaseParaExConfiguracao.getComplexo());
		exConfiguracao.setConfiguracaoInicial(configuracaoBaseParaExConfiguracao.getConfiguracaoInicial());
		exConfiguracao.setConfiguracoesPosteriores(configuracaoBaseParaExConfiguracao.getConfiguracoesPosteriores());
		exConfiguracao.setCpGrupo(configuracaoBaseParaExConfiguracao.getCpGrupo());
		exConfiguracao.setCpIdentidade(configuracaoBaseParaExConfiguracao.getCpIdentidade());
		exConfiguracao.setCpServico(configuracaoBaseParaExConfiguracao.getCpServico());
		exConfiguracao.setCpSituacaoConfiguracao(configuracaoBaseParaExConfiguracao.getCpSituacaoConfiguracao());
		exConfiguracao.setCpTipoConfiguracao(configuracaoBaseParaExConfiguracao.getCpTipoConfiguracao());
		exConfiguracao.setCpTipoLotacao(configuracaoBaseParaExConfiguracao.getCpTipoLotacao());
		exConfiguracao.setDpPessoa(configuracaoBaseParaExConfiguracao.getDpPessoa());
		exConfiguracao.setDscFormula(configuracaoBaseParaExConfiguracao.getDscFormula());
		exConfiguracao.setDtFimVigConfiguracao(configuracaoBaseParaExConfiguracao.getDtFimVigConfiguracao());
		exConfiguracao.setDtIniVigConfiguracao(configuracaoBaseParaExConfiguracao.getDtIniVigConfiguracao());
		exConfiguracao.setFuncaoConfianca(configuracaoBaseParaExConfiguracao.getFuncaoConfianca());
		exConfiguracao.setHisAtivo(configuracaoBaseParaExConfiguracao.getHisAtivo());
		exConfiguracao.setHisDtFim(configuracaoBaseParaExConfiguracao.getHisDtFim());
		exConfiguracao.setHisDtIni(configuracaoBaseParaExConfiguracao.getHisDtIni());
		exConfiguracao.setHisIdcFim(configuracaoBaseParaExConfiguracao.getHisIdcFim());
		exConfiguracao.setHisIdcIni(configuracaoBaseParaExConfiguracao.getHisIdcIni());
		exConfiguracao.setHisIdIni(configuracaoBaseParaExConfiguracao.getHisIdIni());
		exConfiguracao.setId(configuracaoBaseParaExConfiguracao.getId());
		exConfiguracao.setIdConfiguracao(configuracaoBaseParaExConfiguracao.getIdConfiguracao());
		exConfiguracao.setLotacao(configuracaoBaseParaExConfiguracao.getLotacao());
		exConfiguracao.setNmEmail(configuracaoBaseParaExConfiguracao.getNmEmail());
		exConfiguracao.setOrgaoObjeto(configuracaoBaseParaExConfiguracao.getOrgaoObjeto());
		exConfiguracao.setOrgaoUsuario(configuracaoBaseParaExConfiguracao.getOrgaoUsuario());
		return exConfiguracao;
		
		
	}
	
	public String getDescrDocConfidencial(ExDocumento doc) {
		return Ex.getInstance().getBL().descricaoSePuderAcessar(doc, getTitular(), getLotaTitular());
	}

	public List<ExTipoDocumento> getTiposDocumento() throws AplicacaoException {
		return dao().listarExTiposDocumento();
	}

	public ExDao dao() {
		return ExDao.getInstance();
	}

	public ExDocumento daoDoc(long id) {
		return dao().consultar(id, ExDocumento.class, false);
	}

	public ExMovimentacao daoMov(long id) {
		return dao().consultar(id, ExMovimentacao.class, false);
	}

	public ExMobil daoMob(long id) {
		return dao().consultar(id, ExMobil.class, false);
	}

	public List<ExEstadoDoc> getEstados() throws AplicacaoException {
		return ExDao.getInstance().listarExEstadosDoc();
	}

	public Map<Integer, String> getListaTipoResp() {
		final Map<Integer, String> map = new TreeMap<Integer, String>();
		map.put(1, "Matr�cula");
		map.put(2, "�rg�o Integrado");
		return map;
	}

	public List<String> getListaAnos() {
		final ArrayList<String> lst = new ArrayList<String>();
		// map.add("", "[Vazio]");
		final Calendar cal = Calendar.getInstance();
		for (Integer ano = cal.get(Calendar.YEAR); ano >= 1990; ano--)
			lst.add(ano.toString());
		return lst;
	}

	public void assertAcesso(String pathServico) throws AplicacaoException,
			Exception {
		super.assertAcesso("DOC:M�dulo de Documentos;" + pathServico);
	}
	
	
	public HttpServletResponse getResponse() {
		return response;
	}
	
	
	public ServletContext getContext() {
		return context;
	}
	


}
