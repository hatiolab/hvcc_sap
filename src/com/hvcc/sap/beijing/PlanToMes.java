/**
 * 
 */
package com.hvcc.sap.beijing;

import org.jboss.logging.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.hvcc.sap.MesUpdater;
import com.hvcc.sap.RfcSearcher;

/**
 * SAP에서 MES로 주간생산계획 데이터를 보내기
 * 
 * @author Shortstop
 */
public class PlanToMes {
	
	private static final Logger LOGGER = Logger.getLogger(ProductToMes.class);
	public static final String INSERT_SQL = "INSERT INTO INF_SAP_PLAN(IFSEQ, WERKS, ARBPL, EQUNR, MATNR, KUNNR, VERID, DISPD, ZSHIFTSEQ1, ZSHIFT1, ZSHIFTSEQ2, ZSHIFT2, ZSHIFTSEQ3, ZSHIFT3, CRDAT, CRTIM, EDUSR, EDDAT, IFRESULT, IFFMSG, MES_STAT, MES_UPDDT) VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, SYSDATE)";
	public static final String RFC_FUNC_NAME = "ZPPG_EA_PROD_PLANNING";
	public static final String RFC_OUT_TABLE = "ET_PLAN";
	
	/**
	 * call rfc 
	 * 
	 * @return
	 * @throws Exception
	 */
	public Map<String, Object> callRfc() throws Exception {
		Map<String, Object> inputParams = new HashMap<String, Object>();
		inputParams.put("IV_WERKS", "GT10");
		inputParams.put("IV_FDATE", "20140205");
		inputParams.put("IV_TDATE", "20140205");
		// 처음 요청일 경우 blank, 재전송 요청일 경우 'X'
		inputParams.put("IV_CHECK", "");

		List<String> outputParams = new ArrayList<String>();
		outputParams.add("EV_IFRESULT");
		outputParams.add("EV_IFFMSG");
		
		LOGGER.info("RFC [" + RFC_FUNC_NAME + "] Call!");
		Map<String, Object> output = new RfcSearcher().callFunction(RFC_FUNC_NAME, inputParams, outputParams, RFC_OUT_TABLE);
		return output;
	}
	
	/**
	 * update to mes (JDBC)
	 * 
	 * @param results
	 * @throws Exception
	 */
	public int updateToMes(String ifResult, String fmsg, List<Map<String, Object>> results) throws Exception {
		List<List<Object>> parameters = new ArrayList<List<Object>>();
		
		for(int i = 0 ; i < results.size() ; i++) {
			Map<String, Object> record = (Map<String, Object>)results.get(i);
			List<Object> parameter = new ArrayList<Object>();
			parameter.add(record.get("IFSEQ"));
			parameter.add(record.get("WERKS"));
			parameter.add(record.get("ARBPL"));
			parameter.add(record.get("EQUNR"));
			parameter.add(record.get("MATNR"));
			parameter.add(record.get("KUNNR"));
			parameter.add(record.get("VERID"));
			parameter.add(record.get("DISPD"));
			parameter.add(record.get("ZSHIFTSEQ1"));
			parameter.add(record.get("ZSHIFT1"));
			parameter.add(record.get("ZSHIFTSEQ2"));
			parameter.add(record.get("ZSHIFT2"));
			parameter.add(record.get("ZSHIFTSEQ3"));
			parameter.add(record.get("ZSHIFT3"));
			parameter.add(record.get("CRDAT"));
			parameter.add(record.get("CRTIM"));
			parameter.add(record.get("EDUSR"));
			parameter.add(record.get("EDDAT"));
			parameter.add("S");
			parameter.add(fmsg);
			parameter.add("N");
			parameters.add(parameter);
		}

		return new MesUpdater().update(INSERT_SQL, parameters);
	}
	
	/**
	 * 실행 
	 * 
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	public void execute() {
		Map<String, Object> output = null;
		int resultCount = 0;

		try {
			output = this.callRfc();
			
			if("S" == output.get("IFRESULT")) {
				List<Map<String, Object>> results = (List<Map<String, Object>>)output.get(RFC_OUT_TABLE);
				resultCount = this.updateToMes((String)output.get("IFRESULT"), (String)output.get("IFFMSG"), results);
				info("Got (" + resultCount + ") Plans From SAP!");
			} else {
				info("Failed to get Plans From SAP!");
			}			
		} catch (Exception e) {
			System.out.println("Failed to get Plans From SAP!");
			LOGGER.error(e);
		}	
	}
	
	private void info(String msg) {
		LOGGER.info(msg);
		System.out.println(msg);
	}
}
