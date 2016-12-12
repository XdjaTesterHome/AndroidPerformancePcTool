package com.xdja.controllers;

import java.util.List;

import org.apache.log4j.Logger;

import com.android.ddmlib.IDevice;
import com.xdja.android.AdbHelper;
import com.xdja.constant.Constants;

public class LaunchController implements InterfaceController{

	private Logger logger = Logger.getLogger(MemoryController.class);
	private boolean isStarted = false;
	private IDevice dev;
	private List<String> results;
	private String cmd;
	
	@Override
	public double getInfo(String packageName) {
		double ret = 0;
		//before launch, first kill the activity that is already launched
		forceStop(packageName.split(Constants.SEPERATOR)[0]);
		if(startCaught(packageName)) {
			for(String result : results) {
				if(result.startsWith(Constants.TOTAL_TIME)) {
					ret = Double.parseDouble(result.substring(Constants.TOTAL_TIME.length()));
				}
			}
			isStarted = true;
		} else {
			logger.info("execute cmd: "+ cmd + " failed!");
			isStarted = false;
		}
		forceStop(packageName.split(Constants.SEPERATOR)[0]);
		return ret;
	}

	@Override
	public boolean startCaught(String packageName) {
		cmd = String.format("am start -W -n %s", packageName);
		logger.info("start to execute cmd: "+ cmd);
		results = AdbHelper.getInstance().executeShellCommandWithOutput(dev, cmd);
		if(results.size() == 0) {
			return false;
		}
		return true;
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
	
	private boolean forceStop(String packageName) {
		cmd = String.format("am force-stop %s", packageName);
		logger.info("start to execute cmd: "+ cmd);
		AdbHelper.getInstance().executeShellCommandWithOutput(dev, cmd);
		return true;
	}

}
