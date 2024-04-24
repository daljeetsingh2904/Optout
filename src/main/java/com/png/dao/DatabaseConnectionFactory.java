package com.png.dao;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.util.concurrent.*;
import javax.sql.DataSource;
import org.apache.log4j.Logger;

import java.util.Collections;


public class DatabaseConnectionFactory {

	private ExecutorService executor;
    private DataSource[] dataSources;
    private ConcurrentHashMap<String, Boolean> map;
    DbHandler dbhandler;
    private static final Logger logger = Logger.getLogger(DatabaseConnectionFactory.class);

    public DatabaseConnectionFactory(DataSource[] dataSources) {
        this.executor = Executors.newCachedThreadPool();
        this.dataSources = dataSources;
        this.map = new ConcurrentHashMap<>();
    }

    public boolean isMobileNumberPresent(String encryptedMobileNumber) throws InterruptedException {
      
    	// Check cache first if present or not 
        if (map.containsKey(encryptedMobileNumber)) {
            return map.get(encryptedMobileNumber);
        }
        dbhandler=new DbHandler();
        
        CountDownLatch latch = new CountDownLatch(dataSources.length);

        for (int i = 0; i < dataSources.length; i++) {
            int dbIndex = i; // Capture the loop variable for each database
            executor.submit(() -> {
                try (Connection connection = dataSources[dbIndex].getConnection()) {
                	DatabaseMetaData metaData = connection.getMetaData();
//                	logger.info("Connected to Database "+metaData.getURL().replaceAll(".*/([^/?]+).*", "$1"));
                    boolean present = dbhandler.updateOptDetails(connection, encryptedMobileNumber);
//                    logger.info("Updating mobile number: " + encryptedMobileNumber + " in database: " + metaData.getURL().replaceAll(".*/([^/?]+).*", "$1"));
                    map.put(encryptedMobileNumber, present); 
                } catch (Exception e) {
                   logger.info("Error checking mobile number in database " + dbIndex + ": " + e.getMessage());
                } finally {
                    latch.countDown(); 
                }
            });
        }

        latch.await();

        return map.getOrDefault(encryptedMobileNumber, false);
    }


    public void shutdown() {
        executor.shutdown();
    }

}