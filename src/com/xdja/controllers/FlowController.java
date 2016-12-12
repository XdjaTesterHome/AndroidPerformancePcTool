package com.xdja.controllers;

import java.util.List;
import org.apache.log4j.Logger;
import com.android.ddmlib.IDevice;
import com.xdja.android.AdbHelper;
import com.xdja.constant.Constants;

public class FlowController implements InterfaceController{

	private Logger logger = Logger.getLogger(FlowController.class);
	private boolean isStarted = false;
	private IDevice dev;
	private List<String> results;
	private String cmd;
	
	@Override
	public double getInfo(String packageName) {
		double ret = 0;
		if(startCaught(packageName)) {
			String result = results.toString();
			//TODO
			result = result.substring(result.indexOf(Constants.FLOW_KEYWORD) + Constants.FLOW_KEYWORD.length() + 1);
			ret = Double.parseDouble(result.substring(0, result.indexOf(Constants.SPACE)).trim());
	        ret /= 1024;
			isStarted = true;
		} else {
			logger.info("execute cmd: "+ cmd + " failed!");
			isStarted = false;
		}
		return ret;
	}

	@Override
	public boolean startCaught(String packageName) {
		cmd = String.format("cat /proc/%s/net/dev", getPid(packageName));
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
	
	private String getPid(String packageName) {
		String pid = "";
		cmd = String.format("ps | grep %s", packageName);
		logger.info("start to execute cmd: "+ cmd);
		List<String> results = AdbHelper.getInstance().executeShellCommandWithOutput(dev, cmd);
		String result = results.toString();
		String[] stringArray = result.split(Constants.SPACE);
		for(int index = 1; index < stringArray.length; index++) {
			if (!stringArray[index].equals(Constants.BLANK)) {
				pid = stringArray[index];
				break;
			}
		}
		return pid;
	}

}
