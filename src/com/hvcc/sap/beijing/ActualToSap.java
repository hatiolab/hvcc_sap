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

import com.hvcc.sap.MesSearcher;
import com.hvcc.sap.RfcSearcher;

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
	public Map<String, Object> callRfc(List<Map<String, Object>> actuals) throws Exception {
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
		Map<String, Object> output = new RfcSearcher().callFunction(RFC_FUNC_NAME, inputParams, outputParams, null);
		return output;
	}
	
	/**
	 * select from MES Actual Table
	 * 
	 * @throws Exception
	 */
	public List<Map<String, Object>> selectActuals() throws Exception {
		String sql = "SELECT WORKCENTER_ID, OPERATION_ID, MACHINE_ID, CUSTOMER_ID, PRODUCT_ID, SUM(ACTUAL_QTY) ACTUAL_QTY FROM PROD_ORDERS WHERE ORDER_DATE = DATE'2013-07-14' GROUP BY WORKCENTER_ID, OPERATION_ID, MACHINE_ID, CUSTOMER_ID, PRODUCT_ID"; 
		return new MesSearcher().search(sql);
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
			List<Map<String, Object>> list = this.selectActuals();
				
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
		new ActualToSap().execute();
	}
}
