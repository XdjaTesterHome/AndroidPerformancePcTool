package com.xdja.collectdata.entity;

/**
 *  电量相关数据
 * @author zlw
 *
 */
public class BatteryData {
	// uid或者数据类型
	public String uid;
	// uid对应的电量值
	public String batteryValue;
	// 关于电量的详细信息
	public String detailInfo;
	
	public String appPackageName; //uid对应的包名
	public BatteryData() {
		super();
		// TODO Auto-generated constructor stub
	}
	public BatteryData(String uid, String batteryValue, String detailInfo) {
		super();
		this.uid = uid;
		this.batteryValue = batteryValue;
		this.detailInfo = detailInfo;
	}
	
	public String getAppPackageName() {
		return appPackageName;
	}
	public String getBatteryValue() {
		return batteryValue;
	}
	public String getDetailInfo() {
		return detailInfo;
	}
	public String getUid() {
		return uid;
	}
	public void setAppPackageName(String appPackageName) {
		this.appPackageName = appPackageName;
	}
	public void setBatteryValue(String batteryValue) {
		this.batteryValue = batteryValue;
	}
	public void setDetailInfo(String detailInfo) {
		this.detailInfo = detailInfo;
	}
	public void setUid(String uid) {
		this.uid = uid;
	}
	
	
}
