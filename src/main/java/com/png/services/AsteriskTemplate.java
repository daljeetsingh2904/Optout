package com.png.services;

import java.sql.Connection;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.asteriskjava.fastagi.AgiChannel;
import org.asteriskjava.fastagi.AgiException;
import org.asteriskjava.fastagi.AgiRequest;
import org.asteriskjava.fastagi.BaseAgiScript;
import javax.sql.DataSource;

import com.png.dao.DatabaseConnectionFactory;
import com.png.dao.DbHandler;

public class AsteriskTemplate extends BaseAgiScript {

	private static final Logger logger = Logger.getLogger(AsteriskTemplate.class);
	Connection conn = null;

	public void service(AgiRequest request, AgiChannel channel) throws AgiException {
		PropertyConfigurator.configure(Constants.LOG_PAH);
		DbHandler dbhandler;
		String encryptedMobileNumber = null;
		DatabaseConnectionFactory database;
		int callerIdValid = 0;
		try {
			dbhandler = new DbHandler();
			if (request.getArguments()[2].equals("connection")) {

				logger.info("Connection established!!");
			}
			String mobileNumber = request.getArguments()[0];
			String callerId = request.getArguments()[1];
			conn = dbhandler.getConnection();

			callerIdValid = dbhandler.fetchOptOutDid(conn, callerId);

			if (callerIdValid > 0) {

				DataSource[] dataSources = dbhandler.initializeDataSources();
				database = new DatabaseConnectionFactory(dataSources);

				if (mobileNumber.length() > 10) {
					mobileNumber = mobileNumber.substring(mobileNumber.length() - 10);
					encryptedMobileNumber = dbhandler.fetchMobileNumberUniqueId(conn, mobileNumber);
				}

				dbhandler.insertOptOut(conn, encryptedMobileNumber, callerId);

				boolean isPresent = database.isMobileNumberPresent(encryptedMobileNumber);
				database.shutdown();
			} else {
				logger.info(callerId + " is not optout DID");
			}

		} catch (Exception e) {
			logger.info("Error --->> " + e);
		}
	}
}
