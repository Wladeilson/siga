package br.gov.jfrj.siga.integration.test;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Calendar;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.PageFactory;
import org.testng.Assert;
import org.testng.SkipException;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import br.gov.jfrj.siga.page.objects.AgendamentoPublicacaoPage;
import br.gov.jfrj.siga.page.objects.AnotacaoPage;
import br.gov.jfrj.siga.page.objects.ApensacaoPage;
import br.gov.jfrj.siga.page.objects.CancelamentoMovimentacaoPage;
import br.gov.jfrj.siga.page.objects.DefinePerfilPage;
import br.gov.jfrj.siga.page.objects.DesapensamentoPage;
import br.gov.jfrj.siga.page.objects.InclusaoCossignatarioPage;
import br.gov.jfrj.siga.page.objects.OficioPage;
import br.gov.jfrj.siga.page.objects.OperacoesDocumentoPage;
import br.gov.jfrj.siga.page.objects.PrincipalPage;
import br.gov.jfrj.siga.page.objects.RedefineNivelAcessoPage;
import br.gov.jfrj.siga.page.objects.TransferenciaPage;
import br.gov.jfrj.siga.page.objects.VinculacaoPage;
import br.gov.jfrj.siga.page.objects.VisualizacaoDossiePage;

public class AcoesDocumentoIT extends IntegrationTestBase {
	private String codigoDocumento;
	
	public AcoesDocumentoIT() throws FileNotFoundException, IOException {
		super();
	}
	
	@BeforeClass	
	public void setUp() {
		try {
			efetuaLogin();
			PrincipalPage principalPage = PageFactory.initElements(driver, PrincipalPage.class);
			operacoesDocumentoPage = PageFactory.initElements(driver, OperacoesDocumentoPage.class);
			
			principalPage.clicarBotaoNovoDocumentoEx();
			OficioPage oficioPage = PageFactory.initElements(driver, OficioPage.class);
			oficioPage.criaOficio(propDocumentos);								
		} catch (Exception e) {
			e.printStackTrace();
			driver.quit();
			throw new SkipException("Exce��o no m�todo setUp!");
		} 
	}
	
	@BeforeMethod
	public void paginaInicial(Method method) {
		try {
			System.out.println("BeforeMethod: " + method.getName() + " - Titulo p�gina: " + driver.getTitle());
			if(!driver.getCurrentUrl().contains("exibir.action") || driver.getTitle().contains("SIGA - Erro Geral")) {
				System.out.println("Efetuando busca!");
				driver.get(baseURL + "/sigaex/expediente/doc/exibir.action?sigla=" + codigoDocumento);				
			}
			
			codigoDocumento = operacoesDocumentoPage.getTextoVisualizacaoDocumento("/html/body/div[4]/div/h2");
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	@Test(enabled = true)
	public void duplicarDocumento() {
		operacoesDocumentoPage.clicarLinkDuplicar();
		operacoesDocumentoPage.clicarLinkExcluir();
		Assert.assertTrue(driver.getTitle().equals("SIGA - P�gina Inicial"), "A a��o n�o direcionou para a p�gina inicial!");
	}
	
	@Test(enabled = false)
	public void incluiCossignatario() {
		operacoesDocumentoPage.clicarLinkIncluirCossignatario();
		InclusaoCossignatarioPage inclusaoCossignatarioPage = PageFactory.initElements(driver, InclusaoCossignatarioPage.class);
		inclusaoCossignatarioPage.incluiCossignatario(propDocumentos);
		Assert.assertTrue(operacoesDocumentoPage.getTextoVisualizacaoDocumento("/html/body/div[4]/div/div/table/tbody/tr/td[4]/a/span").contains(propDocumentos.getProperty("nomeCossignatario")),
				"Nome do cossignat�rio n�o encontrado!");
		operacoesDocumentoPage.excluirCossignatario();		
		Assert.assertTrue(util.isElementInvisible(driver, By.cssSelector("/html/body/div[4]/div/div/table/tbody/tr/td[4]/a/span")), "Nome do cossignat�rio continua aparecendo na tela!");
	}
	
	@Test(enabled = true, priority = 2)
	public void anexarArquivo() {
		super.anexarArquivo(propDocumentos.getProperty("arquivoAnexo"));
	}
	
	@Test(enabled = true, priority = 3)
	public void assinarAnexo() {
		super.assinarAnexo(codigoDocumento);
	}
	
	@Test(enabled = true, priority = 1)
	public void finalizarDocumento() {
		super.finalizarDocumento(); 
	}
	
	@Test(enabled = true, priority = 3)
	public void fazerAnotacao() {
		operacoesDocumentoPage.clicarLinkFazerAnotacao();
		AnotacaoPage anotacaoPage = PageFactory.initElements(driver, AnotacaoPage.class);
		anotacaoPage.fazerAnotacao(propDocumentos);
		WebElement descricaoAnotacao = util.getWebElement(driver, By.xpath("//td[4][contains(., 'Teste de anota��o')]"));
		Assert.assertNotNull(descricaoAnotacao, "Conte�do da anota��o n�o encontrado!");
		WebElement linkExcluir = util.getWebElement(driver, descricaoAnotacao, By.linkText("Excluir"));
		Assert.assertNotNull(linkExcluir, "Link para exclus�o da anota��o n�o encontrado!");
		util.getClickableElement(driver, linkExcluir);
		linkExcluir.click();
		Assert.assertTrue(util.isElementInvisible(driver, By.xpath("//td[4][contains(., 'Teste de anota��o')]")), "Anota��o continua sendo exibida");
	}
	
	@Test(enabled = true, priority = 5)
	public void redefineNivelAcesso() {
		operacoesDocumentoPage.clicarLinkRedefinirNivelAcesso();
		RedefineNivelAcessoPage redefineNivelAcessoPage = PageFactory.initElements(driver, RedefineNivelAcessoPage.class);
		redefineNivelAcessoPage.redefineNivelAcesso(propDocumentos);
		Assert.assertNotNull(util.getWebElement(driver, By.xpath("(//p/b[contains(.,'" + propDocumentos.getProperty("nivelAcesso") + "')])")), "N�vel de acesso n�o foi modificado para p�blico");		
/*		operacoesDocumentoPage.clicarLinkDesfazerRedefinicaoSigilo();		
		Assert.assertNotNull(util.getWebElement(driver, By.xpath("(//p/b[contains(.,'P�blico')])")), "N�vel de acesso n�o foi modificado para p�blico");*/
	}
	
	@Test(enabled = true, priority = 3)
	public void definirPerfil() throws InterruptedException {
		operacoesDocumentoPage.clicarLinkDefinirPerfil();
		DefinePerfilPage definePerfilPage = PageFactory.initElements(driver, DefinePerfilPage.class);
		definePerfilPage.definirPerfil(propDocumentos);
		WebElement divPerfil = util.getContentDiv(driver, By.cssSelector("div.gt-sidebar-content"), propDocumentos.getProperty("perfil"));		

		Assert.assertNotNull(divPerfil, "Texto 'Interessado' n�o encontrado!");
		Assert.assertTrue(divPerfil.getText().toUpperCase().contains(propDocumentos.getProperty("nomeResponsavel").toUpperCase()), "Nome do usu�rio respons�vel n�o encontrado!");			
		operacoesDocumentoPage.clicarLinkDesfazerDefinicaoPerfil();
		Assert.assertTrue(util.isElementInvisible(driver, By.xpath("//p[contains(., '" + propDocumentos.getProperty("perfil") + "')]")), "Texto " + propDocumentos.getProperty("perfil") + " continua vis�vel!");
	}
	
	@Test(enabled = true, priority = 2)
	public void criarVia() {
		operacoesDocumentoPage.clicarCriarVia();
		WebElement divVias = util.getContentDiv(driver, By.cssSelector("div.gt-sidebar-content"), "Vias");		
		Assert.assertNotNull(divVias, "Texto 'Vias' n�o encontrado!");
		
		List<WebElement> listItems = divVias.findElements(By.tagName("li"));
		int i;
		for (i = 1; i < listItems.size(); i++) {
			WebElement listItem = listItems.get(i);
			if(!listItem.getText().contains("Cancelado")) {
				util.getClickableElement(driver, listItem.findElement(By.tagName("a"))).click();
				break;
			}
		}
		operacoesDocumentoPage.clicarCancelarVia();
		Assert.assertNotNull(util.getWebElement(driver, By.xpath("(//ul/li["+ (i+1) +"][contains(.,'Cancelado')])")), "Texto Cancelado n�o encontrado!");
	}
	
	@Test(enabled = true, priority = 3)
	public void registrarAssinaturaManual() {		
		super.registrarAssinaturaManual();
		//Assert.assertNotNull(util.getWebElement(driver, By.xpath("//td[2][contains(., 'Registro de Assinatura')]")), "Texto 'Registro de Assinatura' n�o encontrado!");
	}
	
	@Test(enabled = true, priority = 3)
	public void assinarDigitalmente() {
		super.assinarDigitalmente(codigoDocumento, "N�");
		//Assert.assertNotNull(util.getWebElement(driver, By.xpath("//td[2][contains(., 'Assinatura')]")), "Texto 'Assinatura' n�o encontrado!");
	}
	
	@Test(enabled = true, priority = 4)
	public void agendarPublicacao() {
		operacoesDocumentoPage.clicarLinkAgendarPublicacao();
		AgendamentoPublicacaoPage agendamentoPublicacaoPage = PageFactory.initElements(driver, AgendamentoPublicacaoPage.class);
		Assert.assertTrue(agendamentoPublicacaoPage.visualizaPagina(), "N�o foi poss�vel visualizar os bot�es da p�gina de agendamento corretamente!");
	}
	
	@Test(enabled = true, priority = 4)
	public void solicitarPublicacaoBoletim() {
		operacoesDocumentoPage.clicarLinkSolicitarPublicacaoBoletim();
		
		if(Calendar.getInstance().get(Calendar.HOUR_OF_DAY) >= 17) {
			Assert.assertNotNull(util.getWebElement(driver, By.xpath("//h3[contains(., 'A solicita��o de publica��o no BIE apenas � permitida at� as 17:00')]")),
					"Texto 'A solicita��o de publica��o no BIE apenas � permitida at� as 17:00' n�o foi encontrado!");
		} else {
			Assert.assertNotNull(util.getWebElement(driver, By.xpath("//td[3][contains(., 'Solicita��o de Publica��o no Boletim')]")), "Texto 'Solicita��o de Publica��o no Boletim' n�o foi encontrado!");		
			operacoesDocumentoPage.clicarLinkDesfazerSolicitacaoPublicacaoBoletim();
			Assert.assertNotNull(util.isElementInvisible(driver, By.xpath("//td[3][contains(., 'Solicita��o de Publica��o no Boletim')]")), 
					"Texto 'Solicita��o de Publica��o no Boletim' continua sendo exibido!");
			Assert.assertNotNull(util.getWebElement(driver, By.linkText("Solicitar Publica��o no Boletim")), "Texto Solicitar Publica��o no Boletim n�o foi encontrado!");		
		}
	}
	
	@Test(enabled = true, priority = 4)
	public void sobrestar() {
		operacoesDocumentoPage.clicarLinkSobrestar();
		Assert.assertNotNull(util.getWebElement(driver, By.xpath(OperacoesDocumentoPage.XPATH_STATUS_DOCUMENTO + "[contains(text(), 'Sobrestado')]|//div[h3 = 'Vias']/ul/li[contains(., 'Sobrestado')]")), "Texto 'Sobrestado' n�o encontrado!");	
		operacoesDocumentoPage.clicarLinkDesobrestar();
		Assert.assertNotNull(util.getWebElement(driver, By.xpath(OperacoesDocumentoPage.XPATH_STATUS_DOCUMENTO + "[contains(text(), 'Aguardando Andamento')]|//div[h3 = 'Vias']/ul/li[contains(., 'Aguardando Andamento')]")), "Texto 'Aguardando Andamento' n�o encontrado!");	
		//Assert.assertNotNull(util.getWebElement(driver, By.xpath("//td[2][contains(., 'Desobrestar')]")), "Texto 'Desobrestar' n�o encontrado!");
	}
	
	@Test(enabled = true, priority = 4)
	public void vincularDocumento() {
		operacoesDocumentoPage.clicarLinkVincular();
		VinculacaoPage vinculacaoPage = PageFactory.initElements(driver, VinculacaoPage.class);		
		String documentoApensado = vinculacaoPage.vincularDocumento(propDocumentos, codigoDocumento);
		WebElement documentosRelationados = util.getWebElement(driver, By.id("outputRelacaoDocs"));		
		Assert.assertNotNull(documentosRelationados, "�rea de Documentos Relacionados n�o foi encontrada!");
		Assert.assertTrue(documentosRelationados.getText().contains(documentoApensado), "C�digo do documento vinculado n�o foi encontrado!");
				
		operacoesDocumentoPage.clicarLinkExibirInformacoesCompletas();
		WebElement vinculacao = util.getWebElement(driver, By.xpath("//td[7][contains(., 'Ver tamb�m:')]"));
		Assert.assertNotNull(vinculacao, "Texto 'Ver tamb�m:' n�o encontrado");
		util.getClickableElement(driver, vinculacao.findElement(By.linkText("Cancelar"))).click();

		CancelamentoMovimentacaoPage cancelamentoMovimentacaoPage = PageFactory.initElements(driver, CancelamentoMovimentacaoPage.class);
		cancelamentoMovimentacaoPage.cancelarMovimentacao(propDocumentos);
		Assert.assertTrue(util.isElementInvisible(driver, By.id("outputRelacaoDocs")), "�rea de Documentos Relacionados ainda est� vis�vel!");
	}
	
	@Test(enabled = true, priority = 4)
	public void arquivarCorrente() {
		operacoesDocumentoPage.clicarLinkArquivarCorrente();
		Assert.assertNotNull(util.getWebElement(driver, By.xpath(OperacoesDocumentoPage.XPATH_STATUS_DOCUMENTO + "[contains(., 'Arquivado Corrente')]|//div[h3 = 'Vias']/ul/li[contains(., 'Arquivado Corrente')]")), "Texto Arquivado Corrente n�o foi encontrado!");
		operacoesDocumentoPage.clicarLinkDesfazerArquivamentoCorrente();
		Assert.assertNotNull(util.getWebElement(driver, By.xpath(OperacoesDocumentoPage.XPATH_STATUS_DOCUMENTO + "[contains(text(), 'Aguardando Andamento')]|//div[h3 = 'Vias']/ul/li[contains(., 'Aguardando Andamento')]")), "Texto 'Aguardando Andamento' n�o foi encontrado!");	
	}
	
	@Test(enabled = true, priority = 4)
	public void apensarDocumento() {
		operacoesDocumentoPage.clicarLinkApensar();
		ApensacaoPage apensacaoPage = PageFactory.initElements(driver, ApensacaoPage.class);
		String documentoApensado = apensacaoPage.apensarDocumento(propDocumentos, codigoDocumento);
		WebElement documentosRelacionados = util.getWebElement(driver, By.id("outputRelacaoDocs"));		
		if(documentosRelacionados == null ) {
			Assert.assertNotNull(util.getWebElement(driver, By.xpath("//h3[text() = 'N�o � poss�vel apensar a um documento n�o finalizado' or "
					+ "text() = 'N�o � poss�vel apensar um volume aberto a um volume encerrado']")), "Documento n�o apensado e mensagem de erro esperada n�o encontrada!");
		} else {
		//Assert.assertNotNull(documentosRelacionados, "�rea de Documentos Relacionados n�o foi encontrada!");
		Assert.assertTrue(documentosRelacionados.getText().contains(documentoApensado), "C�digo do documento apensado n�o foi encontrado!");

		operacoesDocumentoPage.clicarLinkDesapensar();
		DesapensamentoPage desapensamentoPage = PageFactory.initElements(driver, DesapensamentoPage.class);
		desapensamentoPage.desapensarDocumento(propDocumentos);
		Assert.assertNotNull(util.getWebElement(driver, By.xpath(OperacoesDocumentoPage.XPATH_STATUS_DOCUMENTO + "[contains(text(), 'Aguardando Andamento')]|//div[h3 = 'Vias']/ul/li[contains(., 'Aguardando Andamento')]")), "Texto 'Aguardando Andamento' n�o foi encontrado!");	
		Assert.assertTrue(util.isElementInvisible(driver, By.id("outputRelacaoDocs")), "�rea de Documentos Relacionados ainda est� vis�vel!");	
		}
	}
	
	@Test(enabled = true, priority = 4)
	public void despacharDocumento() {
		operacoesDocumentoPage.clicarLinkDespacharTransferir();
		TransferenciaPage transferenciaPage = PageFactory.initElements(driver, TransferenciaPage.class);
		transferenciaPage.despacharDocumento(propDocumentos);
		Assert.assertNotNull(util.getWebElement(driver, By.xpath("//td[4][contains(., '" + propDocumentos.getProperty("despacho") + "')]")), "Texto do despacho n�o encontrado!");
	}
	
	@Test(enabled = true, priority = 5)
	public void assinarDespacho() {
		operacoesDocumentoPage.clicarAssinarDespacho(baseURL, codigoDocumento);
		Assert.assertNotNull(util.getWebElement(driver, By.xpath("//td[4][contains(., 'Assinado por')]")), "Texto 'Assinado por' n�o foi encontrado!");
	}
	  
	@Test(enabled = true, priority = 4)
	public void transferirDocumento() {
		operacoesDocumentoPage.clicarLinkDespacharTransferir();
		TransferenciaPage transferenciaPage = PageFactory.initElements(driver, TransferenciaPage.class);
		transferenciaPage.transferirDocumento(propDocumentos);
		Assert.assertNotNull(util.getWebElement(driver, By.xpath(OperacoesDocumentoPage.XPATH_STATUS_DOCUMENTO + "[contains(text(), 'A Receber (F�sico)')]|//div[h3 = 'Vias']/ul/li[contains(., 'A Receber (F�sico)')]")), "Texto 'A Receber (F�sico)' n�o foi encontrado!");	
		
		operacoesDocumentoPage.clicarLinkExibirInformacoesCompletas();
		operacoesDocumentoPage.clicarProtocolo();
		operacoesDocumentoPage.clicarLinkDesfazerTransferencia();
		Assert.assertNotNull(util.getWebElement(driver, By.xpath(OperacoesDocumentoPage.XPATH_STATUS_DOCUMENTO + "[contains(text(), 'Aguardando Andamento')]|//div[h3 = 'Vias']/ul/li[contains(., 'Aguardando Andamento')]")), "Texto 'Aguardando Andamento' n�o foi encontrado!");	
	}
	
	@Test(enabled = true, priority = 4)
	public void despachoDocumentoFilho() {
		operacoesDocumentoPage.clicarLinkDespacharTransferir();
		TransferenciaPage transferenciaPage = PageFactory.initElements(driver, TransferenciaPage.class);
		String codigoDocumentoJuntado = transferenciaPage.despachoDocumentoFilho(propDocumentos, codigoDocumento);
		WebElement juntada = util.getWebElement(driver, By.xpath("//td[4][contains(., 'Documento juntado:')]"));
		
		Assert.assertNotNull(juntada, "Texto 'Documento juntado:' n�o foi encontrado!");	
		Assert.assertTrue(juntada.getText().contains(codigoDocumentoJuntado), "C�digo do documento juntado n�o encontrado!");	
	}
	
	@Test(enabled = true, priority = 4)
	public void visualizarDossie() {
		operacoesDocumentoPage.clicarLinkVisualizarDossie();
		VisualizacaoDossiePage visualizacaoDossiePage = PageFactory.initElements(driver, VisualizacaoDossiePage.class);
		Assert.assertTrue(visualizacaoDossiePage.visualizarDossie(), "Texto 'DESPACHO N�' n�o foi encontrado");
	}
	
	@AfterClass
	public void tearDown() throws Exception {
		driver.quit();
	}
}
