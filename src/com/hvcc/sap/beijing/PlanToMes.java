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
 * SAP에서 MES로 주간생산계획 데이터를 보내기
 * 
 * @author Shortstop
 */
public class PlanToMes {
	
	private static final Logger LOGGER = Logger.getLogger(PlanToMes.class);
	public static final String INSERT_SQL = "INSERT INTO INF_SAP_PLAN(IFSEQ, WERKS, ARBPL, EQUNR, MATNR, KUNNR, VERID, DISPD, ZSHIFTSEQ1, ZSHIFT1, ZSHIFTSEQ2, ZSHIFT2, ZSHIFTSEQ3, ZSHIFT3, CRUSR, CRDAT, CRTIM, EDUSR, EDDAT, EDTIM, IFRESULT, IFFMSG, MES_STAT, MES_UPDDT) VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, SYSDATE)";
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
			
			// AENAM : MES_USER ,ERZET : 15:14:08 ,KUNNR : 0000010189 ,IFMSG : COMMUNICATION_FAILURE ,IFRESULT : S ,ERDAT : 2014-02-05 ,WERKS : GT10 ,ERNAM : M0010-20 ,EQUNR :  ,AEZET : 17:14:10 ,ZSEQ1 : 1.000 ,IFSEQ : 0000000003 ,ZSEQ2 : 2.000 ,ZSEQ3 : 0 ,AEDAT : 2014-02-06 ,ZSHIFT2 : 490.000 ,ZSHIFT3 : 0 ,ARBPL : 6ATLA ,DISPD : 2014-02-06 ,ZSHIFT1 : 490.000 ,MEINS :  ,MATNR : F124ATBAA05 ,CHARG :  ,
			parameter.add(record.get("IFSEQ"));
			parameter.add(record.get("WERKS"));
			parameter.add(record.get("ARBPL"));
			parameter.add((record.get("EQUNR") == null || record.get("EQUNR").toString().equals("")) ? "EMPTY" : record.get("EQUNR"));
			parameter.add(record.get("MATNR"));
			parameter.add(record.get("KUNNR"));
			parameter.add((record.get("VERID") == null || record.get("VERID").toString().equals("")) ? "1111" : record.get("VERID"));
			parameter.add(record.get("DISPD"));
			parameter.add(record.get("ZSEQ1"));
			parameter.add(record.get("ZSHIFT1"));
			parameter.add(record.get("ZSEQ2"));
			parameter.add(record.get("ZSHIFT2"));
			parameter.add(record.get("ZSEQ3"));
			parameter.add(record.get("ZSHIFT3"));
			parameter.add(record.get("ERNAM"));
			parameter.add(record.get("ERDAT"));
			parameter.add(record.get("ERZET"));
			parameter.add(record.get("AENAM"));
			parameter.add(record.get("AEDAT"));
			parameter.add(record.get("AEZET"));
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
		new PlanToMes().execute();
	}
}
