package com.xdja.controllers;

import java.util.List;

import org.apache.log4j.Logger;

import com.android.ddmlib.IDevice;
import com.xdja.android.AdbHelper;
import com.xdja.constant.Constants;

public class BatteryController implements InterfaceController{

	private Logger logger = Logger.getLogger(BatteryController.class);
	private boolean isStarted = false;
	private IDevice dev;
	private List<String> results;
	private String cmd;
	private boolean usbPowered = false;
	
	@Override
	public double getInfo(String packageName) {
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
		cmd = String.format("dumpsys battery");
		logger.info("start to execute cmd: " + cmd);
		results = AdbHelper.getInstance().executeShellCommandWithOutput(dev, cmd);
		if (results.size() == 0) {
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
	
	public void setUsbPowered(boolean usbPowered) {
		this.usbPowered = usbPowered;
		int powered = this.usbPowered ? 1 : 0;
		cmd = String.format("dumpsys battery set usb %d", powered);
		AdbHelper.getInstance().executeShellCommandWithOutput(dev, cmd);
		logger.info("execute cmd: " + cmd + " to set usb powered state!");
	}

}
