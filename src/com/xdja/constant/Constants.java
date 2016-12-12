package com.xdja.constant;

import java.io.File;

public class Constants {
	// 工作空间根目录
	public static final String RESOURCES = "resources";
	public static final String LOCAL_PATH = System.getProperty("user.dir");
	public static final String LOG4J_PATH = Constants.LOCAL_PATH + File.separator + RESOURCES + "\\log4j.properties";
	
	// res目录
	public static final String RES_PATH = System.getProperty("user.dir") + File.separator + RESOURCES;
	public static final String ADB_PATH = LOCAL_PATH + File.separator + RESOURCES + "\\bin\\adb.exe";
	
	//log_xDevice
	public final static String COLON_BLANK = ": ";
	public final static String COMMA_BLANK = ", ";
	public final static String COLOM = ":";
	public final static String BLANK = "";
	public final static String SPACE = " ";
	public final static String KB = "kB:";
	public final static String BRACKET = "[";
	public final static String SEPERATOR = "/";
	
	//new
	public static final String AUTHOR = "xdja";
	public static final String PRODUCT_NAME = "Android性能测试工具";
	public static final String MONKEY = "monkey test";
	public static final String LAUNCH_COST = "launch cost";
	public static final String FPS_VIEW = "fps view";
	public static final String LAUNCH_COST_TEST = "launch cost test";
	public static final String START_TEST = "start test";
	public static final String STOP_TEST = "stop test";
	public static final String CONFIRM = "确定";
	public static final String CANCEL = "取消";
	public static final String TIME_UNIT = "时间(秒)";
	public static final String PACKAGE_NAME = "package name:";
	public static final String ACTIVITY_NAME = "activity name:";
	public static final String LOOPS = "loops:";
	public static final String SELECT_PACKAGE = "please select package name";
	public static final String PACKAGE_NAME_NULL = "package name can't be null!";
	public static final String DEVICE_NULL = "device is not selected!";
	public static final String PACKAGE = "package:";
	public static final String TOTAL_TIME = "TotalTime:";
	public static final String WAIT_TIME = "WaitTime:";
	public static final String ADJUSTING = "is_adjusting";
	
	public static final String MEMORY = "Memory";
	public static final String MEMORY_UNIT = "kb";
	
	public static final String FLOW = "Flow";
	public static final String FLOW_UNIT = "kb/s";
	public static final String FLOW_KEYWORD = "wlan0:";
	
	public static final String CPU = "Cpu";
	public static final String CPU_UNIT = "%";
	
	public static final String BATTERY = "Battery";
	public static final String BATTERY_UNIT = "%";
	public static final String USB_POWERED = "Usb Powered";
	public static final String LEVEL = "level:";
	public static final String SCALE = "scale:";
	
	public static final String FPS = "FPS";
	public static final String FPS_UNIT = "fps";
}
