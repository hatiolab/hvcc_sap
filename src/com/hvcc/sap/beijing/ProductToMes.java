/**
 * 
 */
package com.hvcc.sap.beijing;

import org.jboss.logging.Logger;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.hvcc.sap.MesConnectionFactory;
import com.hvcc.sap.MesUpdater;
import com.hvcc.sap.RfcSearcher;

/**
 * ERP에서 MES 시스템으로 Material Master 정보를 전송하는 Interface
 * SAP RFC : ZRFC_PPG_Material To MES		
 * 
 * @author Shortstop
 */
public class ProductToMes {
	
	private static final Logger LOGGER = Logger.getLogger(ProductToMes.class);
	public static final String INSERT_SQL = "INSERT INTO INF_SAP_PRODUCT(WERKS,MATNR,MAKTX,MTART,MEINS,MATKL,BESKZ,SOBSL,MMSTA,BSTRF,DATUV,DATUB,CRUSR,CRDAT,CRTIM,EDUSR,EDDAT,EDTIM,IFRES,IFFMS,MES_STAT,MES_UPDDT) VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, SYSDATE)";
	public static final String RFC_FUNC_NAME = "ZRFC_PPG_Material To MES";
	public static final String RFC_OUT_TABLE = "ZPPG9001S";
	
	/**
	 * call rfc 
	 * 
	 * @return
	 * @throws Exception
	 */
	public Map<String, Object> callRfc() throws Exception {
		Map<String, Object> inputParams = new HashMap<String, Object>();
		List<String> outputParams = new ArrayList<String>();
		outputParams.add("IFRESULT");
		outputParams.add("IFFMSG");
		
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
			parameter.add(record.get("WERKS"));
			parameter.add(record.get("MATNR"));
			parameter.add(record.get("MAKTX"));
			parameter.add(record.get("MTART"));
			parameter.add(record.get("MEINS"));
			parameter.add(record.get("MATKL"));
			parameter.add(record.get("BESKZ"));
			parameter.add(record.get("SOBSL"));
			parameter.add(record.get("MMSTA"));
			parameter.add(record.get("BSTRF"));
			parameter.add(record.get("DATUV"));
			parameter.add(record.get("DATUB"));
			parameter.add(record.get("CRUSR"));
			parameter.add(record.get("CRDAT"));
			parameter.add(record.get("CRTIM"));
			parameter.add(record.get("EDUSR"));
			parameter.add(record.get("EDDAT"));
			parameter.add(record.get("EDTIM"));
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
				info("Got (" + resultCount + ") Products From SAP!");
			} else {
				info("Failed to get Products From SAP!");
			}			
		} catch (Exception e) {
			System.out.println("Failed to get Products From SAP!");
			LOGGER.error(e);
		}	
	}
	
	private void info(String msg) {
		LOGGER.info(msg);
		System.out.println(msg);
	}
	
	public void testConn() {
		Connection conn = null;
		try {
			conn = MesConnectionFactory.getInstance().getConnection();
			System.out.println("Connection success : " + conn);
			conn.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
