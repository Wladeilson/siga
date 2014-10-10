package br.gov.jfrj.siga.integration.test;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Method;

import org.openqa.selenium.By;
import org.openqa.selenium.support.PageFactory;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import br.gov.jfrj.siga.integration.test.util.IntegrationTestUtil;
import br.gov.jfrj.siga.page.objects.AnexoPage;
import br.gov.jfrj.siga.page.objects.AssinaturaDigitalPage;
import br.gov.jfrj.siga.page.objects.OperacoesDocumentoPage;
import br.gov.jfrj.siga.page.objects.PortariaPage;
import br.gov.jfrj.siga.page.objects.PrincipalPage;
import br.gov.jfrj.siga.page.objects.TransferenciaPage;

public class AcoesDocumentoDigitalIT extends IntegrationTestBase {
	private PrincipalPage principalPage;
	private OperacoesDocumentoPage operacoesDocumentoPage;
	private String codigoDocumento;
	private IntegrationTestUtil util;
	
	public AcoesDocumentoDigitalIT() throws FileNotFoundException, IOException {
		super();
		util = new IntegrationTestUtil();
	}
	
	@BeforeClass	
	public void setUp() {
		try {
			efetuaLogin();
			principalPage = PageFactory.initElements(driver, PrincipalPage.class);
			operacoesDocumentoPage = PageFactory.initElements(driver, OperacoesDocumentoPage.class);
			
			PortariaPage portariaPage = PageFactory.initElements(driver, PortariaPage.class);
			principalPage.clicarBotaoNovoDocumentoEx();
			portariaPage.criaPortaria(propDocumentos);
		} catch (Exception e) {
			e.printStackTrace();
			driver.quit();
		} 
	}
	
	@BeforeMethod
	public void paginaInicial(Method method) {
		try {
			System.out.println("BeforeMethod: " + method.getName() + " - Titulo p�gina: " + driver.getTitle());
			if(!driver.getCurrentUrl().contains("exibir.action")) {
				System.out.println("Efetuando busca!");
				driver.get(baseURL + "/sigaex/expediente/doc/exibir.action?sigla=" + codigoDocumento);			
			}
			
			codigoDocumento = operacoesDocumentoPage.getTextoVisualizacaoDocumento("/html/body/div[4]/div/h2");
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	@Test(enabled = true, priority = 1)
	public void finalizarDocumento() {
		operacoesDocumentoPage.clicarLinkFinalizar();
		Assert.assertTrue(operacoesDocumentoPage.getTextoVisualizacaoDocumento("/html/body/div[4]/div/h3[1]")
				.contains("Pendente de Assinatura, Como Subscritor"), "Texto Pendente de Assinatura, Como Subscritor n�o foi encontrado!");		 
	}
	
	@Test(enabled = true, priority = 2)
	public void assinarDigitalmente() {
		operacoesDocumentoPage.clicarLinkAssinarDigitalmente();
		AssinaturaDigitalPage assinaturaDigitalPage = PageFactory.initElements(driver, AssinaturaDigitalPage.class);
		assinaturaDigitalPage.registrarAssinaturaDigital(baseURL, codigoDocumento);	
		Assert.assertNotNull(util.getWebElement(driver, By.xpath("//h3[1][contains(text(), 'Aguardando Andamento')]")), "Texto Aguardando Andamento n�o foi encontrado!");
		//Assert.assertNotNull(util.getWebElement(driver, By.xpath("//td[2][contains(., 'Assinatura')]")), "Linha de registro da assinatura n�o encontrada!");
	}
	
	@Test(enabled = true, priority = 1)
	public void anexarArquivo() {
		operacoesDocumentoPage.clicarLinkAnexarArquivo();
		AnexoPage anexoPage = PageFactory.initElements(driver, AnexoPage.class);
		anexoPage.anexarArquivo(propDocumentos);	
		Assert.assertNotNull(util.getWebElement(driver, By.linkText(propDocumentos.getProperty("arquivoAnexo").toLowerCase())), "Nome do arquivo selecionado n�o encontrado na tela!");
		anexoPage.clicarBotaovoltar();
		Assert.assertNotNull(util.getWebElement(driver, By.xpath("//h3[1][contains(text(), 'Anexo Pendente de Assinatura/Confer�ncia')]")), 
				"Texto Anexo Pendente de Assinatura/Confer�ncia n�o foi encontrado!");
	}
	
	@Test(enabled = true, priority = 3)
	public void despacharDocumento() {
		operacoesDocumentoPage.clicarLinkDespacharTransferir();
		TransferenciaPage transferenciaPage = PageFactory.initElements(driver, TransferenciaPage.class);
		transferenciaPage.despacharDocumento(propDocumentos);
		Assert.assertNotNull(util.getWebElement(driver, By.xpath("//td[4][contains(., '" + propDocumentos.getProperty("despacho") + "')]")),
				"Texto " + propDocumentos.getProperty("despacho") + " n�o encontrado.");
	}

	@AfterClass
	public void tearDown() throws Exception {
		efetuaLogout();
		driver.quit();
	}
}