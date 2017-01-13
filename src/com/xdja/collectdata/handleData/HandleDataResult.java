package com.xdja.collectdata.handleData;

/**
 *  处理数据的结果
 * @author zlw
 *
 */
public class HandleDataResult{
	// 性能数据是否符合要求 true 时其他元素不填充值，  false时  填充值
	public boolean result;
	// 出现问题时的页面
	public String activityName;
	
	// 保存截图的路径
	public String screenshotsPath;
	// 保存日志的路径
	public String logPath;
	// 保存dump memory trace的路径
	public String memoryTracePath;
	// traceMethod Path
	public String methodTracePath;
	
	
	public HandleDataResult(boolean result) {
		super();
		this.result = result;
		// TODO Auto-generated constructor stub
	}
	
	public HandleDataResult(boolean result, String activityName, String screenshotsPath, String logPath,
			String methodTrace) {
		super();
		this.result = result;
		this.activityName = activityName;
		this.screenshotsPath = screenshotsPath;
		this.logPath = logPath;
		this.methodTracePath = methodTrace;
		// TODO Auto-generated constructor stub
	}

	public String getActivityName() {
		return activityName;
	}

	public String getLogPath() {
		return logPath;
	}
	
	public String getMemoryTracePath() {
		return memoryTracePath;
	}
	
	public String getMethodTracePath() {
		return methodTracePath;
	}
	public String getScreenshotsPath() {
		return screenshotsPath;
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
	public void setMemoryTracePath(String memoryTracePath) {
		this.memoryTracePath = memoryTracePath;
	}
	public void setMethodTracePath(String methodTracePath) {
		this.methodTracePath = methodTracePath;
	}
	public void setResult(boolean result) {
		this.result = result;
	}
	public void setScreenshotsPath(String screenshotsPath) {
		this.screenshotsPath = screenshotsPath;
	}
	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return "result = " + this.result + ", activityName = " + this.activityName + ", screenshotsPath = " + this.screenshotsPath + ", logPath =" + this.logPath;
		
	}
	
}
