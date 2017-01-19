package com.xdja.collectdata.handleData.entity;

public class CpuHandleResult extends HandleDataResultBase {
	
	// ×¥È¡method trace µÄpath
	public String methodTracePath;
	
	public CpuHandleResult(boolean result){
		super(result);
	}
	
	public String getMethodTracePath() {
		return methodTracePath;
	}

	public void setMethodTracePath(String methodTracePath) {
		this.methodTracePath = methodTracePath;
	}
	
}
