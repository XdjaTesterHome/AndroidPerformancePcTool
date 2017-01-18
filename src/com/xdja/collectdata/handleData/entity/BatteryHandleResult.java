package com.xdja.collectdata.handleData.entity;

/**
 *  电量处理结果
 * @author zlw
 *
 */
public class BatteryHandleResult extends HandleDataResultBase {
	
	public String uid;
	public String detailInfo;
	
	public BatteryHandleResult() {
		super();
		// TODO Auto-generated constructor stub
	}

	public BatteryHandleResult(boolean result) {
		super(result);
		// TODO Auto-generated constructor stub
	}

	public BatteryHandleResult(String testValue, boolean result, String activityName, String logPath) {
		super(testValue, result, activityName, logPath);
		// TODO Auto-generated constructor stub
	}

	public String getDetailInfo() {
		return detailInfo;
	}

	public String getUid() {
		return uid;
	}

	public void setDetailInfo(String detailInfo) {
		this.detailInfo = detailInfo;
	}

	public void setUid(String uid) {
		this.uid = uid;
	}
	
	
}
