package com.xdja.collectdata.handleData.entity;

public class CpuHandleResult extends HandleDataResultBase {
	
	// ×¥È¡method trace µÄpath
	public String methodTracePath;
	
	public CpuHandleResult(){
		super();
	}
	
	public CpuHandleResult(boolean result){
		super(result);
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
		String[] dataArray = new String[]{activityName, testValue, logPath, methodTracePath, resulutStr};
		return dataArray;
	}

	@Override
	public boolean equals(Object obj) {
		return super.equals(obj);
	}
}
