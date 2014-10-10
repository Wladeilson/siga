package br.gov.jfrj.siga.integration.test;

import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;

import org.openqa.selenium.support.PageFactory;
import org.testng.Assert;
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
	private OperacoesDocumentoPage operacoesDocumentoPage;
	private Properties propDocumentos = new Properties();
	
	@Parameters({ "baseURL"})
	public CriacaoDocumentoIT(String baseURL) {
		super(baseURL);
	}

	@BeforeClass	
	@Parameters("infoDocumentos")
	public void setUp(String infoDocumentos) {
		File file = new File(infoDocumentos);
		try {
			efetuaLogin();
			propDocumentos.load(new FileInputStream(file));
			principalPage = PageFactory.initElements(driver, PrincipalPage.class);
			editaDocumentoPage = PageFactory.initElements(driver, EditaDocumentoPage.class);
			operacoesDocumentoPage = PageFactory.initElements(driver, OperacoesDocumentoPage.class);
		} catch (Exception e) {
			e.printStackTrace();
			driver.quit();
		} 
	}
	
	@BeforeMethod
	public void paginaInicial() {
		try {
			System.out.println("BeforeMethod... Titulo p�gina: " + driver.getTitle());
			if(!driver.getTitle().equals("SIGA - P�gina Inicial")) {
				driver.get(baseURL + "/siga");
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
	}

	@Test(enabled = true)
	public void criaDocumentoExterno() {
		principalPage.clicarBotaoNovoDocumentoEx();
		editaDocumentoPage.preencheDocumentoExterno(propDocumentos);
		Assert.assertTrue(operacoesDocumentoPage.getTextoVisualizacaoDocumento("/html/body/div[5]/div[1]/div/table/tbody/tr/td/table[1]/tbody/tr[3]/td/table/tbody/tr/td[1]/p")
				.contains("Expediente Externo N� TMP"), "Texto Expediente Externo N� TMP n�o foi encontrado!");
		operacoesDocumentoPage.clicarLinkEditar();
	}

	@Test(enabled = true)
	public void criaDocumentoInternoImportado() {
		principalPage.clicarBotaoNovoDocumentoEx();
		editaDocumentoPage.preencheDocumentoInternoImportado(propDocumentos);
		Assert.assertTrue(operacoesDocumentoPage.getTextoVisualizacaoDocumento("/html/body/div[5]/div[1]/div/table/tbody/tr/td/table[2]/tbody/tr/td[1]/b")
				.contains("Expediente Interno N� TMP"), "Texto Expediente Interno N� TMP n�o foi encontrado!");
	}
	
	@Test(enabled = true)
	public void criaMemorando() {
		MemorandoPage memorandoPage = PageFactory.initElements(driver, MemorandoPage.class);
		principalPage.clicarBotaoNovoDocumentoEx();
		memorandoPage.criaMemorando(propDocumentos);
		Assert.assertTrue(operacoesDocumentoPage.getTextoVisualizacaoDocumento("/html/body/div[5]/div[1]/div/table/tbody/tr/td/span/table/tbody/tr[1]/td/p")
				.contains("MEMORANDO N� TMP"), "Texto MEMORANDO N� TMP n�o foi encontrado!");
		Assert.assertTrue(operacoesDocumentoPage.getTextoVisualizacaoDocumento("/html/body/div[5]/div[1]/div/table/tbody/tr/td/span/span/p[1]")
				.contains("Atenciosamente"), "Fecho n�o encontrado");
	}
	
	@Test(enabled = true)
	public void criaPortaria() {
		PortariaPage portariaPage = PageFactory.initElements(driver, PortariaPage.class);
		principalPage.clicarBotaoNovoDocumentoEx();
		portariaPage.criaPortaria(propDocumentos);
		Assert.assertTrue(operacoesDocumentoPage.getTextoVisualizacaoDocumento("/html/body/div[5]/div[1]/div/table/tbody/tr/td/span/table[1]/tbody/tr/td/p")
				.contains("PORTARIA N� TMP"), "Texto PORTARIA N� TMP n�o foi encontrado!");
		Assert.assertTrue(operacoesDocumentoPage.getTextoVisualizacaoDocumento("/html/body/div[5]/div[1]/div/table/tbody/tr/td/span/table[2]/tbody/tr/td[2]/p")
				.contains("Testes de Integra��o"), "Informa��o sobre o que Disp�e o documento n�o encontrada!");
		Assert.assertTrue(operacoesDocumentoPage.getTextoVisualizacaoDocumento("/html/body/div[5]/div[1]/div/table/tbody/tr/td/span/div/p[1]/span")
				.contains("Exmo. Sr. Juiz Federal"), "Texto do memorando n�o encontrado!");
	}
	
	@Test(enabled = true)
	public void criaOficio() {
		OficioPage oficioPage = PageFactory.initElements(driver, OficioPage.class);
		principalPage.clicarBotaoNovoDocumentoEx();
		oficioPage.criaOficio(propDocumentos);
		Assert.assertTrue(operacoesDocumentoPage.getTextoVisualizacaoDocumento("/html/body/div[5]/div[1]/div/table/tbody/tr/td/span/table[1]/tbody/tr[1]/td/p")
				.contains("OF�CIO N� TMP"), "Texto OF�CIO N� TMP n�o foi encontrado!");
		Assert.assertTrue(operacoesDocumentoPage.getTextoVisualizacaoDocumento("//table/tbody/tr/td/span/p[contains(.,'"+ propDocumentos.getProperty("enderecoDestinatario") + "')]")
				.contains("Rio de Janeiro"), "Endere�o n�o encontrado!");
		Assert.assertTrue(operacoesDocumentoPage.getTextoVisualizacaoDocumento("/html/body/div[5]/div[1]/div/table/tbody/tr/td/span/div/p[2]")
				.contains("Senhora Juiza"), "Forma de Tratamento n�o encontrada!");
	}
	
	@AfterClass
	public void tearDown() throws Exception {
		driver.quit();
	}
}