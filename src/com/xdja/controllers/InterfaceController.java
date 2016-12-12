package com.xdja.controllers;

import com.android.ddmlib.IDevice;

public interface InterfaceController {

	/* *
	 * get the real info from the Controller
	 * */
	public double getInfo(String packageName);
	
	/* *
	 * Controller start catching the info that you are interested
	 * */
	public boolean startCaught(String packageName);
	
	/* *
	 * Controller stop catching the info that you are interested
	 * */
	public boolean stopCaught();
	
	/* *
	 * set the device to be controlled
	 * */
	public void setDevice(IDevice device);
}
