/**
 * 
 */
package com.hvcc.sap.beijing;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.jboss.logging.Logger;

import com.hvcc.sap.MesUpdater;
import com.hvcc.sap.RfcSearcher;

/**
 * Production Parameter SAP TO MES
 * 
 * @author Shortstop
 */
public class ParameterToMes {
	
	private static final Logger LOGGER = Logger.getLogger(ParameterToMes.class);
	public static final String INSERT_SQL = "INSERT INTO INF_SAP_PARAMETER(IFSEQ, WERKS, ZPTYP, ZDEPT, ARBPL, ZMACN, MATNR, VERID, ZMKEY, ZUPH, LOTQT, VGW03, ERDAT, ERZET, ERNAM, AEDAT, AEZET, AENAM, IFRESULT, IFFMSG, MES_STAT, MES_UPDDT) VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, SYSDATE)";
	public static final String RFC_FUNC_NAME = "ZPPG_EA_PLAN_PARAM";
	public static final String RFC_OUT_TABLE = "ET_PARAM";
	
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
		outputParams.add("EV_IFMSG");
		
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
		int resultCnt = results.size();
		for(int i = 0 ; i < resultCnt ; i++) {
			Map<String, Object> record = (Map<String, Object>)results.get(i);
			this.showMap(record);
			List<Object> parameter = new ArrayList<Object>();
			parameter.add(record.get("IFSEQ"));
			parameter.add(record.get("WERKS"));
			parameter.add(record.get("ZPTYP"));
			parameter.add(record.get("ZDEPT"));
			parameter.add(record.get("ARBPL"));
			parameter.add(record.get("ZMACN"));
			parameter.add(record.get("MATNR"));
			parameter.add(record.get("VERID"));
			parameter.add(record.get("ZMKEY"));
			parameter.add(record.get("ZUPH "));
			parameter.add(record.get("LOTQT"));
			parameter.add(record.get("VGW03"));
			parameter.add(record.get("ERDAT"));
			parameter.add(record.get("ERZET"));
			parameter.add(record.get("ERNAM"));
			parameter.add(record.get("AEDAT"));
			parameter.add(record.get("AEZET"));
			parameter.add(record.get("AENAM"));
			parameter.add(record.get("IFRESULT"));
			parameter.add(record.get("IFMSG"));
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
			String ifresult = output.get("EV_IFRESULT").toString();
			if("S".equals(ifresult)) {
				List<Map<String, Object>> results = (List<Map<String, Object>>)output.get(RFC_OUT_TABLE);
				resultCount = this.updateToMes((String)output.get("EV_IFRESULT"), (String)output.get("EV_IFMSG"), results);
				info("Got (" + resultCount + ") Parameters From SAP!");
			} else {
				info("Failed to get Parameters From SAP!");
			}			
		} catch (Exception e) {
			System.out.println("Failed to get Parameters From SAP!");
			LOGGER.error(e);
		}	
	}
	
	private void info(String msg) {
		LOGGER.info(msg);
		System.out.println(msg);
	}
	
	@SuppressWarnings("rawtypes")
	private void showMap(Map map) {
		StringBuffer buf = new StringBuffer();
		Iterator iter = map.keySet().iterator();
		while(iter.hasNext()) {
			String key = (String)iter.next();
			String value = (map.get(key) == null ? "" : map.get(key).toString());
			buf.append(key);
			buf.append(" : ");
			buf.append(value);
			buf.append(", ");
		}
		this.info(buf.toString());
	}
	
	public static void main(String[] args) {
		new ParameterToMes().execute();
	}
}
