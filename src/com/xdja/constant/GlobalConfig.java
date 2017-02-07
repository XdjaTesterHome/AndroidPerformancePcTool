package com.xdja.constant;

import com.xdja.util.ProPertiesUtil;

/***
 *  全局的一些配置信息，和Constants区分开来
 * @author zlw
 *
 */
public class GlobalConfig {
	// 采集数据的次数
	public static final int collectDataCount = 20;
	
	// 采集数据的时长
	public static final int collectDataTime  = 20;
	
	// 采集数据的时间间隔
	public static final int collectInterval  = 1000;
	public static final int collectMIDDLEInterval  = 1000;
	public static final int collectLONGInterval  = 1500;
	
	public static final int DEBUGPORT = 1111;
	public static final int BASEPORT = 7500;
	
	// 测试的版本号和包名
	public static String TestVersion = "";
	public static String TestPackageName = "";
	
	// 选中的设备名称
	public static String DeviceName = "";
	
//	/**
//	 *  获取当前测试的包名
//	 * @return
//	 */
	public static String getTestPackageName(){
		String packageName = ProPertiesUtil.getInstance().getProperties(Constants.CHOOSE_PACKAGE);
		return packageName;
	}
}
