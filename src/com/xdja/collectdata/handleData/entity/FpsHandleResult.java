package com.xdja.collectdata.handleData.entity;

/**
 *  帧率处理结果
 * @author zlw
 *
 */
public class FpsHandleResult extends HandleDataResultBase {
	public String methodTracePath;
	public String memoryHprofPath;
	public int dropcount;
	
	public FpsHandleResult() {
		super();
		// TODO Auto-generated constructor stub
	}

	public FpsHandleResult(boolean result) {
		super(result);
		// TODO Auto-generated constructor stub
	}

	public FpsHandleResult(String testValue, boolean result, String activityName, String logPath) {
		super(testValue, result, activityName, logPath);
		// TODO Auto-generated constructor stub
	}

	public int getDropcount() {
		return dropcount;
	}

	public String getMemoryHprofPath() {
		return memoryHprofPath;
	}

	public String getMethodTracePath() {
		return methodTracePath;
	}

	public void setDropcount(int dropcount) {
		this.dropcount = dropcount;
	}

	public void setMemoryHprofPath(String memoryHprofPath) {
		this.memoryHprofPath = memoryHprofPath;
	}

	public void setMethodTracePath(String methodTracePath) {
		this.methodTracePath = methodTracePath;
	}
	
	@Override
	public int hashCode() {
		// TODO Auto-generated method stub
		if (activityName != null) {
			return activityName.hashCode();
		}
		return super.hashCode();
	}
	
	@Override
	public boolean equals(Object obj) {
		// TODO Auto-generated method stub
		return this.activityName.equals(((FpsHandleResult)obj).activityName);
	}
}
