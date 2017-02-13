package com.xdja.collectdata.handleData.entity;

public class MemoryHandleResult extends HandleDataResultBase {
	
	// 抓取的内存快照的path
	public String memoryHprofPath;
	// 抓取的方法trace 的path
	public String methodTracePath;
	// 内存值是否超过最大值
	public boolean mIsErrorMemory;
	
	public boolean ismIsErrorMemory() {
		return mIsErrorMemory;
	}

	public void setmIsErrorMemory(boolean mIsErrorMemory) {
		this.mIsErrorMemory = mIsErrorMemory;
	}

	public MemoryHandleResult(){
		super();
	}
	
	public MemoryHandleResult(boolean result){
		super(result);
	}
	
	public String getMemoryHprofPath() {
		return memoryHprofPath;
	}
	public void setMemoryHprofPath(String memoryHprofPath) {
		this.memoryHprofPath = memoryHprofPath;
	}
	public String getMethodTracePath() {
		return methodTracePath;
	}
	public void setMethodTracePath(String methodTracePath) {
		this.methodTracePath = methodTracePath;
	}
	
	@Override
	public String[] formatDataToArray() {
		// TODO Auto-generated method stub
		String resulutStr = result ? "true" : "false";
		String[] dataArray = new String[]{activityName, testValue, logPath, memoryHprofPath, resulutStr};
		return dataArray;
	}
}
