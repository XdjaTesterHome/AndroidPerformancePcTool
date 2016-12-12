package com.xdja.controllers;

import java.util.List;

import org.apache.log4j.Logger;

import com.android.ddmlib.IDevice;
import com.xdja.android.AdbHelper;
import com.xdja.constant.Constants;

public class MemoryController implements InterfaceController{

	private Logger logger = Logger.getLogger(MemoryController.class);
	private boolean isStarted = false;
	private IDevice dev;
	private List<String> results;
	private String cmd;
	
	@Override
	public double getInfo(String packageName) {
		double ret = 0;
		if(startCaught(packageName)) {
			String result = results.toString();
			result = result.replace(Constants.SPACE, Constants.BLANK);
			ret = Double.parseDouble(result.substring(result.indexOf(Constants.BRACKET) + 1, result.indexOf(Constants.KB)));
			isStarted = true;
		} else {
			logger.info("execute cmd: "+ cmd + " failed!");
			isStarted = false;
		}
		return ret;
	}

	@Override
	public boolean startCaught(String packageName) {
		cmd = String.format("dumpsys meminfo | grep %s", packageName);
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

}
