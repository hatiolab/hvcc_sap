package com.hvcc.sap;

import java.util.logging.Logger;

public class Main {

	private static final Logger LOGGER = Logger.getLogger(Main.class.getName());
	
	public static void main(String[] args) {
		LOGGER.info("Starting SAP (Production) Interface Standalone program (released 2014-05-12)");
		
		TaskThread taskThread = new TaskThread();
		taskThread.setDaemon(true);
		taskThread.start();
		try {
			taskThread.join();
		} catch (InterruptedException e) {
			LOGGER.severe(e.getMessage());
		}		
		
		LOGGER.info("Started ...");
	}
}
