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
/*
 * Criado em  21/12/2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package br.gov.jfrj.siga.dp;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

@Entity
@Table(name = "DP_SUBSTITUICAO", schema = "CORPORATIVO")
@Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL)
@NamedQueries({
	@NamedQuery(name = "consultarSubstituicoesPermitidas", query = "from DpSubstituicao dps "+
			" where (dps.dtIniSubst < sysdate or dps.dtIniSubst = null) "+
			" and (dps.dtFimSubst > sysdate or dps.dtFimSubst = null) "+
			" and ((dps.substituto = null and dps.lotaSubstituto.idLotacao in (select lot.idLotacao from DpLotacao as lot where lot.idLotacaoIni = :idLotaSubstitutoIni)) or "+
			" dps.substituto.idPessoa in (select pes.idPessoa from DpPessoa as pes where pes.idPessoaIni = :idSubstitutoIni)) "+
			" and dps.dtFimRegistro = null"),
	@NamedQuery(name = "consultarOrdemData", query = "from DpSubstituicao as dps "+
			" where ((dps.titular = null and dps.lotaTitular.idLotacao in (select lot.idLotacao from DpLotacao as lot where lot.idLotacaoIni = :idLotaTitularIni)) or "+
			" dps.titular.idPessoa in (select pes.idPessoa from DpPessoa as pes where pes.idPessoaIni = :idTitularIni)) "+
			" and dps.dtFimRegistro = null "+
			" order by dps.dtIniSubst, dps.dtFimSubst ")
})
public class DpSubstituicao extends AbstractDpSubstituicao implements Serializable{

	/**
	 * 
	 */
	public DpSubstituicao() {
		super();
	}

	public String getDtFimSubstDDMMYY() {
		if (getDtFimSubst() != null) {
			final SimpleDateFormat df = new SimpleDateFormat(
					"dd/MM/yy");
			return df.format(getDtFimSubst());
		}
		return "";
	}

	public String getDtIniSubstDDMMYY() {
		if (getDtIniSubst() != null) {
			final SimpleDateFormat df = new SimpleDateFormat(
					"dd/MM/yy");
			return df.format(getDtIniSubst());
		}
		return "";
	}

	public boolean isEmVoga(){
		Calendar dtFim = Calendar.getInstance();
		Calendar dtIni = Calendar.getInstance();
		Calendar now = Calendar.getInstance();
		now.setTime(new Date());
		if (getDtIniSubst()!=null){
			dtIni.setTime(getDtIniSubst());
			if (now.before(dtIni))
				return false;
		}
		if (getDtFimSubst()!=null){
			dtFim.setTime(getDtFimSubst());
			if (now.after(dtFim))
				return false;
		}
		return true;
	}

	public boolean isFutura(){		
		Calendar dtIni = Calendar.getInstance();
		Calendar now = Calendar.getInstance();
		now.setTime(new Date());
		if (getDtIniSubst()!=null){
			dtIni.setTime(getDtIniSubst());
			if (now.before(dtIni))
				return true;
		}		
		return false;
	}

	public boolean isTerminada(){
		if (getDtFimSubst()==null)
			return false;
		Calendar dtFim = Calendar.getInstance();
		Calendar now = Calendar.getInstance();
		dtFim.setTime(getDtFimSubst());
		now.setTime(new Date());
		if (now.after(dtFim))
			return true;
		return false;
	}	

	public boolean isExcluida(){
		if (getDtFimRegistro()!=null)
			return true;
		return false;
	}	

}
