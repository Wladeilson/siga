package br.gov.jfrj.siga.page.objects;

import java.util.List;
import java.util.Properties;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

public class SolicitacaoEletronicaContratacaoPage extends EditaDocumentoPage {

	@FindBy(name = "progrAnual_chk")
	private List<WebElement> programacaoAnual; 
	
	public SolicitacaoEletronicaContratacaoPage(WebDriver driver) {
		super(driver);
	}
	
	public void criaSolicitacaoEletronicaContratacao(Properties propDocumentos) {
		preencheOrigem(propDocumentos.getProperty("internoProduzido"));
		selectTipoDocumento("Solicita��o Eletr�nica de Contrata��o", 599);
		preencheDocumentoInterno(propDocumentos, Boolean.TRUE, Boolean.FALSE);
		programacaoAnual.get(0).click();
		botaoOk.click();
	}
}
