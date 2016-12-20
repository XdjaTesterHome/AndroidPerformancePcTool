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
		Logger logger = Logger.getLogger(LoggerManager.class);//创建logger对象
		System.out.println(!LoggerManager.isInited());//返回false或者true
		if(!LoggerManager.isInited()) {
			LoggerManager.initLogger();
		}
		logger.info("info test");
		logger.debug("debug test");
		logger.error("error test");
		System.out.println("你好");
	}
	
	public static void logInfo(String logTag, String func, String info) {
		Logger logger = Logger.getLogger(LoggerManager.class);
		if(!LoggerManager.isInited()) {
			LoggerManager.initLogger();
		}
		
		logger.info(logTag + "==" + func + "=="+ info);
	}
	
	public static void logError(String logTag, String func, String error) {
		Logger logger = Logger.getLogger(LoggerManager.class);
		if(!LoggerManager.isInited()) {
			LoggerManager.initLogger();
		}
		
		logger.error(logTag + "==" + func + "=="+ error);
	}
	
	public static void logDebug(String logTag, String func, String debug) {
		Logger logger = Logger.getLogger(LoggerManager.class);
		if(!LoggerManager.isInited()) {
			LoggerManager.initLogger();
		}
		
		logger.debug(logTag + "==" + func + "=="+ debug);
	}
}
