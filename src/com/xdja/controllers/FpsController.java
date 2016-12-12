package com.xdja.controllers;

import java.util.List;
import org.apache.log4j.Logger;
import com.android.ddmlib.IDevice;
import com.xdja.android.AdbHelper;
import com.xdja.constant.Constants;

public class FpsController implements InterfaceController {

	private Logger logger = Logger.getLogger(FpsController.class);
	private boolean isStarted = false;
	private IDevice dev;
	private List<String> results;
	private String cmd;
	
	@Override
	public double getInfo(String packageName) {
		//TODO
		double ret = 0;
		if(startCaught(packageName)) {
			String result = results.toString();
			//TODO
			result = result.replace(Constants.SPACE, Constants.BLANK);
			ret = Double.parseDouble(result.substring(result.indexOf(Constants.LEVEL) + Constants.LEVEL.length(), result.indexOf(Constants.SCALE) - 1));
			isStarted = true;
		} else {
			logger.info("execute cmd: "+ cmd + " failed!");
			isStarted = false;
		}
		return ret;
	}

	@Override
	public boolean startCaught(String packageName) {
		//TODO
		cmd = String.format("dumpsys battery");
		logger.info("start to execute cmd: " + cmd);
		results = AdbHelper.getInstance().executeShellCommandWithOutput(dev, cmd);
		if (results.size() == 0) {
			return false;
		}
		return true;
	}
	
	public String getActivityName() {
		String activityName = "";
		//TODO
		
		return activityName;
	}

	@Override
	public boolean stopCaught() {
		if(isStarted) {
			//TODO
			logger.info("MemoryController start to stop cmd: "+ cmd);
		}
		return true;
	}

	@Override
	public void setDevice(IDevice device) {
		this.dev = device;
	}

}
