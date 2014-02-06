/**
 * 
 */
package com.hvcc.sap.beijing;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.jboss.logging.Logger;

import com.hvcc.sap.MesSearcher;
import com.hvcc.sap.RfcInvoker;

/**
 * Actual MES To SAP
 *  
 * @author Shortstop
 */
public class ActualToSap {
	
	private static final Logger LOGGER = Logger.getLogger(ActualToSap.class);
	public static final String RFC_FUNC_NAME = "ZPPG_EA_ACT_PROD";
	
	/**
	 * call rfc 
	 * 
	 * @return
	 * @throws Exception
	 */
	public Map<String, Object> callRfc(Map<String, Object> inputParams) throws Exception {
		List<String> outputParams = new ArrayList<String>();
		outputParams.add("ES_RESULT");
		outputParams.add("EV_IFSEQ");
		
		LOGGER.info("RFC [" + RFC_FUNC_NAME + "] Call!");
		Map<String, Object> output = new RfcInvoker().callFunction(RFC_FUNC_NAME, "IS_ACT", inputParams, outputParams);
		return output;
	}
	
	/**
	 * select from MES Actual Table
	 * 
	 * @throws Exception
	 */
	public List<Map<String, Object>> selectActuals() throws Exception {
		String sql = "SELECT '001' AS IFSEQ, 'GT10' AS WERKS, '6ATLA' AS ARBPL, '1' AS LOGRP, 'A1' AS VAART, 'F124ATBAA05' AS MATNR, '20140205' AS BUDAT, '20140205' AS PDDAT, 100 AS ERFMG FROM DUAL"; 
		return new MesSearcher().search(sql);
	}
	
	/**
	 * 실행 
	 * 
	 * @throws Exception
	 */
	public void execute() {
		Map<String, Object> output = null;

		try {
			List<Map<String, Object>> list = this.selectActuals();
			Map<String, Object> inputParam = list.get(0);
			this.showMap(inputParam);
			output = this.callRfc(inputParam);
			this.info(output.get("EV_IFSEQ").toString());
			
		} catch (Exception e) {
			System.out.println("Failed to Actual Interface");
			e.printStackTrace();
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
		new ActualToSap().execute();
	}
}
