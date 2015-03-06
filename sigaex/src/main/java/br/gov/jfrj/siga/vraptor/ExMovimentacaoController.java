package br.gov.jfrj.siga.vraptor;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.persistence.EntityManager;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jboss.logging.Logger;

import br.com.caelum.vraptor.Get;
import br.com.caelum.vraptor.Post;
import br.com.caelum.vraptor.Resource;
import br.com.caelum.vraptor.Result;
import br.com.caelum.vraptor.interceptor.multipart.UploadedFile;
import br.gov.jfrj.siga.base.AplicacaoException;
import br.gov.jfrj.siga.ex.ExClassificacao;
import br.gov.jfrj.siga.ex.ExDocumento;
import br.gov.jfrj.siga.ex.ExFormaDocumento;
import br.gov.jfrj.siga.ex.ExMobil;
import br.gov.jfrj.siga.ex.ExModelo;
import br.gov.jfrj.siga.ex.ExMovimentacao;
import br.gov.jfrj.siga.ex.ExNivelAcesso;
import br.gov.jfrj.siga.ex.ExPapel;
import br.gov.jfrj.siga.ex.ExTipoDespacho;
import br.gov.jfrj.siga.ex.ExTipoDocumento;
import br.gov.jfrj.siga.ex.ExTipoMovimentacao;
import br.gov.jfrj.siga.ex.SigaExProperties;
import br.gov.jfrj.siga.ex.bl.Ex;
import br.gov.jfrj.siga.ex.vo.ExMobilVO;
import br.gov.jfrj.siga.hibernate.ExDao;
import br.gov.jfrj.siga.libs.webwork.CpOrgaoSelecao;
import br.gov.jfrj.siga.libs.webwork.DpLotacaoSelecao;
import br.gov.jfrj.siga.libs.webwork.DpPessoaSelecao;
import br.gov.jfrj.siga.model.dao.HibernateUtil;
import br.gov.jfrj.siga.vraptor.builder.BuscaDocumentoBuilder;
import br.gov.jfrj.siga.vraptor.builder.ExMovimentacaoBuilder;

@Resource
public class ExMovimentacaoController extends ExController {

	private static final Logger LOGGER = Logger.getLogger(ExMovimentacaoController.class);

	public ExMovimentacaoController(HttpServletRequest request, HttpServletResponse response, ServletContext context, Result result, SigaObjects so,
			EntityManager em) {
		super(request, response, context, result, ExDao.getInstance(), so, em);

		result.on(AplicacaoException.class).forwardTo(this).appexception();
		result.on(Exception.class).forwardTo(this).exception();
	}

	private ExDocumento buscarDocumento(final BuscaDocumentoBuilder builder) {
		return buscarDocumento(builder, true);
	}

	private ExDocumento buscarDocumento(final BuscaDocumentoBuilder builder, final boolean verificarAcesso) {
		ExDocumento doc = builder.buscarDocumento(dao());

		if (verificarAcesso && builder.getMob() != null)
			verificaNivelAcesso(builder.getMob());

		return doc;
	}

	@Get("app/expediente/mov/anexar")
	public void anexa(final String sigla, final Long idMovPendencia) {
		final BuscaDocumentoBuilder documentoBuilder = BuscaDocumentoBuilder.novaInstancia().setSigla(sigla);

		buscarDocumento(documentoBuilder);
		final ExMobil mob = documentoBuilder.getMob();

		final ExMovimentacaoBuilder movimentacaoBuilder = ExMovimentacaoBuilder.novaInstancia().setMob(mob);

		if (!(mob.isGeral() && mob.doc().isFinalizado())) {
			if (!Ex.getInstance().getComp().podeAnexarArquivo(getTitular(), getLotaTitular(), mob)) {
				throw new AplicacaoException("Arquivo n�o pode ser anexado");
			}
		}

		final ExMobilVO mobilVO = new ExMobilVO(mob, getTitular(), getLotaTitular(), true, ExTipoMovimentacao.TIPO_MOVIMENTACAO_ANEXACAO, false);
		final ExMobilVO mobilCompletoVO = new ExMobilVO(mob, getTitular(), getLotaTitular(), true, null, false);
		
		if(idMovPendencia != null) {
			final ExMovimentacao movPendencia = dao.consultar(idMovPendencia, ExMovimentacao.class, false);
			
			if(movPendencia != null)
				result.include("descrMov", movPendencia.getDescrMov());
		}

		result.include("mobilCompletoVO", mobilCompletoVO);
		result.include("mobilVO", mobilVO);
		result.include("sigla", sigla);
		result.include("idMovPendencia", idMovPendencia);
		result.include("mob", mob);
		result.include("subscritorSel", movimentacaoBuilder.getSubscritorSel());
		result.include("titularSel", movimentacaoBuilder.getTitularSel());
		result.include("request", getRequest());
	}

	@Post("app/expediente/mov/anexar_gravar")
	public void anexarGravar(final String sigla, final DpPessoaSelecao subscritorSel, final DpPessoaSelecao titularSel, final boolean substituicao,
			final UploadedFile arquivo, final String dtMovString, final String descrMov) {

		final BuscaDocumentoBuilder documentoBuilder = BuscaDocumentoBuilder.novaInstancia().setSigla(sigla);

		buscarDocumento(documentoBuilder);
		final ExMobil mob = documentoBuilder.getMob();

		final ExMovimentacaoBuilder movimentacaoBuilder = ExMovimentacaoBuilder.novaInstancia().setMob(documentoBuilder.getMob()).setSubstituicao(substituicao)
				.setSubscritorSel(subscritorSel).setTitularSel(titularSel).setDtMovString(dtMovString).setDescrMov(descrMov)
				.setContentType(arquivo.getContentType()).setFileName(arquivo.getFileName());

		final ExMovimentacao mov = movimentacaoBuilder.construir(dao());
		mov.setSubscritor(subscritorSel.getObjeto());
		mov.setTitular(titularSel.getObjeto());

		if (arquivo.getFile() == null) {
			throw new AplicacaoException("O arquivo a ser anexado n�o foi selecionado!");
		}

		try {
			final byte[] baArquivo = toByteArray(arquivo);
			if (baArquivo == null) {
				throw new AplicacaoException("Arquivo vazio n�o pode ser anexado.");
			}
			if (baArquivo.length > 10 * 1024 * 1024) {
				throw new AplicacaoException("N�o � permitida a anexa��o de arquivos com mais de 10MB.");
			}
			mov.setConteudoBlobMov2(baArquivo);
		} catch (IOException ex) {
			throw new AplicacaoException("Falha ao manipular aquivo", 1, ex);
		}

		if (mov.getContarNumeroDePaginas() == null || mov.getArquivoComStamp() == null) {
			throw new AplicacaoException(MessageFormat.format("O arquivo {0} est� corrompido. Favor ger�-lo novamente antes de anexar.", arquivo.getFileName()));
		}
		if (mob.isVolumeEncerrado()) {
			throw new AplicacaoException("N�o � poss�vel anexar arquivo em volume encerrado.");
		}

		if (!Ex.getInstance().getComp().podeAnexarArquivo(getTitular(), getLotaTitular(), mob)) {
			throw new AplicacaoException("Arquivo n�o pode ser anexado");
		}
		if (!arquivo.getContentType().equals("application/pdf")) {
			throw new AplicacaoException("Somente � permitido anexar arquivo PDF.");
		}

		// Obtem as pendencias que ser�o resolvidas
		final String aidMov[] = getRequest().getParameterValues("pendencia_de_anexacao");
		Set<ExMovimentacao> pendencias = null;
		if (aidMov != null) {
			pendencias = new TreeSet<ExMovimentacao>();
			for (String s : aidMov) {
				pendencias.add(dao().consultar(Long.parseLong(s), ExMovimentacao.class, false));
			}
		}

		// Nato: Precisei usar o c�digo abaixo para adaptar o charset do
		// nome do arquivo
		try {
			final byte[] ab = mov.getNmArqMov().getBytes();
			for (int i = 0; i < ab.length; i++) {
				if (ab[i] == -29) {
					ab[i] = -61;
				}
			}
			final String sNmArqMov = new String(ab, "utf-8");

			Ex.getInstance()
					.getBL()
					.anexarArquivo(getCadastrante(), getLotaTitular(), mob, mov.getDtMov(), mov.getSubscritor(), sNmArqMov, mov.getTitular(),
							mov.getLotaTitular(), mov.getConteudoBlobMov2(), mov.getConteudoTpMov(), movimentacaoBuilder.getDescrMov(), pendencias);
		} catch (UnsupportedEncodingException ex) {
			LOGGER.error(ex.getMessage(), ex);
		}

		result.redirectTo(MessageFormat.format("anexar?sigla={0}", sigla));
	}

	@Get("app/expediente/mov/mostrar_anexos_assinados")
	public void mostrarAnexosAssinados(final String sigla) {
		final BuscaDocumentoBuilder builder = BuscaDocumentoBuilder.novaInstancia().setSigla(sigla);

		buscarDocumento(builder);

		final ExMobilVO mobilVO = new ExMobilVO(builder.getMob(), getTitular(), getLotaTitular(), true, ExTipoMovimentacao.TIPO_MOVIMENTACAO_ANEXACAO, true);

		result.include("mobilVO", mobilVO);
	}

	@Get("app/expediente/mov/desobrestar_gravar")
	public void aDesobrestarGravar(final String sigla) {
		final BuscaDocumentoBuilder builder = BuscaDocumentoBuilder.novaInstancia().setSigla(sigla);

		buscarDocumento(builder);
		final ExMobil mob = builder.getMob();

		final ExMovimentacaoBuilder movimentacaoBuilder = ExMovimentacaoBuilder.novaInstancia().setMob(mob);

		final ExMovimentacao movimentacao = movimentacaoBuilder.construir(dao());

		if (!Ex.getInstance().getComp().podeDesobrestar(getTitular(), getLotaTitular(), mob)) {
			throw new AplicacaoException("Via n�o pode ser desobrestada");
		}

		Ex.getInstance().getBL().desobrestar(getCadastrante(), getLotaTitular(), mob, movimentacao.getDtMov(), movimentacao.getSubscritor());
		ExDocumentoController.redirecionarParaExibir(result, sigla);
	}

	@Get("app/expediente/mov/sobrestar_gravar")
	public void sobrestarGravar(final String sigla) {
		final BuscaDocumentoBuilder builder = BuscaDocumentoBuilder.novaInstancia().setSigla(sigla);

		buscarDocumento(builder);

		final ExMovimentacao mov = ExMovimentacaoBuilder.novaInstancia().construir(dao());

		if (!Ex.getInstance().getComp().podeAcessarDocumento(getTitular(), getLotaTitular(), builder.getMob())) {
			throw new AplicacaoException("Acesso permitido a usu�rios autorizados.");
		}

		if (!Ex.getInstance().getComp().podeSobrestar(getTitular(), getLotaTitular(), builder.getMob())) {
			throw new AplicacaoException("Via n�o pode ser sobrestada");
		}

		Ex.getInstance().getBL().sobrestar(getCadastrante(), getLotaTitular(), builder.getMob(), mov.getDtMov(), null, mov.getSubscritor());
		ExDocumentoController.redirecionarParaExibir(result, sigla);
	}

	@Get("app/expediente/mov/assinar")
	public void aAssinar(String sigla) throws Exception {
		BuscaDocumentoBuilder builder = BuscaDocumentoBuilder.novaInstancia().setSigla(sigla);

		ExDocumento doc = buscarDocumento(builder);
		boolean previamenteAssinado = doc.isAssinado();

		if (devePreAssinar(doc, previamenteAssinado)) {
			Ex.getInstance().getBL().processarComandosEmTag(doc, "pre_assinatura");
		}
		result.include("sigla", sigla);
		result.include("doc", doc);
		result.include("titular", this.getTitular());
		result.include("lotaTitular", this.getLotaTitular());
	}

	private boolean devePreAssinar(ExDocumento doc, boolean fPreviamenteAssinado) {
		return !fPreviamenteAssinado && (doc.getExModelo() != null && ("template/freemarker".equals(doc.getExModelo().getConteudoTpBlob())));
	}

	@Get("app/expediente/mov/redefinir_nivel_acesso")
	public void redefinirNivelAcesso(final String sigla) {
		final BuscaDocumentoBuilder builder = BuscaDocumentoBuilder.novaInstancia().setSigla(sigla);

		final ExDocumento doc = buscarDocumento(builder);

		result.include("sigla", sigla);
		result.include("mob", builder.getMob());
		result.include("listaNivelAcesso", getListaNivelAcesso(doc));
		result.include("nivelAcesso", doc.getExNivelAcesso().getIdNivelAcesso());
	}

	@Post("app/expediente/mov/redefinir_nivel_acesso_gravar")
	public void redefinirNivelAcessoGravar(final String sigla, final DpPessoaSelecao subscritorSel, final DpPessoaSelecao titularSel, final String dtMovString,
			final boolean substituicao, final Long nivelAcesso) {
		final BuscaDocumentoBuilder builder = BuscaDocumentoBuilder.novaInstancia().setSigla(sigla);

		final ExDocumento doc = buscarDocumento(builder);

		final ExMovimentacaoBuilder movimentacaoBuilder = ExMovimentacaoBuilder.novaInstancia();
		movimentacaoBuilder.setSubscritorSel(subscritorSel).setTitularSel(titularSel).setDtMovString(dtMovString).setSubstituicao(substituicao);

		final ExMovimentacao mov = movimentacaoBuilder.construir(dao());

		ExNivelAcesso exTipoSig = null;

		if (nivelAcesso != null) {
			exTipoSig = dao().consultar(nivelAcesso, ExNivelAcesso.class, false);
		}

		if (!Ex.getInstance().getComp().podeRedefinirNivelAcesso(getTitular(), getLotaTitular(), builder.getMob())) {
			throw new AplicacaoException("N�o � poss�vel redefinir o n�vel de acesso");
		}

		Ex.getInstance()
				.getBL()
				.redefinirNivelAcesso(getCadastrante(), getLotaTitular(), doc, mov.getDtMov(), mov.getLotaResp(), mov.getResp(), mov.getSubscritor(),
						mov.getTitular(), mov.getNmFuncaoSubscritor(), exTipoSig);
		ExDocumentoController.redirecionarParaExibir(result, sigla);
	}

	@Get("/app/expediente/mov/cancelarMovimentacao")
	public void aCancelarUltimaMovimentacao(final String sigla) {
		final BuscaDocumentoBuilder documentoBuilder = BuscaDocumentoBuilder.novaInstancia().setSigla(sigla);

		buscarDocumento(documentoBuilder);
		final ExMobil mob = documentoBuilder.getMob();

		final ExMovimentacao exUltMovNaoCanc = mob.getUltimaMovimentacaoNaoCancelada();
		final ExMovimentacao exUltMov = mob.getUltimaMovimentacao();

		if (exUltMovNaoCanc.getExTipoMovimentacao().getIdTpMov() == ExTipoMovimentacao.TIPO_MOVIMENTACAO_CRIACAO
				&& exUltMovNaoCanc.getIdMov() == exUltMov.getIdMov()) {
			if (!Ex.getInstance().getComp().podeCancelarVia(getTitular(), getLotaTitular(), mob)) {
				throw new AplicacaoException("N�o � poss�vel cancelar via");
			}
		} else {
			if (!Ex.getInstance().getComp().podeCancelarMovimentacao(getTitular(), getLotaTitular(), mob)) {
				throw new AplicacaoException("N�o � poss�vel cancelar movimenta��o");
			}
		}

		Ex.getInstance().getBL().cancelarMovimentacao(getCadastrante(), getLotaTitular(), mob);
		ExDocumentoController.redirecionarParaExibir(result, sigla);
	}

	@Get("app/expediente/mov/excluir")
	public void excluir(final Long id) {
		final BuscaDocumentoBuilder builder = BuscaDocumentoBuilder.novaInstancia().setId(id);

		final ExDocumento doc = buscarDocumento(builder);

		Ex.getInstance().getBL().excluirMovimentacao(getCadastrante(), getLotaTitular(), builder.getMob(), id);
		result.redirectTo(MessageFormat.format("anexar?sigla={0}", doc.getSigla()));
	}

	@Get("app/expediente/mov/exibir")
	public void aExibir(final boolean popup, final Long id) {
		final BuscaDocumentoBuilder builder = BuscaDocumentoBuilder.novaInstancia().setId(id);

		final ExDocumento doc = buscarDocumento(builder);

		if (id == null) {
			throw new AplicacaoException("id n�o foi informada.");
		}

		final ExMovimentacao mov = dao().consultar(id, ExMovimentacao.class, false);

		result.include("id", id);
		result.include("doc", doc);
		result.include("mov", mov);
		result.include("enderecoAutenticacao", SigaExProperties.getEnderecoAutenticidadeDocs());
	}

	@Post("app/expediente/mov/protocolo_transf")
	public void aGerarProtocolo(final String movId, final Long pessoa, final String dt, final List<String> itens, final String campoData,
			final String campoPara, final String campoDe) {
		final ArrayList<Object> al = criarListaDocumentos(itens);

		final ExMovimentacao mov = criarMov(movId);
		final Object[] arr = al.toArray();

		Arrays.sort(arr, new Comparator<Object>() {
			public int compare(Object obj1, Object obj2) {
				final ExDocumento doc1 = (ExDocumento) ((Object[]) obj1)[0];
				final ExMovimentacao mov1 = (ExMovimentacao) ((Object[]) obj1)[1];
				final ExDocumento doc2 = (ExDocumento) ((Object[]) obj2)[0];
				final ExMovimentacao mov2 = (ExMovimentacao) ((Object[]) obj2)[1];

				if (doc1.getAnoEmissao() > doc2.getAnoEmissao()) {
					return 1;
				} else if (doc1.getAnoEmissao() < doc2.getAnoEmissao()) {
					return -1;
				} else if (doc1.getExFormaDocumento().getIdFormaDoc() > doc2.getExFormaDocumento().getIdFormaDoc()) {
					return 1;
				} else if (doc1.getExFormaDocumento().getIdFormaDoc() < doc2.getExFormaDocumento().getIdFormaDoc()) {
					return -1;
				} else if (doc1.getNumExpediente() > doc2.getNumExpediente()) {
					return 1;
				} else if (doc1.getNumExpediente() < doc2.getNumExpediente()) {
					return -1;
				} else if (mov1.getExMobil().getExTipoMobil().getIdTipoMobil() > mov2.getExMobil().getExTipoMobil().getIdTipoMobil()) {
					return 1;
				} else if (mov1.getExMobil().getExTipoMobil().getIdTipoMobil() < mov2.getExMobil().getExTipoMobil().getIdTipoMobil()) {
					return -1;
				} else if (mov1.getExMobil().getNumSequencia() > mov2.getExMobil().getNumSequencia()) {
					return 1;
				} else if (mov1.getExMobil().getNumSequencia() < mov2.getExMobil().getNumSequencia()) {
					return -1;
				} else if (doc1.getIdDoc() > doc2.getIdDoc()) {
					return 1;
				} else if (doc1.getIdDoc() < doc2.getIdDoc()) {
					return -1;
				} else {
					return 0;
				}
			}
		});

		al.clear();
		for (int k = 0; k < arr.length; k++) {
			al.add(arr[k]);
		}

		result.include("campoDe", campoDe);
		result.include("campoPara", campoPara);
		result.include("campoData", campoData);

		result.include("itens", al);
		result.include("cadastrante", this.getCadastrante());
		result.include("mov", mov);
		result.include("lotaTitular", this.getLotaTitular());
	}

	private ArrayList<Object> criarListaDocumentos(List<String> itens) {
		final ArrayList<Object> listarDocumentos = new ArrayList<Object>();

		for (String idMubString : itens) {
			try {
				final Long idMob = Long.parseLong(idMubString);
				final ExMobil mob = dao().consultar(idMob, ExMobil.class, false);
				final Object[] ao = { mob.doc(), mob.getUltimaMovimentacaoNaoCancelada() };
				listarDocumentos.add(ao);
			} catch (NumberFormatException nfe) {
				System.out.println(MessageFormat.format("{0} nao pode ser convertido para Long", idMubString));
			}
		}
		return listarDocumentos;
	}

	private ExMovimentacao criarMov(String movIdString) {
		try {
			Long idMov = Long.parseLong(movIdString);
			final ExMovimentacao mov = dao.consultar(idMov, ExMovimentacao.class, false);
			return mov;
		} catch (NumberFormatException nfe) {
			System.out.println(MessageFormat.format("{0} nao pode ser convertido para Long", movIdString));
		}
		return null;
	}

	@Get("app/expediente/mov/protocolo_unitario")
	public void protocolo(final String sigla, final Long id) {
		final BuscaDocumentoBuilder builder = BuscaDocumentoBuilder.novaInstancia().setSigla(sigla);

		final ExDocumento doc = buscarDocumento(builder);

		ExMovimentacao mov = dao().consultar(id, ExMovimentacao.class, false);

		ArrayList<Object> lista = new ArrayList<Object>();
		final Object[] ao = { doc, mov };
		lista.add(ao);
		result.include("cadastrante", getCadastrante());
		result.include("mov", mov);
		result.include("itens", lista);
		result.include("lotaTitular", getLotaTitular());
	}

	@Get("app/expediente/mov/juntar")
	public void juntar(final String sigla) {
		final BuscaDocumentoBuilder builder = BuscaDocumentoBuilder.novaInstancia().setSigla(sigla);

		final ExDocumento doc = buscarDocumento(builder);

		if (!Ex.getInstance().getComp().podeJuntar(getTitular(), getLotaTitular(), builder.getMob())) {
			throw new AplicacaoException("N�o � poss�vel fazer juntada");
		}

		result.include("sigla", sigla);
		result.include("mob", builder.getMob());
		result.include("doc", doc);
	}

	@Post("app/expediente/mov/juntar_gravar")
	public void aJuntarGravar(final Integer postback, final String sigla, final String dtMovString, final boolean substituicao,
			final String idDocumentoPaiExterno, final DpPessoaSelecao subscritorSel, final DpPessoaSelecao titularSel, final ExMobilSelecao documentoRefSel,
			final String idDocumentoEscolha) {
		this.setPostback(postback);

		final BuscaDocumentoBuilder builder = BuscaDocumentoBuilder.novaInstancia().setSigla(sigla);

		buscarDocumento(builder);

		final ExMovimentacaoBuilder movimentacaoBuilder = ExMovimentacaoBuilder.novaInstancia().setDtMovString(dtMovString).setSubstituicao(substituicao)
				.setSubscritorSel(subscritorSel).setTitularSel(titularSel).setDocumentoRefSel(documentoRefSel);

		if (movimentacaoBuilder.getDocumentoRefSel() == null) {
			movimentacaoBuilder.setDocumentoRefSel(new ExMobilSelecao());
		}

		if (movimentacaoBuilder.getSubscritorSel() == null) {
			movimentacaoBuilder.setSubscritorSel(new DpPessoaSelecao());
		}

		final ExMovimentacao mov = movimentacaoBuilder.construir(dao());

		if (!Ex.getInstance().getComp().podeJuntar(getTitular(), getLotaTitular(), movimentacaoBuilder.getMob())) {
			throw new AplicacaoException("N�o � poss�vel fazer juntada");
		}

		// Nato: precisamos rever o codigo abaixo, pois a movimentacao nao pode
		// ser gravada sem hora, minuto e segundo.
		if (mov.getExDocumento().isEletronico()) {
			SimpleDateFormat sdf = new SimpleDateFormat();
			sdf.applyPattern("dd/MM/yyyy");
			mov.setSubscritor(getTitular());
		}

		// Quando o documento e eletronico, o responsavel pela juntada fica
		// sendo o proprio cadastrante e a data fica sendo a data atual
		if (mov.getExDocumento().isEletronico()) {
			mov.setDtMov(new Date());
			mov.setSubscritor(getCadastrante());
			mov.setTitular(getTitular());
		}

		Ex.getInstance()
				.getBL()
				.juntarDocumento(getCadastrante(), getTitular(), getLotaTitular(), idDocumentoPaiExterno, movimentacaoBuilder.getMob(), mov.getExMobilRef(),
						mov.getDtMov(), mov.getSubscritor(), mov.getTitular(), idDocumentoEscolha);
		ExDocumentoController.redirecionarParaExibir(result, sigla);
	}

	@Get("app/expediente/mov/apensar")
	public void apensar(final String sigla) {
		final BuscaDocumentoBuilder builder = BuscaDocumentoBuilder.novaInstancia().setSigla(sigla);
		final ExDocumento doc = buscarDocumento(builder);

		if (!Ex.getInstance().getComp().podeApensar(getTitular(), getLotaTitular(), builder.getMob())) {
			throw new AplicacaoException("N�o � poss�vel apensar");
		}

		result.include("mob", builder.getMob());
		result.include("doc", doc);
		result.include("sigla", sigla);
	}

	@Post("app/expediente/mov/apensar_gravar")
	public void apensarGravar(final ExMobilSelecao documentoRefSel, final DpPessoaSelecao subscritorSel, final DpPessoaSelecao titularSel, final String sigla,
			final String dtMovString, final boolean substituicao) {
		final BuscaDocumentoBuilder builder = BuscaDocumentoBuilder.novaInstancia().setSigla(sigla);
		buscarDocumento(builder);

		final ExMovimentacaoBuilder movimentacaoBuilder = ExMovimentacaoBuilder.novaInstancia();
		movimentacaoBuilder.setDocumentoRefSel(documentoRefSel).setSubscritorSel(subscritorSel).setTitularSel(titularSel).setDtMovString(dtMovString)
				.setSubstituicao(substituicao);

		final ExMovimentacao mov = movimentacaoBuilder.construir(dao());

		if (!Ex.getInstance().getComp().podeApensar(getTitular(), getLotaTitular(), builder.getMob())) {
			throw new AplicacaoException("N�o � poss�vel fazer apensar");
		}

		// Quando o documento e eletronico, o responsavel pela juntada fica
		// sendo o proprio cadastrante e a data fica sendo a data atual
		if (mov.getExDocumento().isEletronico()) {
			mov.setDtMov(new Date());
			mov.setSubscritor(getCadastrante());
			mov.setTitular(getTitular());
		}

		Ex.getInstance()
				.getBL()
				.apensarDocumento(getCadastrante(), getTitular(), getLotaTitular(), builder.getMob(), mov.getExMobilRef(), mov.getDtMov(), mov.getSubscritor(),
						mov.getTitular());
		ExDocumentoController.redirecionarParaExibir(result, mov.getExDocumento().getSigla());
	}

	@Get("/app/expediente/mov/registrar_assinatura")
	public void aRegistrarAssinatura(final String sigla) {
		final BuscaDocumentoBuilder builder = BuscaDocumentoBuilder.novaInstancia().setSigla(sigla);
		final ExDocumento doc = buscarDocumento(builder);

		DpPessoaSelecao sub = null;

		if (doc.getSubscritor() != null) {
			sub = new DpPessoaSelecao();
			sub.setId(doc.getSubscritor().getId());
			sub.buscar();
		}

		if (!Ex.getInstance().getComp().podeRegistrarAssinatura(getTitular(), getLotaTitular(), builder.getMob())) {
			throw new AplicacaoException("N�o � poss�vel registrar a assinatura");
		}

		result.include("mob", builder.getMob());
		result.include("sigla", sigla);
		result.include("subscritorSel", sub);
		result.include("substituicao", false);
	}

	@Post("/app/expediente/mov/registrar_assinatura_gravar")
	public void registrar_assinatura_gravar(final int postback, final String sigla, final String dtMovString, final DpPessoaSelecao subscritorSel,
			final boolean substituicao, final DpPessoaSelecao tilularSel) {
		this.setPostback(postback);

		final BuscaDocumentoBuilder builder = BuscaDocumentoBuilder.novaInstancia().setSigla(sigla);

		final ExDocumento doc = buscarDocumento(builder);

		final ExMovimentacaoBuilder movimentacaoBuilder = ExMovimentacaoBuilder.novaInstancia();
		movimentacaoBuilder.setDtMovString(dtMovString).setSubscritorSel(subscritorSel).setSubstituicao(substituicao).setTitularSel(tilularSel);

		final ExMovimentacao mov = movimentacaoBuilder.construir(dao());

		if (mov.getSubscritor() == null) {
			throw new AplicacaoException("Respons�vel n�o informado");
		}

		if (!Ex.getInstance().getComp().podeRegistrarAssinatura(getTitular(), getLotaTitular(), builder.getMob())) {
			throw new AplicacaoException("N�o � poss�vel registrar a assinatura");
		}

		Ex.getInstance().getBL().RegistrarAssinatura(getCadastrante(), getLotaTitular(), doc, mov.getDtMov(), mov.getSubscritor(), mov.getTitular());

		result.redirectTo("/app/expediente/doc/exibir?sigla=" + sigla);
	}

	@Get("/app/expediente/mov/incluir_cosignatario")
	public void incluirCosignatario(final String sigla) {
		final BuscaDocumentoBuilder builder = BuscaDocumentoBuilder.novaInstancia().setSigla(sigla);

		final ExDocumento doc = buscarDocumento(builder);

		if (!Ex.getInstance().getComp().podeIncluirCosignatario(getTitular(), getLotaTitular(), builder.getMob())) {
			throw new AplicacaoException("N�o � poss�vel incluir cossignat�rio");
		}

		result.include("sigla", sigla);
		result.include("documento", doc);
		result.include("mob", builder.getMob());
	}

	@Post("/app/expediente/mov/incluir_cosignatario_gravar")
	public void aIncluirCosignatarioGravar(final String sigla, final DpPessoaSelecao cosignatarioSel, final String funcaoCosignatario, final Integer postback) {
		this.setPostback(postback);

		final BuscaDocumentoBuilder documentoBuilder = BuscaDocumentoBuilder.novaInstancia().setSigla(sigla);

		final ExDocumento doc = buscarDocumento(documentoBuilder);

		final ExMovimentacaoBuilder movimentacaoBuilder = ExMovimentacaoBuilder.novaInstancia().setMob(documentoBuilder.getMob())
				.setDescrMov(funcaoCosignatario).setSubscritorSel(cosignatarioSel);

		final ExMovimentacao mov = movimentacaoBuilder.construir(dao());

		if (!Ex.getInstance().getComp().podeIncluirCosignatario(getTitular(), getLotaTitular(), documentoBuilder.getMob())) {
			throw new AplicacaoException("N�o � poss�vel incluir cossignat�rio");
		}

		Ex.getInstance().getBL().incluirCosignatario(getCadastrante(), getLotaTitular(), doc, mov.getDtMov(), mov.getSubscritor(), mov.getDescrMov());

		ExDocumentoController.redirecionarParaExibir(result, mov.getExDocumento().getSigla());
	}

	// Nato: Temos que substituir por uma tela que mostre os itens marcados como
	// "em transito"
	@Get("/app/expediente/mov/receber_lote")
	public void aReceberLote() {
		final List<ExMobil> provItens = dao().consultarParaReceberEmLote(getLotaTitular());

		final List<ExMobil> itens = new ArrayList<ExMobil>();

		for (ExMobil m : provItens) {
			if (!m.isApensado() && Ex.getInstance().getComp().podeAcessarDocumento(getTitular(), getLotaTitular(), m)) {
				itens.add(m);
			}
		}

		result.include("itens", itens);
	}

	@Post("/app/expediente/mov/receber_lote_gravar")
	public void aReceberLoteGravar(final Integer postback) {
		this.setPostback(postback);

		final ExMovimentacaoBuilder builder = ExMovimentacaoBuilder.novaInstancia();
		final ExMovimentacao mov = builder.construir(dao());

		final Pattern p = Pattern.compile("chk_([0-9]+)");

		for (final String s : getPar().keySet()) {
			if (s.startsWith("chk_") && param(s).equals("true")) {
				final Matcher m = p.matcher(s);
				if (!m.find()) {
					throw new AplicacaoException("N�o foi poss�vel ler a Id do documento e o n�mero da via.");
				}
				final ExMobil mob = dao().consultar(Long.valueOf(m.group(1)), ExMobil.class, false);

				if (Ex.getInstance().getComp().podeReceber(getTitular(), getLotaTitular(), mob)) {
					Ex.getInstance().getBL().receber(getCadastrante(), getLotaTitular(), mob, mov.getDtMov());
				}
			}
		}

		result.redirectTo("/app/expediente/mov/receber_lote");
	}

	@Get("/app/expediente/mov/arquivar_corrente_lote")
	public void aArquivarCorrenteLote() {
		final List<ExMobil> provItens = dao().consultarParaArquivarCorrenteEmLote(getLotaTitular());

		List<ExMobil> itens = new ArrayList<ExMobil>();

		for (ExMobil m : provItens) {
			if (!m.isApensado() && Ex.getInstance().getComp().podeAcessarDocumento(getTitular(), getLotaTitular(), m)) {
				itens.add(m.isVolume() ? m.doc().getMobilGeral() : m);
			}
		}

		result.include("itens", itens);
	}

	@Post("/app/expediente/mov/arquivar_corrente_lote_gravar")
	public void aArquivarCorrenteLoteGravar(final Integer postback) {
		this.setPostback(postback);

		final ExMovimentacaoBuilder builder = ExMovimentacaoBuilder.novaInstancia();
		final ExMovimentacao mov = builder.construir(dao());

		final Pattern p = Pattern.compile("chk_([0-9]+)");
		final Date dt = dao().dt();

		for (final String s : getPar().keySet()) {
			if (s.startsWith("chk_") && param(s).equals("true")) {
				final Matcher m = p.matcher(s);
				if (!m.find()) {
					throw new AplicacaoException("N�o foi poss�vel ler a Id do documento e o n�mero da via.");
				}
				final ExMobil mob = dao().consultar(Long.valueOf(m.group(1)), ExMobil.class, false);

				Ex.getInstance().getBL().arquivarCorrente(getCadastrante(), getLotaTitular(), mob, mov.getDtMov(), dt, mov.getSubscritor(), false);
			}
		}

		result.redirectTo("/app/expediente/mov/arquivar_corrente_lote");
	}

	@Get("/app/expediente/mov/assinar_despacho_lote")
	public void aAssinarDespachoLote() {
		final List<ExMovimentacao> itensComoSubscritor = dao().listarDespachoPendenteAssinatura(getTitular());

		final List<ExMovimentacao> itens = new ArrayList<ExMovimentacao>();
		final List<ExMovimentacao> movimentacoesQuePodemSerAssinadasComSenha = new ArrayList<ExMovimentacao>();

		for (ExMovimentacao mov : itensComoSubscritor) {
			if (!mov.isAssinada() && !mov.isCancelada()) {
				itens.add(mov);

				if (Ex.getInstance().getComp().podeAssinarMovimentacaoComSenha(getTitular(), getLotaTitular(), mov)) {
					movimentacoesQuePodemSerAssinadasComSenha.add(mov);
				}
			}

		}

		result.include("itens", itens);
		result.include("movimentacoesQuePodemSerAssinadasComSenha", movimentacoesQuePodemSerAssinadasComSenha);
	}

	@Get("/app/expediente/mov/referenciar")
	public void aReferenciar(final String sigla) {
		final BuscaDocumentoBuilder builder = BuscaDocumentoBuilder.novaInstancia().setSigla(sigla);
		final ExDocumento doc = buscarDocumento(builder);

		if (!Ex.getInstance().getComp().podeReferenciar(getTitular(), getLotaTitular(), builder.getMob())) {
			throw new AplicacaoException("N�o � poss�vel fazer vincula��o");
		}

		result.include("sigla", sigla);
		result.include("doc", doc);
		result.include("mob", builder.getMob());
	}

	@Post("app/expediente/mov/prever")
	public void preve(final String sigla) {
		final BuscaDocumentoBuilder builder = BuscaDocumentoBuilder.novaInstancia().setSigla(sigla);

		buscarDocumento(builder);

		ExMovimentacao mov;

		if (builder.getId() != null) {
			mov = daoMov(builder.getId());
		} else {
			mov = ExMovimentacaoBuilder.novaInstancia().construir(dao());
		}

		if (param("processar_modelo") != null) {
			result.forwardTo(this).processa_modelo(mov);
		} else {
			result.include("par", getRequest().getParameterMap());
			result.include("modelo", getModelo());
			result.include("nmArqMod", getModelo().getNmArqMod());
			result.include("mov", mov);
		}
	}

	private void processa_modelo(final ExMovimentacao mov) {
		result.include("par", getRequest().getParameterMap());
		result.include("modelo", getModelo());
		result.include("nmArqMod", getModelo().getNmArqMod());
		result.include("mov", mov);
	}

	@Post("/app/expediente/mov/referenciar_gravar")
	public void aReferenciarGravar(final String sigla, final String dtMovString, final boolean substituicao, final DpPessoaSelecao titularSel,
			final DpPessoaSelecao subscritorSel, final ExMobilSelecao documentoRefSel) {
		final BuscaDocumentoBuilder builder = BuscaDocumentoBuilder.novaInstancia().setSigla(sigla);
		buscarDocumento(builder);

		final ExMovimentacaoBuilder movimentacaoBuilder = ExMovimentacaoBuilder.novaInstancia();
		movimentacaoBuilder.setDtMovString(dtMovString).setSubstituicao(substituicao).setTitularSel(titularSel).setSubscritorSel(subscritorSel)
				.setDocumentoRefSel(documentoRefSel);

		final ExMovimentacao mov = movimentacaoBuilder.construir(dao());

		if (!Ex.getInstance().getComp().podeReferenciar(getTitular(), getLotaTitular(), builder.getMob())) {
			throw new AplicacaoException("N�o � poss�vel fazer vincula��o");
		}
		if (mov.getExMobilRef() == null) {
			throw new AplicacaoException("N�o foi selecionado um documento para a vincula��o");
		}

		if (mov.getExDocumento().isEletronico()) {
			mov.setSubscritor(getTitular());
		}

		Ex.getInstance()
				.getBL()
				.referenciarDocumento(getCadastrante(), getLotaTitular(), builder.getMob(), mov.getExMobilRef(), mov.getDtMov(), mov.getSubscritor(),
						mov.getTitular());

		ExDocumentoController.redirecionarParaExibir(result, mov.getExDocumento().getSigla());
	}

	@Get("/app/expediente/mov/transferir")
	public void aTransferir(final String sigla) {
		final BuscaDocumentoBuilder builder = BuscaDocumentoBuilder.novaInstancia().setSigla(sigla);
		final ExDocumento doc = buscarDocumento(builder);

		final DpLotacaoSelecao lot = new DpLotacaoSelecao();
		final DpPessoaSelecao pes = new DpPessoaSelecao();
		int tipoResponsavel = -1;

		final ExMovimentacao ultMov = builder.getMob().getUltimaMovimentacao();
		if (getRequest().getAttribute("postback") == null) {
			if (ultMov.getLotaDestinoFinal() != null) {
				lot.buscarPorObjeto(ultMov.getLotaDestinoFinal());
				tipoResponsavel = 1;
			}
			if (ultMov.getDestinoFinal() != null) {
				pes.buscarPorObjeto(ultMov.getDestinoFinal());
				tipoResponsavel = 2;
			}
		}

		if (!(Ex.getInstance().getComp().podeTransferir(getTitular(), getLotaTitular(), builder.getMob()) || Ex.getInstance().getComp()
				.podeDespachar(getTitular(), getLotaTitular(), builder.getMob()))) {
			throw new AplicacaoException("N�o � poss�vel fazer despacho nem transfer�ncia");
		}

		result.include("doc", doc);
		result.include("mob", builder.getMob());
		result.include("postback", this.getPostback());
		result.include("sigla", sigla);
		result.include("tiposDespacho", this.getTiposDespacho(builder.getMob()));
		result.include("listaTipoResp", this.getListaTipoResp());
		result.include("tipoResponsavel", tipoResponsavel);

		result.include("lotaResponsavelSel", lot);
		result.include("responsavelSel", pes);
	}

	@Post("/app/expediente/mov/transferir_gravar")
	public void transferir_gravar(final int postback, final String sigla, final String dtMovString, final DpPessoaSelecao subscritorSel,
			final boolean substituicao, final DpPessoaSelecao titularSel, final String nmFuncaoSubscritor, final long idTpDespacho, final long idResp,
			final List<ExTipoDespacho> tiposDespacho, final String descrMov, final List<Map<Integer, String>> listaTipoResp, final int tipoResponsavel,
			final DpLotacaoSelecao lotaResponsavel, final DpPessoaSelecao responsavelSel, final CpOrgaoSelecao cpOrgacaoSel, final String obsOrgao,
			final String protocolo) {
		this.setPostback(postback);

		final BuscaDocumentoBuilder builder = BuscaDocumentoBuilder.novaInstancia().setSigla(sigla);
		buscarDocumento(builder);

		final ExMovimentacaoBuilder movimentacaoBuilder = ExMovimentacaoBuilder.novaInstancia();
		movimentacaoBuilder.setDtMovString(dtMovString).setSubscritorSel(subscritorSel).setSubstituicao(substituicao).setTitularSel(titularSel)
				.setNmFuncaoSubscritor(nmFuncaoSubscritor).setIdTpDespacho(idTpDespacho).setDescrMov(descrMov).setLotaResponsavelSel(lotaResponsavel)
				.setResponsavelSel(responsavelSel).setCpOrgaoSel(cpOrgacaoSel).setObsOrgao(obsOrgao);

		final ExMovimentacao mov = movimentacaoBuilder.construir(dao());

		final ExMovimentacao UltMov = builder.getMob().getUltimaMovimentacaoNaoCancelada();
		if ((mov.getLotaResp() != null && mov.getResp() == null && UltMov.getLotaResp() != null && UltMov.getResp() == null && UltMov.getLotaResp().equivale(
				mov.getLotaResp()))
				|| (mov.getResp() != null && UltMov.getResp() != null && UltMov.getResp().equivale(mov.getResp()))) {
			throw new AplicacaoException("Novo respons�vel n�o pode ser igual ao atual");
		}

		if (!Ex.getInstance().getComp().podeReceberPorConfiguracao(mov.getResp(), mov.getLotaResp())) {
			throw new AplicacaoException("Destinat�rio n�o pode receber documentos");
		}

		if (!(Ex.getInstance().getComp().podeTransferir(getTitular(), getLotaTitular(), builder.getMob()) || Ex.getInstance().getComp()
				.podeDespachar(getTitular(), getLotaTitular(), builder.getMob()))) {
			throw new AplicacaoException("N�o � poss�vel fazer despacho nem transfer�ncia");
		}

		Ex.getInstance()
				.getBL()
				.transferir(mov.getOrgaoExterno(), mov.getObsOrgao(), getCadastrante(), getLotaTitular(), builder.getMob(), mov.getDtMov(), mov.getDtIniMov(),
						mov.getDtFimMov(), mov.getLotaResp(), mov.getResp(), mov.getLotaDestinoFinal(), mov.getDestinoFinal(), mov.getSubscritor(),
						mov.getTitular(), mov.getExTipoDespacho(), false, mov.getDescrMov(), movimentacaoBuilder.getConteudo(), mov.getNmFuncaoSubscritor(),
						false, false);

		if (protocolo != null && protocolo.equals("mostrar")) {
			result.forwardTo(this).transferido();
		}

		result.redirectTo(this).fechar_popup();
	}

	@Get("/app/expediente/mov/fechar_popup")
	public void fechar_popup() {
		System.out.println("popup fechado.");
	}

	@Get("/app/expediente/mov/transferido")
	public void transferido() {
		final ExMovimentacao mov = ExMovimentacaoBuilder.novaInstancia().construir(dao());

		result.include("mov", mov);
	}

	@Get("app/expediente/mov/encerrar_volume")
	public void encerrarVolumeGravar(final String sigla) {
		final BuscaDocumentoBuilder builder = BuscaDocumentoBuilder.novaInstancia().setSigla(sigla);
		buscarDocumento(builder);

		final ExMovimentacao mov = ExMovimentacaoBuilder.novaInstancia().construir(dao());

		if (builder.getMob().isVolumeEncerrado()) {
			throw new AplicacaoException("N�o � permitido encerrar um volume j� encerrado.");
		}

		Ex.getInstance()
				.getBL()
				.encerrarVolume(getCadastrante(), getLotaTitular(), builder.getMob(), mov.getDtMov(), mov.getSubscritor(), mov.getTitular(),
						mov.getNmFuncaoSubscritor(), false);
		ExDocumentoController.redirecionarParaExibir(result, mov.getExDocumento().getSigla());
	}

	@Get("/app/expediente/mov/anotar")
	public void aAnotar(final String sigla) {
		final BuscaDocumentoBuilder documentoBuilder = BuscaDocumentoBuilder.novaInstancia().setSigla(sigla);

		final ExDocumento documento = buscarDocumento(documentoBuilder);

		final ExMovimentacaoBuilder movimentacaoBuilder = ExMovimentacaoBuilder.novaInstancia().setMob(documentoBuilder.getMob());

		final ExMovimentacao movimentacao = movimentacaoBuilder.construir(dao());

		if (!Ex.getInstance().getComp().podeFazerAnotacao(getTitular(), getLotaTitular(), documentoBuilder.getMob())) {
			throw new AplicacaoException("N�o � poss�vel fazer anota��o");
		}

		result.include("sigla", sigla);
		result.include("dtMovString", movimentacaoBuilder.getDtMovString());
		result.include("mob", documentoBuilder.getMob());
		result.include("mov", movimentacao);
		result.include("doc", documento);
		result.include("substituicao", movimentacaoBuilder.isSubstituicao());
		result.include("nmFuncaoSubscritor", movimentacaoBuilder.getNmFuncaoSubscritor());
		result.include("descrMov", movimentacaoBuilder.getDescrMov());
		result.include("tipoResponsavel", this.processarTipoResponsavel(documentoBuilder.getMob()));
		result.include("obsOrgao", movimentacaoBuilder.getObsOrgao());
		result.include("subscritorSel", movimentacaoBuilder.getSubscritorSel());
		result.include("titularSel", movimentacaoBuilder.getTitularSel());
	}

	@Post("/app/expediente/mov/anotar_gravar")
	public void anotar_gravar(final Integer postback, final String sigla, final String dtMovString, final DpPessoaSelecao subscritorSel,
			final boolean substituicao, final DpPessoaSelecao titularSel, final String nmFuncaoSubscritor, final String descrMov, final String obsOrgao,
			final String[] campos) {
		this.setPostback(postback);

		final ExMovimentacaoBuilder builder = ExMovimentacaoBuilder.novaInstancia();

		builder.setDtMovString(dtMovString).setSubscritorSel(subscritorSel).setSubstituicao(substituicao).setTitularSel(titularSel)
				.setNmFuncaoSubscritor(nmFuncaoSubscritor).setDescrMov(descrMov).setObsOrgao(obsOrgao);

		final ExMovimentacao mov = builder.construir(dao());

		final BuscaDocumentoBuilder documentoBuilder = BuscaDocumentoBuilder.novaInstancia().setSigla(sigla);

		buscarDocumento(documentoBuilder);

		if (!Ex.getInstance().getComp().podeFazerAnotacao(getTitular(), getLotaTitular(), documentoBuilder.getMob())) {
			throw new AplicacaoException("N�o � poss�vel fazer anota��o");
		}

		Ex.getInstance()
				.getBL()
				.anotar(getCadastrante(), getLotaTitular(), documentoBuilder.getMob(), mov.getDtMov(), mov.getLotaResp(), mov.getResp(), mov.getSubscritor(),
						mov.getTitular(), mov.getDescrMov(), mov.getNmFuncaoSubscritor());

		result.redirectTo("/app/expediente/doc/exibir?sigla=" + sigla);
	}

	@Get("/app/expediente/mov/anotar_lote")
	public void aAnotarLote() {
		final List<ExMobil> provItens = dao().consultarParaAnotarEmLote(getLotaTitular());

		final List<ExMobil> itens = new ArrayList<ExMobil>();

		for (ExMobil m : provItens) {
			if (!m.isApensado() && Ex.getInstance().getComp().podeAcessarDocumento(getTitular(), getLotaTitular(), m)) {
				itens.add(m);
			}
		}

		result.include("itens", itens);
	}

	@Post("/app/expediente/mov/anotar_lote_gravar")
	public void aAnotarLoteGravar(final Integer postback, final String dtMovString, final DpPessoaSelecao subscritorSel, final boolean substituicao,
			final DpPessoaSelecao titularSel, final String nmFuncaoSubscritor, final String descrMov, final String obsOrgao, final String[] campos) {
		this.setPostback(postback);

		ExMovimentacaoBuilder builder = ExMovimentacaoBuilder.novaInstancia();

		builder.setDtMovString(dtMovString).setSubscritorSel(subscritorSel).setSubstituicao(substituicao).setTitularSel(titularSel)
				.setNmFuncaoSubscritor(nmFuncaoSubscritor).setDescrMov(descrMov).setObsOrgao(obsOrgao);

		final ExMovimentacao mov = builder.construir(dao());

		final Pattern p = Pattern.compile("chk_([0-9]+)");

		for (final String s : getPar().keySet()) {
			if (s.startsWith("chk_") && param(s).equals("true")) {
				final Matcher m = p.matcher(s);
				if (!m.find()) {
					throw new AplicacaoException("N�o foi poss�vel ler a Id do documento e o n�mero da via.");
				}
				final ExMobil mob = dao().consultar(Long.valueOf(m.group(1)), ExMobil.class, false);

				Ex.getInstance()
						.getBL()
						.anotar(getCadastrante(), getLotaTitular(), mob, mov.getDtMov(), mov.getLotaResp(), mov.getResp(), mov.getSubscritor(),
								mov.getTitular(), mov.getDescrMov(), mov.getNmFuncaoSubscritor());
			}
		}

		result.redirectTo("/app/expediente/mov/anotar_lote");
	}

	@Get("/app/expediente/mov/vincularPapel")
	public void aVincularPapel(final String sigla) {
		final BuscaDocumentoBuilder builder = BuscaDocumentoBuilder.novaInstancia().setSigla(sigla);
		buscarDocumento(builder);

		if (!Ex.getInstance().getComp().podeFazerVinculacaoPapel(getTitular(), getLotaTitular(), builder.getMob())) {
			throw new AplicacaoException("N�o � poss�vel fazer vincula��o de papel");
		}

		result.include("sigla", sigla);
		result.include("mob", builder.getMob());
		result.include("listaTipoRespPerfil", this.getListaTipoRespPerfil());
		result.include("listaExPapel", this.getListaExPapel());
	}

	@Post("/app/expediente/mov/vincularPapel_gravar")
	public void vincularPapel_gravar(final int postback, final String sigla, final String dtMovString, final int tipoResponsavel,
			final DpPessoaSelecao responsavelSel, final DpLotacaoSelecao lotaResponsavelSel, final Long idPapel) {
		this.setPostback(postback);

		final BuscaDocumentoBuilder builder = BuscaDocumentoBuilder.novaInstancia().setSigla(sigla);
		buscarDocumento(builder);

		final ExMovimentacaoBuilder movimentacaoBuilder = ExMovimentacaoBuilder.novaInstancia();
		movimentacaoBuilder.setDtMovString(dtMovString).setResponsavelSel(responsavelSel).setLotaResponsavelSel(lotaResponsavelSel).setIdPapel(idPapel);

		if (responsavelSel == null || tipoResponsavel == 2) {
			movimentacaoBuilder.setResponsavelSel(new DpPessoaSelecao());
		}

		if (lotaResponsavelSel == null || tipoResponsavel == 1) {
			movimentacaoBuilder.setLotaResponsavelSel(new DpLotacaoSelecao());
		}

		final ExMovimentacao mov = movimentacaoBuilder.construir(dao());

		if (mov.getResp() == null && mov.getLotaResp() == null) {
			throw new AplicacaoException("N�o foi informado o respons�vel ou lota��o respons�vel para a vincula��o de papel ");
		}

		if (mov.getResp() != null) {
			mov.setDescrMov(mov.getExPapel().getDescPapel() + ":" + mov.getResp().getDescricaoIniciaisMaiusculas());
		} else {
			if (mov.getLotaResp() != null) {
				mov.setDescrMov(mov.getExPapel().getDescPapel() + ":" + mov.getLotaResp().getDescricaoIniciaisMaiusculas());
			}
		}

		if (!Ex.getInstance().getComp().podeFazerVinculacaoPapel(getTitular(), getLotaTitular(), builder.getMob())) {
			throw new AplicacaoException("N�o � poss�vel fazer vincula��o de papel");
		}

		Ex.getInstance()
				.getBL()
				.vincularPapel(getCadastrante(), getLotaTitular(), builder.getMob(), mov.getDtMov(), mov.getLotaResp(), mov.getResp(), mov.getSubscritor(),
						mov.getTitular(), mov.getDescrMov(), mov.getNmFuncaoSubscritor(), mov.getExPapel());

		result.redirectTo("/app/expediente/doc/exibir?sigla=" + sigla);
	}

	@Get("app/expediente/mov/transferir_lote")
	public void aTransferirLote() {
		final Iterator<ExMobil> provItens = dao().consultarParaTransferirEmLote(getLotaTitular());

		final List<ExMobil> itens = new ArrayList<ExMobil>();

		while (provItens.hasNext()) {
			itens.add(provItens.next());
		}

		result.include("listaTipoResp", this.getListaTipoResp());
		result.include("titular", this.getTitular());
		result.include("tiposDespacho", this.getTiposDespacho(null));
		result.include("itens", itens);
	}

	@Post("app/expediente/mov/transferir_lote_gravar")
	public void aTransferirLoteGravar(final String dtMovString, final DpPessoaSelecao subscritorSel, final boolean substituicao,
			final DpPessoaSelecao titularSel, final String nmFuncaoSubscritor, final int tipoResponsavel, final DpLotacaoSelecao lotaResponsavelSel,
			final DpPessoaSelecao lotaResponsavel, final CpOrgaoSelecao cpOrgaoSel, final String obsOrgao, final Long tpdall, final String txtall,
			final boolean checkall, final String campoDe, final String campoPara, final String campoData) {
		final ExMovimentacaoBuilder builder = ExMovimentacaoBuilder.novaInstancia();
		builder.setDtMovString(dtMovString).setSubscritorSel(subscritorSel).setSubstituicao(substituicao).setTitularSel(titularSel)
				.setNmFuncaoSubscritor(nmFuncaoSubscritor).setLotaResponsavelSel(lotaResponsavelSel).setCpOrgaoSel(cpOrgaoSel).setObsOrgao(obsOrgao);

		final ExMovimentacao mov = builder.construir(dao());

		final Pattern p = Pattern.compile("chk_([0-9]+)");
		boolean despaUnico = false;
		final Date dt = dao().dt();
		mov.setDtIniMov(dt);
		ExMobil nmobil = new ExMobil();
		final HashMap<ExMobil, AplicacaoException> MapMensagens = new HashMap<ExMobil, AplicacaoException>();
		final List<ExMobil> Mobeis = new ArrayList<ExMobil>();
		final List<ExMobil> MobilSucesso = new ArrayList<ExMobil>();

		if (mov.getResp() == null && mov.getLotaResp() == null) {
			throw new AplicacaoException("N�o foi definido o destino da transfer�ncia.");
		}
		if (tpdall != null && tpdall != 0) {
			despaUnico = true;
		}

		AplicacaoException msgErroNivelAcessoso = null;

		for (final String s : getPar().keySet()) {
			try {
				if (s.startsWith("chk_") && param(s).equals("true")) {
					final Long idTpDespacho;
					if (!despaUnico) {
						idTpDespacho = Long.valueOf(param(s.replace("chk_", "tpd_")));
					} else {
						idTpDespacho = tpdall;
					}

					ExTipoDespacho tpd = null;
					if (idTpDespacho != null && idTpDespacho > 0) {
						tpd = dao().consultar(idTpDespacho, ExTipoDespacho.class, false);
					}

					final Matcher m = p.matcher(s);
					if (!m.find()) {
						throw new AplicacaoException("N�o foi poss�vel ler a Id do documento e o n�mero da via.");
					}

					final ExMobil mobil = dao().consultar(Long.valueOf(m.group(1)), ExMobil.class, false);

					if (!Ex.getInstance().getComp().podeAcessarDocumento(getTitular(), getLotaTitular(), mobil)) {
						if (msgErroNivelAcessoso == null) {
							msgErroNivelAcessoso = new AplicacaoException("O documento n�o pode ser transferido por estar inacess�vel ao usu�rio.");
						}
						if (!(msgErroNivelAcessoso == null)) {
							MapMensagens.put(mobil, msgErroNivelAcessoso);
						}
					} else {
						String txt = "";
						if (!despaUnico) {
							txt = param(s.replace("chk_", "txt_"));
						} else {
							txt = txtall;
						}
						if (txt.equals("")) {
							txt = null;
						}

						nmobil = new ExMobil();
						nmobil = mobil;
						Mobeis.add(mobil);

						Ex.getInstance()
								.getBL()
								.transferir(mov.getOrgaoExterno(), mov.getObsOrgao(), mov.getCadastrante(), mov.getLotaTitular(), mobil, mov.getDtMov(), dt,
										mov.getDtFimMov(), mov.getLotaResp(), mov.getResp(), mov.getLotaDestinoFinal(), mov.getDestinoFinal(),
										mov.getSubscritor(), mov.getTitular(), tpd, false, txt, null, mov.getNmFuncaoSubscritor(), false, false);

					}
				}
			} catch (AplicacaoException e) {
				MapMensagens.put(nmobil, e);
			}
		}

		final ArrayList<Object> al = new ArrayList<Object>();
		final ArrayList<Object> check = new ArrayList<Object>();
		final ArrayList<Object> arrays = new ArrayList<Object>();

		if (!(MapMensagens.isEmpty())) {
			for (Iterator<Entry<ExMobil, AplicacaoException>> it = MapMensagens.entrySet().iterator(); it.hasNext();) {
				Entry<ExMobil, AplicacaoException> excep = it.next();
				final Object[] ao = { excep.getKey(), excep.getValue().getMessage() };
				System.out.println("Falha: " + excep.getKey().doc().getSigla());
				System.out.println("Mensagem de erro: " + excep.getValue().getMessage());
				al.add(ao);
			}
		}

		for (Iterator<ExMobil> it = Mobeis.iterator(); it.hasNext();) {
			ExMobil mob = it.next();
			if (!(MapMensagens.containsKey(mob))) {
				MobilSucesso.add(mob);
				System.out.println("Mobil Geral: " + mob.doc().getMobilGeral().isGeral());
				final Object[] ao = { mob.doc(), mob.getUltimaMovimentacaoNaoCancelada() };
				System.out.println("Sucesso sigla: " + mob.doc().getSigla());
				check.add(ao);
			}
		}

		Object[] arr = al.toArray();
		Object[] arr_ = check.toArray();

		al.clear();
		check.clear();

		for (int k = 0; k < arr.length; k++) {
			al.add(arr[k]);
		}

		for (int k = 0; k < arr_.length; k++) {
			check.add(arr_[k]);
		}

		arrays.add(al);
		arrays.add(check);

		result.include("cadastrante", mov.getCadastrante());
		result.include("mov", mov);
		result.include("itens", arrays);
		result.include("lotaTitular", mov.getLotaTitular());
	}

	private List<ExNivelAcesso> getListaNivelAcesso(final ExDocumento doc) {
		ExFormaDocumento exForma = doc.getExFormaDocumento();
		ExClassificacao exClassif = doc.getExClassificacaoAtual();
		ExTipoDocumento exTipo = doc.getExTipoDocumento();
		ExModelo exMod = doc.getExModelo();

		return getListaNivelAcesso(exTipo, exForma, exMod, exClassif);
	}

	public Map<Integer, String> getListaTipoResp() {
		final Map<Integer, String> map = new TreeMap<Integer, String>();
		map.put(1, "�rg�o Integrado");
		map.put(2, "Matr�cula");
		map.put(3, "�rg�o Externo");
		return map;
	}

	private Map<Integer, String> getListaTipoRespPerfil() {
		final Map<Integer, String> map = new TreeMap<Integer, String>();
		map.put(1, "Matr�cula");
		map.put(2, "�rg�o Integrado");
		return map;
	}

	@SuppressWarnings("unchecked")
	private List<ExTipoDespacho> getTiposDespacho(final ExMobil mob) {
		final List<ExTipoDespacho> tiposDespacho = new ArrayList<ExTipoDespacho>();
		tiposDespacho.add(new ExTipoDespacho(0L, "[Nenhum]", "S"));
		tiposDespacho.addAll(dao().consultarAtivos());
		tiposDespacho.add(new ExTipoDespacho(-1, "[Outros] (texto curto)", "S"));

		if (mob != null && Ex.getInstance().getComp().podeCriarDocFilho(getTitular(), getLotaTitular(), mob))
			tiposDespacho.add(new ExTipoDespacho(-2, "[Outros] (texto longo)", "S"));

		return tiposDespacho;
	}

	private byte[] toByteArray(final UploadedFile upload) throws IOException {
		final InputStream is = upload.getFile();

		// Get the size of the file
		final long tamanho = upload.getSize();

		// N�o podemos criar um array usando o tipo long.
		// � necess�rio usar o tipo int.
		if (tamanho > Integer.MAX_VALUE)
			throw new IOException("Arquivo muito grande");

		// Create the byte array to hold the data
		final byte[] meuByteArray = new byte[(int) tamanho];

		// Read in the bytes
		int offset = 0;
		int numRead = 0;
		while (offset < meuByteArray.length && (numRead = is.read(meuByteArray, offset, meuByteArray.length - offset)) >= 0) {
			offset += numRead;
		}

		// Ensure all the bytes have been read in
		if (offset < meuByteArray.length)
			throw new IOException("N�o foi poss�vel ler o arquivo completamente " + upload.getFileName());

		// Close the input stream and return bytes
		is.close();
		return meuByteArray;
	}

	@SuppressWarnings("unchecked")
	private List<ExPapel> getListaExPapel() {
		return (List<ExPapel>) HibernateUtil.getSessao().createQuery("from ExPapel").list();
	}

	private ExModelo getModelo() {
		return dao().consultarExModelo(null, "Despacho Autom�tico");
	}

	private int processarTipoResponsavel(ExMobil mob) {
		if (mob == null) {
			throw new IllegalArgumentException("Mob nao preenchido!");
		}
		ExMovimentacao ultMov = mob.getUltimaMovimentacao();

		if (ultMov.getLotaDestinoFinal() != null) {
			return 1;
		} else if (ultMov.getDestinoFinal() != null) {
			return 2;
		}
		return -1;
	}

}
