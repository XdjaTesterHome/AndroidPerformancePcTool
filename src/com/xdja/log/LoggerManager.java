package com.xdja.log;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import com.xdja.constant.Constants;

public class LoggerManager {
	private static boolean isInited = false;
	
	public static void initLogger() {
		PropertyConfigurator.configure(Constants.LOG4J_PATH);
		isInited = true;
	}
	
	public static boolean isInited() {
		return isInited;
	}
	
	public static void main(String[] args) {
		Logger logger = Logger.getLogger(LoggerManager.class);
		if(!LoggerManager.isInited()) {
			LoggerManager.initLogger();
		}
		logger.info("info test");
		logger.debug("debug test");
		logger.error("error test");
	}

}
