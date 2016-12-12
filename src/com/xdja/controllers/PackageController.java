package com.xdja.controllers;

import java.util.List;
import org.apache.log4j.Logger;
import com.android.ddmlib.IDevice;
import com.xdja.android.AdbHelper;
import com.xdja.constant.Constants;

public class PackageController {

	private Logger logger = Logger.getLogger(PackageController.class);
	private boolean isStarted = false;
	private IDevice dev;
	private List<String> results;
	private String cmd;
	
	public List<String> getInfo() {
		if(startCaught()) {
			for(int index = 0; index < results.size(); index++) {
				if (!results.get(index).isEmpty()) {
					String packageName = results.get(index).substring(Constants.PACKAGE.length());
					results.set(index, packageName);
				} else {
					results.remove(index);
				}
			}
			isStarted = true;
		} else {
			logger.info("execute cmd: "+ cmd + " failed!");
			isStarted = false;
		}
		return results;
	}
	
	public boolean startCaught() {
		cmd = "pm list packages";
		logger.info("start to execute cmd: "+ cmd);
		results = AdbHelper.getInstance().executeShellCommandWithOutput(dev, cmd);
		if(results.size() == 0) {
			return false;
		}
		return true;
	}

	public boolean stopCaught() {
		if(isStarted) {
			//TODO
			logger.info("MemoryController start to stop cmd: "+ cmd);
		}
		return true;
	}

	public void setDevice(IDevice device) {
		this.dev = device;
	}

}
