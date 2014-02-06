package com.hvcc.sap.beijing.test;

import com.hvcc.sap.SapConnectionPool;
import com.sap.mw.jco.IRepository;
import com.sap.mw.jco.JCO;

public class RfcTest {

	public void rfcTest() {
		String funcName = "ZCOG_RFC_COST_CENTER";
		String input1Name = "IV_BUKRS";
		String input2Name = "IV_LANGU";
		String output1Name = "E_MSGE";
		//String output2Name = "ET_OUT";

		SapConnectionPool	sapPool		= null;
		JCO.Client			mConnection	= null;
		IRepository			mRepository	= null;
		JCO.Function		function	= null; 

		try {
			sapPool = SapConnectionPool.getInstance();
			mConnection = sapPool.getConnection();
			mRepository = new JCO.Repository(" ", mConnection);
			function = sapPool.createFunction(mRepository, funcName);

			if (function == null) {
				String errMsg = "SapFunc.getNewSaleshop:" + funcName + " not found in SAP";		 
				System.out.println(errMsg);
				throw new Exception(errMsg);
			}

			JCO.ParameterList input = function.getImportParameterList();
			input.setValue("H100", input1Name);
			input.setValue("3", input2Name);

			mConnection.execute(function);
			
			// Print return message
			JCO.ParameterList output = function.getExportParameterList();
			JCO.Table outTable = function.getTableParameterList().getTable("ET_OUT");
			Object out1Obj = output.getString(output1Name);
			System.out.println("Output1 : " + out1Obj.toString());
			
			System.out.println("Table : ET_OUT ----------------------------------");
			//int etOutFieldCount = outTable.getFieldCount();
			//int etOutColumnCount = outTable.getNumColumns();
			int etOutRowCount = outTable.getNumRows();
			
			for(int i = 0 ; i < etOutRowCount ; i++) {
				outTable.setRow(i);
				System.out.println("Row : " + i);
				System.out.println("MANDT : " + outTable.getString("MANDT"));
				System.out.println("SPRAS : " + outTable.getString("SPRAS"));
				System.out.println("KOKRS : " + outTable.getString("KOKRS"));
				System.out.println("KOSTL : " + outTable.getString("KOSTL"));
				System.out.println("DATBI : " + outTable.getString("DATBI"));
				System.out.println("KTEXT : " + outTable.getString("KTEXT"));
				System.out.println("LTEXT : " + outTable.getString("LTEXT"));
				System.out.println("MCTXT : " + outTable.getString("MCTXT"));
			}
			System.out.println("-------------------------------------------------");

		} catch (Exception e) {
			e.printStackTrace();

		} finally {
			mRepository = null;
			if (mConnection != null) { 
				sapPool.releaseConnection(mConnection); 
			}	
		}
	}
	
	public static void main(String[] args) {
		new RfcTest().rfcTest();
	}

}
