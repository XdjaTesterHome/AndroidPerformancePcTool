package com.xdja.collectdata.entity;

/**
 *  基本的测试信息
 * @author zlw
 *
 */
public class BaseTestInfo {
	// 包的名称
	public String packageName;
	
	// 测试包的版本号
	public String versionName;

	public String getPackageName() {
		return packageName;
	}

	public void setPackageName(String packageName) {
		this.packageName = packageName;
	}

	public String getVersionName() {
		return versionName;
	}

	public void setVersionName(String versionName) {
		this.versionName = versionName;
	}

	public BaseTestInfo(String packageName, String versionName) {
		super();
		this.packageName = packageName;
		this.versionName = versionName;
	}
	
	
}
