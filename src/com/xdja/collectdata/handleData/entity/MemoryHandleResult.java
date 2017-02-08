package com.xdja.collectdata.handleData.entity;

public class MemoryHandleResult extends HandleDataResultBase {
	
	// 抓取的内存快照的path
	public String memoryHprofPath;
	// 抓取的方法trace 的path
	public String methodTracePath;
	
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
	
}
