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
	
	// 默认的性能标准
	public static final int MEMORY_MAX = 40;
	// 10s内抖动4次
	public static final int MEMORY_SHAKE_COUNT = 4;
	// cpu最大值(默认不超过40%)
	public static final int CPU_MAX = 40;
	// cpu静默默认值
	public static final int SILENT_CPU_MAX = 0;
	// kpi默认最大值（ms）
	public static final int MAX_KPI_DATA = 2000;
	// fps默认最低值（fps）
	public static final int MIN_FPS_DATA = 40;
	// 流量默认最大值(KB)
	public static final int MAX_FLOW_DATA = 1024;
	public static final int SILENT_FLOW_DATA = 0;
	
	
//	/**
//	 *  获取当前测试的包名
//	 * @return
//	 */
	public static String getTestPackageName(){
		String packageName = ProPertiesUtil.getInstance().getProperties(Constants.CHOOSE_PACKAGE);
		return packageName;
	}
}
