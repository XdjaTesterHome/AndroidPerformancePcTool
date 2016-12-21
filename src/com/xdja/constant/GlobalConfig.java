package com.xdja.constant;

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
	public static final int collectInterval  = collectDataCount / collectDataTime;

	public static final String PACKAGENAME = "com.xdja.HDSafeEMailClient";
	
}
