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
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import br.gov.jfrj.siga.page.objects.EditaDocumentoPage;
import br.gov.jfrj.siga.page.objects.MemorandoPage;
import br.gov.jfrj.siga.page.objects.OficioPage;
import br.gov.jfrj.siga.page.objects.OperacoesDocumentoPage;
import br.gov.jfrj.siga.page.objects.PortariaPage;
import br.gov.jfrj.siga.page.objects.PrincipalPage;

public class CriacaoDocumentoIT extends IntegrationTestBase{
	private PrincipalPage principalPage;
	private EditaDocumentoPage editaDocumentoPage;
	
	public CriacaoDocumentoIT() throws FileNotFoundException, IOException {
		super();
	}

	@BeforeClass	
	@Parameters("infoDocumentos")
	public void setUp(String infoDocumentos) {
		try {
			efetuaLogin();
			principalPage = PageFactory.initElements(driver, PrincipalPage.class);
			editaDocumentoPage = PageFactory.initElements(driver, EditaDocumentoPage.class);
			operacoesDocumentoPage = PageFactory.initElements(driver, OperacoesDocumentoPage.class);
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
			
			if(driver.getCurrentUrl().contains("exibir.action") && util.getClickableElement(driver, By.linkText("Editar")) != null) {
				operacoesDocumentoPage.clicarLinkEditar();
			} else {
				if(!driver.getTitle().equals("SIGA - P�gina Inicial")) {
					driver.get(baseURL + "/siga");
				}
				principalPage.clicarBotaoNovoDocumentoEx();
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
	}

	@Test(enabled = true)
	public void criaDocumentoExterno() {
		editaDocumentoPage.preencheDocumentoExterno(propDocumentos);
		WebElement divVisualizacaoDocumento = util.getWebElement(driver, By.cssSelector("div.gt-content-box"));		
		Assert.assertNotNull(util.getWebElement(driver, divVisualizacaoDocumento, By.xpath("//p[contains(., 'EXPEDIENTE EXTERNO N�')]")), "Texto Expediente Externo N� TMP n�o foi encontrado!");
	}

	@Test(enabled = true)
	public void criaDocumentoInternoImportado() {
		editaDocumentoPage.preencheDocumentoInternoImportado(propDocumentos);
		WebElement divVisualizacaoDocumento = util.getWebElement(driver, By.cssSelector("div.gt-content-box"));
		Assert.assertNotNull(util.getWebElement(driver, divVisualizacaoDocumento, By.xpath("//b[contains(., 'Expediente Interno N� TMP')]")), "Texto Expediente Interno N� TMP n�o foi encontrado!");
	}
	
	@Test(enabled = true)
	public void criaMemorando() {
		MemorandoPage memorandoPage = PageFactory.initElements(driver, MemorandoPage.class);
		memorandoPage.criaMemorando(propDocumentos);
		WebElement divVisualizacaoDocumento = util.getWebElement(driver, By.cssSelector("div.gt-content-box"));
		Assert.assertNotNull(util.getWebElement(driver, divVisualizacaoDocumento, By.xpath("//p[contains(., 'MEMORANDO N� TMP')]")), "Texto MEMORANDO N� TMP n�o foi encontrado!");
		Assert.assertNotNull(util.getWebElement(driver, divVisualizacaoDocumento, By.xpath("//p[contains(., 'Atenciosamente')]")), "Fecho n�o encontrado");
	}
	
	@Test(enabled = true)
	public void criaPortaria() {
		PortariaPage portariaPage = PageFactory.initElements(driver, PortariaPage.class);
		portariaPage.criaPortaria(propDocumentos);
		WebElement divVisualizacaoDocumento = util.getWebElement(driver, By.cssSelector("div.gt-content-box"));
		Assert.assertNotNull(util.getWebElement(driver, divVisualizacaoDocumento, By.xpath("//p[contains(., 'PORTARIA N� TMP')]")), "Texto PORTARIA N� TMP n�o foi encontrado!");
		Assert.assertNotNull(util.getWebElement(driver, divVisualizacaoDocumento, By.xpath("//p[contains(., 'Testes de Integra��o')]")), "Informa��o sobre o que Disp�e o documento n�o encontrada!");
		Assert.assertNotNull(util.getWebElement(driver, divVisualizacaoDocumento, By.xpath("//span[contains(., 'Exmo. Sr. Juiz Federal')]")), "Texto do memorando n�o encontrado!");		
	}
	
	@Test(enabled = true)
	public void criaOficio() {
		OficioPage oficioPage = PageFactory.initElements(driver, OficioPage.class);
		oficioPage.criaOficio(propDocumentos);
		WebElement divVisualizacaoDocumento = util.getWebElement(driver, By.cssSelector("div.gt-content-box"));
		Assert.assertNotNull(util.getWebElement(driver, divVisualizacaoDocumento, By.xpath("//p[contains(., 'OF�CIO N� TMP')]")), "Texto OF�CIO N� TMP n�o foi encontrado!");
		Assert.assertNotNull(util.getWebElement(driver, divVisualizacaoDocumento, By.xpath("//p[contains(., '" + propDocumentos.getProperty("enderecoDestinatario") + "')]")), 
				"Endere�o n�o encontrado!");
		Assert.assertNotNull(util.getWebElement(driver, divVisualizacaoDocumento, By.xpath("//p[contains(., 'Senhora Juiza')]")), "Forma de Tratamento n�o encontrada!");
	}
	
	@AfterClass
	public void tearDown() throws Exception {
		driver.quit();
	}
}