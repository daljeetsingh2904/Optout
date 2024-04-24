package com.png.controller;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.asteriskjava.fastagi.DefaultAgiServer;

import com.png.services.Constants;


public class AsteriskController extends Thread{

	private static final Logger logger = Logger.getLogger(AsteriskController.class);
	@Override
	public void run() {
		PropertyConfigurator.configure(Constants.LOG_PAH);
		DefaultAgiServer asteriskJavaServer = new DefaultAgiServer();
		try {
			logger.info("Asterisk server started at port"+asteriskJavaServer.getPort());
//			asteriskJavaServer.setPort(1234);
			asteriskJavaServer.startup();
			
		}catch (Exception e) {
			logger.info("Error starting Asterisk server" + e);
		}
	}
}
