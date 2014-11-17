package br.gov.jfrj.siga.integration.test;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Method;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.PageFactory;
import org.testng.Assert;
import org.testng.SkipException;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import br.gov.jfrj.siga.page.objects.AssinaturaDigitalPage;
import br.gov.jfrj.siga.page.objects.CancelamentoJuntadaPage;
import br.gov.jfrj.siga.page.objects.OperacoesDocumentoPage;
import br.gov.jfrj.siga.page.objects.PortariaPage;
import br.gov.jfrj.siga.page.objects.PrincipalPage;
import br.gov.jfrj.siga.page.objects.VisualizacaoDossiePage;

public class ProcessoAdministrativoDigitalIT extends IntegrationTestBase {
	private String codigoDocumento;
	private String codigoProcesso;
	
	public ProcessoAdministrativoDigitalIT() throws FileNotFoundException, IOException {
		super();
	}
	
	@BeforeClass
	public void setUp() {
		try{
			efetuaLogin();			
			operacoesDocumentoPage = PageFactory.initElements(driver, OperacoesDocumentoPage.class);
			PrincipalPage principalPage = PageFactory.initElements(driver, PrincipalPage.class);
			principalPage.clicarBotaoNovoDocumentoEx();
			
			PortariaPage portariaPage = PageFactory.initElements(driver, PortariaPage.class);
			portariaPage.criaPortaria(propDocumentos);
						
			operacoesDocumentoPage.clicarLinkFinalizar();
			codigoDocumento = operacoesDocumentoPage.getTextoVisualizacaoDocumento("/html/body/div[4]/div/h2");
			
			operacoesDocumentoPage.clicarLinkAssinarDigitalmente();			
			AssinaturaDigitalPage assinaturaDigitalPage = PageFactory.initElements(driver, AssinaturaDigitalPage.class);
			assinaturaDigitalPage.registrarAssinaturaDigital(baseURL, codigoDocumento);			
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
				
				if(codigoProcesso != null) {	
					driver.get(baseURL + "/sigaex/expediente/doc/exibir.action?sigla=" + codigoProcesso);		
				} else {
					driver.get(baseURL + "/sigaex/expediente/doc/exibir.action?sigla=" + codigoDocumento);		
				}	
			}
			
			codigoProcesso = operacoesDocumentoPage.getTextoVisualizacaoDocumento("/html/body/div[4]/div[1]/h2");
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	@Test(enabled = true)
	public void autuar(){
		super.autuar(Boolean.TRUE);
	}
	
	@Test(enabled = true, priority = 1)
	public void finalizar() {
		super.finalizarProcesso();
	}
	
	@Test(enabled = true, priority = 2)
	public void assinarDigitalmente() {
		super.assinarDigitalmente(codigoProcesso, propDocumentos.getProperty("descricao"));
	}
	
	@Test(enabled = true, priority = 3)
	public void juntar() {
		// Se o documento for digital, o anterior ter� sido juntado automaticamente ao processo no evento da assinatura do processo. 
		// Clicar em "Visualizar Dossi�"
		operacoesDocumentoPage.clicarLinkVisualizarDossie();
		
		// Garantir que o n�mero da p�gina esteja aparecendo			
		VisualizacaoDossiePage visualizacaoDossiePage = PageFactory.initElements(driver, VisualizacaoDossiePage.class);	
		Assert.assertTrue(visualizacaoDossiePage.visualizaNumeroPagina(codigoDocumento), "O n�mero da p�gina n�o foi visualizado!");
		
		// Clicar em "Visualizar Movimenta��es"
		visualizacaoDossiePage.clicarLinkVisualizarMovimentacoes();
	}
	
	@Test(enabled = true, priority = 4)	
	public void cancelarJuntada() {
		// Acessar o documento juntado, por meio do link existente no TR do evento de juntada
		WebElement linkDocumentoJuntado = util.getClickableElement(driver, By.partialLinkText(codigoDocumento));
		linkDocumentoJuntado.click();
		
		// Clicar em "Desentranhar"
		operacoesDocumentoPage.clicarLinkDesentranhar();
		
		// Se o documento for digital, informar um motivo qualquer 
		CancelamentoJuntadaPage cancelamentoJuntadaPage = PageFactory.initElements(driver, CancelamentoJuntadaPage.class);
		cancelamentoJuntadaPage.cancelarJuntada(propDocumentos);
		Assert.assertTrue(util.isElementInvisible(driver, By.xpath("//tr[contains(@class, 'juntada ')]")), "Evento de juntada continua vis�vel!");

		validaDesentranhamento(codigoProcesso);
	}
	
	@Test(enabled = true, priority = 3)
	public void anexarArquivo() {
		String nomeArquivo = propDocumentos.getProperty("arquivoAnexo");
		super.anexarArquivo(nomeArquivo);
		
		// Se o documento for digital, garantir que a String "Anexo Pendente de Assinatura/Confer�ncia" apare�a na tela
		Assert.assertNotNull(util.getWebElement(driver, By.xpath(OperacoesDocumentoPage.XPATH_STATUS_DOCUMENTO + 
				"[contains(text(), 'Anexo Pendente de Assinatura/Confer�ncia')]|//div[h3 = 'Volumes']/ul/li[contains(., 'Anexo Pendente de Assinatura/Confer�ncia')]")), "Texto 'Anexo Pendente de Assinatura/Confer�ncia' n�o foi encontrado!");
		
		// Clicar em "Visualizar Dossi�"
		operacoesDocumentoPage.clicarLinkVisualizarDossie();
		
		// Garantir que o nome do anexo apare�a na tela (� a se��o OBJETO, da capa do processo)
		String documentoDossie = nomeArquivo.substring(0, nomeArquivo.indexOf(".")).toLowerCase();
		VisualizacaoDossiePage visualizacaoDossiePage = PageFactory.initElements(driver, VisualizacaoDossiePage.class);
		Assert.assertTrue(visualizacaoDossiePage.visualizaConteudo(By.xpath("//td[contains(div[@class = 'numeracao'], '" + visualizacaoDossiePage.getNumeroPagina(documentoDossie) +"') "
				+ "and contains(div[@class = 'anexo'], a[text()='" + documentoDossie +"'])]")), "O n�mero da p�gina n�o foi visualizado!");
		
		// Clicar em "Visualizar Movimenta��es"
		visualizacaoDossiePage.clicarLinkVisualizarMovimentacoes();
	}	
	
	@Test(enabled = true, priority = 4)
	public void assinarAnexo() {
		super.assinarAnexo(codigoDocumento);
		Assert.assertTrue(util.isElementInvisible(driver, By.xpath(OperacoesDocumentoPage.XPATH_STATUS_DOCUMENTO + "[contains(text(), 'Anexo Pendente de Assinatura/Confer�ncia')]")),
				"Texto 'Anexo Pendente de Assinatura/Confer�ncia' ainda est� vis�vel!");
	}
	
	@Test(enabled = true, priority = 5)
	public void cancelarAnexo() {
		super.cancelarAnexo();
		
		// Se o documento for eletr�nico, garantir que o texto "CERTID�O DE DESENTRANHAMENTO" e o nome do subscritor escolhido no cancelamento apare�am na tela
		VisualizacaoDossiePage visualizacaoDossiePage = PageFactory.initElements(driver, VisualizacaoDossiePage.class);		
		Assert.assertTrue(visualizacaoDossiePage.visualizaConteudo(By.xpath("//div[@class='documento'][contains(., 'CERTID�O DE DESENTRANHAMENTO')"+ 
		" and contains(., '" + propDocumentos.getProperty("nomeResponsavel").toUpperCase() + "')]")), "Certid�o de desentranhamento ou nome do subscritor n�o encontrado!");
		
		// Clicar em "Visualizar Movimenta��es"
		visualizacaoDossiePage.clicarLinkVisualizarMovimentacoes();
	}
	
	@Test(enabled = true, priority = 6)
	public void encerrarVolume() {
		super.encerrarVolume();
		
		// Clicar em "Ver/Assinar" (no mesmo <tr> do evento Encerramento de Volume).  - Garantir que o texto "encerrei o volume 1" apare�a na tela 
		Assert.assertTrue(operacoesDocumentoPage.clicarAssinarEncerramentoVolume(), "Texto 'encerrei o volume' n�o foi visualizado!");		
	}
	
	@AfterClass
	public void tearDown() throws Exception {
		efetuaLogout();
	}
}