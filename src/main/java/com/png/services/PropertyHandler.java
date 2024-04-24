/**
 * 
 */
package com.png.services;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

import org.apache.log4j.Logger;

/**
 * @author vinay.sethi
 *
 */
public class PropertyHandler {
	private static PropertyHandler mInstance = null;
	private static Properties mProperties;
	private String mFile;
	private static final Logger logger = Logger.getLogger(PropertyHandler.class);

	static {
		if (mInstance == null) {
			mInstance = new PropertyHandler();
		}
		PropertyHandler.loadDefaultConfigurations();
	}

	public PropertyHandler() {
		this.mFile = File.separator + "Itrial_OBD" + File.separator + "properties" + File.separator + "genericapplication" + File.separator + "optout_application.properties";
	}

	public static PropertyHandler getInstance() {
		return mInstance;
	}

	private static void loadDefaultConfigurations() {
		mProperties = new Properties();
		try (FileInputStream inFile = new FileInputStream(mInstance.getFileName())){			
			mProperties.load(inFile);
			inFile.close();
		} catch (IOException ex) {
			logger.error("Unable to Load Property File -->" + ex);
		}
	}

	public String getValue(String pKey) {
		return mProperties.getProperty(pKey);
	}

	public void setValue(String pKey, String pValue) {
		mProperties.setProperty(pKey, pValue);
		try {
			FileOutputStream outFile = new FileOutputStream(mFile);
			mProperties.store(outFile, "----Opt Out Application Properties ---- ");
			outFile.close();
		} catch (Exception ex) {
			logger.error("Unable to Set Value -->" + ex);
			System.exit(0);
		}
	}

	public String getFileName() {
		return mFile;
	}

	public static void reload() {
		logger.info("Property File Reloaded for Generic Misscall Application Properties at "+Utility.getCurrentDatetime());
		loadDefaultConfigurations();
	}
	
	public static void main(String[] args) {
		reload();
	}
}