package com.xdja.collectdata.handleData.entity;

import com.xdja.constant.GlobalConfig;

/**
 * 处理数据的结果
 * 基础方法
 * 
 * @author zlw
 *
 */
public class HandleDataResultBase {
	// 测试的值
	public String testValue;

	// 性能数据是否符合要求 true 时其他元素不填充值， false时 填充值
	public boolean result;
	// 出现问题时的页面
	public String activityName;
	// 保存日志的路径
	public String logPath;
	// 包名
	public String packageName;
	// 是否显示错误信息
	public boolean isShowErrorMsg;
	
	public boolean isShowErrorMsg() {
		return isShowErrorMsg;
	}
	public void setShowErrorMsg(boolean isShowErrorMsg) {
		this.isShowErrorMsg = isShowErrorMsg;
	}
	public String getPackageName() {
		return packageName;
	}
	public void setPackageName(String packageName) {
		this.packageName = packageName;
	}
	public String getVersion() {
		return version;
	}
	public void setVersion(String version) {
		this.version = version;
	}
	// 版本号
	public String version;
	
	public HandleDataResultBase() {
		super();
		setPackageName(GlobalConfig.TestPackageName);
		setVersion(GlobalConfig.TestVersion);
		// TODO Auto-generated constructor stub
	}
	public HandleDataResultBase(boolean result){
		this.result = result;
		setPackageName(GlobalConfig.TestPackageName);
		setVersion(GlobalConfig.TestVersion);
	}
	public HandleDataResultBase(String testValue, boolean result, String activityName, String logPath) {
		super();
		this.testValue = testValue;
		this.result = result;
		this.activityName = activityName;
		this.logPath = logPath;
		setPackageName(GlobalConfig.TestPackageName);
		setVersion(GlobalConfig.TestVersion);
	}
	public String getActivityName() {
		return activityName;
	}
	public String getLogPath() {
		return logPath;
	}
	public String getTestValue() {
		return testValue;
	}
	public boolean isResult() {
		return result;
	}
	public void setActivityName(String activityName) {
		this.activityName = activityName;
	}
	public void setLogPath(String logPath) {
		this.logPath = logPath;
	}
	
	
	public void setResult(boolean result) {
		this.result = result;
	}
	
	public void setTestValue(String testValue) {
		this.testValue = testValue;
	}
	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return "result = " + result + ", testValue = " + testValue + ", activityName = " + activityName + ",logPath =" + logPath;
	}

	
}
