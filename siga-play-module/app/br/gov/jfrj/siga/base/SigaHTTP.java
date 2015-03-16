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
package br.gov.jfrj.siga.base;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.io.IOUtils;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.fluent.Form;
import org.apache.http.client.fluent.Request;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

/**
 * 
 * @author Rodrigo Ramalho
 * 	       hodrigohamalho@gmail.com
 *
 * Essa classe dever� ser usada para requests entre m�dulos do SIGA. Se fizer um GET direto
 * o request ser� processado como feito por um usu�rio an�nimo e retornar� um form de autentica��o.
 */
public class SigaHTTP {
	
	private static final Logger log = Logger.getLogger(SigaHTTP.class.getName());
	private Header[] headers;
	private final String COOKIE = "cookie";
	private final String SAMLRequest = "SAMLRequest";
	private final String SAMLResponse = "SAMLResponse";
	private final String JSESSIONID_PREFIX = "JSESSIONID=";
	private final String SET_COOKIE = "set-cookie";
	private final String HTTP_POST_BINDING_REQUEST = "HTTP Post Binding (Request)";
	private final String doubleQuotes = "\"";
	private static final int MAX_RETRY = 2; 
	private int retryCount = 0;
	private String idp;

	/**
	 * @param URL pode ser a url completa ou relativa.
	 * @param request (se for modulo play, setar pra null)
	 * @param cookieValue (necessario apenas nos modulos play)
	 */
	public String get(String URL, HttpServletRequest request, String cookieValue) {
		if (URL.startsWith("/"))
			URL = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort() + URL;
		
		return handleAuthentication(URL, request, cookieValue);
	}


	private String handleAuthentication(String URL, HttpServletRequest request, String cookieValue) {
		String html = "";
		try{
			 // Efetua o request para o Service Provider (m�dulo)
			 Request req = Request.Get(URL);
		//	 req.addHeader(COOKIE, JSESSIONID_PREFIX+getCookie(request, cookieValue));
			 // Atribui o html retornado e pega o header do Response
			 // Se a aplica��o j� efetuou a autentica��o entre o m�dulo da URL o conte�do ser� trago nesse primeiro GET
			 // Caso contr�rio passar� pelo processo de autentica��o (if abaixo)
			 html = req.execute().handleResponse(new ResponseHandler<String>() {
				@Override            
				public String handleResponse(HttpResponse httpResponse) throws ClientProtocolException, IOException {
					// O atributo que importa nesse header � o set-cookie que ser� utilizado posteriormente
					headers = httpResponse.getAllHeaders();
					return IOUtils.toString(httpResponse.getEntity().getContent(), "UTF-8");
				}
			});

			// Verifica se retornou o form de autentica��o do picketlink 
			if (html.contains(HTTP_POST_BINDING_REQUEST)){
				// Atribui o cookie recuperado no response anterior
				String setCookie = null;
				try{
					setCookie = extractCookieFromHeader(getHeader(SET_COOKIE));
				}catch(ElementNotFoundException elnf){
					log.warning("Nao encontrou o set-cookie");
					setCookie = getCookie(request, cookieValue);
				}
				
				// Atribui o valor do SAMLRequest contido no html retornado no GET efetuado.
				String SAMLRequestValue = getAttributeValueFromHtml(html, SAMLRequest);
				// Atribui a URL do IDP (sigaidp)
				String idpURL = getAttributeActionFromHtml(html);

				// Faz um novo POST para o IDP com o SAMLRequest como parametro e utilizando o sessionID do IDP
				html = Request.Post(idpURL).addHeader(COOKIE, JSESSIONID_PREFIX+doubleQuotes+getIdp(request)+doubleQuotes).bodyForm(Form.form().add(SAMLRequest, SAMLRequestValue).build()).execute().returnContent().toString();

				// Extrai o valor do SAMLResponse
				// Caso o SAMLResponse n�o esteja dispon�vel aqui, � porque o JSESSIONID utilizado n�o foi o do IDP.
				String SAMLResponseValue = getAttributeValueFromHtml(html, SAMLResponse);
				
				// Faz um POST para o SP com o atributo SAMLResponse utilizando o sessionid do primeiro GET
				// O retorno � discartado pois o resultado � um 302.
				Request.Post(URL).addHeader(COOKIE, JSESSIONID_PREFIX+doubleQuotes+setCookie+doubleQuotes).
						bodyForm(Form.form().add(SAMLResponse, SAMLResponseValue).build()).execute().discardContent();
				
				// Agora que estamos autenticado efetua o GET para p�gina desejada.
				html = Request.Get(URL).addHeader(COOKIE, JSESSIONID_PREFIX+doubleQuotes+setCookie+doubleQuotes).execute().returnContent().toString();
				if (html.contains(HTTP_POST_BINDING_REQUEST)){
					log.info("Alguma coisa falhou na autenticacao.");
					if (retryCount <= MAX_RETRY){
						log.info("tentando novamente o processo de autenticacao...");
						this.retryCount ++;
						handleAuthentication(URL, request, cookieValue);
					}
				}
			}
		}catch(Exception io){
			io.printStackTrace();
		}
		return html;
	}


	private String getCookie(HttpServletRequest request, String cookieValue) {
		if (cookieValue == null || cookieValue.isEmpty()){
			return request.getSession().getId();
		}
		return cookieValue;
	}


	private String extractCookieFromHeader(String headerValue) {
		return headerValue.substring(headerValue.indexOf("=")+1, headerValue.indexOf(";"));
	}

	private String getHeader(String headerName) throws ElementNotFoundException {

		for (Header header : headers) {
			if (header.getName().equalsIgnoreCase(headerName)){
				return header.getValue();
			}
		}

		throw new ElementNotFoundException("Header "+headerName+ " Not founded on headers variable");
	}


	private String getAttributeValueFromHtml(String htmlContent, String attribute){
		String value = "";

		Document doc = Jsoup.parse(htmlContent);
		// Get SAMLRequest value
		for (Element el : doc.select("input")){
			if (el.attr("name").equals(attribute)){
				value = el.attr("value");
			}
		}

		return value;
	}

	private String getAttributeActionFromHtml(String htmlContent){
		Document doc = Jsoup.parse(htmlContent);
		return doc.select("form").attr("action");
	}
	
	@SuppressWarnings("unchecked")
	public String getIdp(HttpServletRequest request) {
		try{
			if (idp == null || idp.isEmpty()){
				Map<String, Object> map = (Map<String, Object>) request.getSession().getAttribute("SESSION_ATTRIBUTE_MAP");
				String idpSessionID = (String) ((List<Object>) map.get("IDPsessionID")).get(0);
				if (idpSessionID != null){
					idp = idpSessionID;
				}
			}else{
				idp = "";
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		return idp;
	}

	public void setIdp(String idp) {
		this.idp = idp;
	}
	
	public String getNaWeb(String URL, HashMap<String, String> header, Integer timeout, String payload)
			throws AplicacaoException {

		try {

			HttpURLConnection conn = (HttpURLConnection) new URL(URL).openConnection();
			
			if (timeout != null) {
				conn.setConnectTimeout(timeout);
				conn.setReadTimeout(timeout);
			}
			
			//conn.setInstanceFollowRedirects(true);

			if (header != null) {
				for (String s : header.keySet()) {
						conn.setRequestProperty(s, header.get(s));
				}
			}	

			System.setProperty("http.keepAlive", "false");
			
			if (payload != null) {
				byte ab[] = payload.getBytes("UTF-8");
				conn.setRequestMethod("POST");
				// Send post request
				conn.setDoOutput(true);
				OutputStream os = conn.getOutputStream();
				os.write(ab);
				os.flush();
				os.close();
			}

			//StringWriter writer = new StringWriter();
			//IOUtils.copy(conn.getInputStream(), writer, "UTF-8");
			//return writer.toString();
			return IOUtils.toString(conn.getInputStream(), "UTF-8");
			
		} catch (IOException ioe) {
			throw new AplicacaoException("N�o foi poss�vel abrir conex�o", 1, ioe);
		}

	}
}
