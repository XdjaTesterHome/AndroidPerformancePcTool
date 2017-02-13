package com.xdja.collectdata.handleData.entity;

/**
 *  帧率处理结果
 * @author zlw
 *
 */
public class FpsHandleResult extends HandleDataResultBase {
	public String methodTracePath;
	public String memoryHprofPath;
	public int dropCount;
	public int frameCount;
	
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

	@Override
	public boolean equals(Object obj) {
		// TODO Auto-generated method stub
		return this.activityName.equals(((FpsHandleResult)obj).activityName);
	}

	public String getMemoryHprofPath() {
		return memoryHprofPath;
	}

	public String getMethodTracePath() {
		return methodTracePath;
	}


	@Override
	public int hashCode() {
		// TODO Auto-generated method stub
		if (activityName != null) {
			return activityName.hashCode();
		}
		return super.hashCode();
	}

	public void setMemoryHprofPath(String memoryHprofPath) {
		this.memoryHprofPath = memoryHprofPath;
	}
	
	public void setMethodTracePath(String methodTracePath) {
		this.methodTracePath = methodTracePath;
	}

	public int getDropCount() {
		return dropCount;
	}

	public void setDropCount(int dropCount) {
		this.dropCount = dropCount;
	}

	public int getFrameCount() {
		return frameCount;
	}

	public void setFrameCount(int frameCount) {
		this.frameCount = frameCount;
	}
	
	@Override
	public String[] formatDataToArray() {
		// TODO Auto-generated method stub
		String resulutStr = result ? "true" : "false";
		String[] dataArray = new String[]{activityName, testValue, String.valueOf(dropCount), String.valueOf(frameCount),logPath, methodTracePath, resulutStr};
		return dataArray;
	}
}
