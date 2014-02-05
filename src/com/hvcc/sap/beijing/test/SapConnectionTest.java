package com.hvcc.sap.beijing.test;

import com.hvcc.sap.SapConnectionPool;
import com.sap.mw.jco.JCO;

public class SapConnectionTest {

	public static void main(String[] args) {
		SapConnectionPool pool = null;
		JCO.Client client = null;
		try {
			pool = SapConnectionPool.getInstance();
			client = pool.getConnection();
			System.out.println("Get Client : " + client.toString());
			pool.releaseConnection(client);
			System.out.println("Released Client");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
