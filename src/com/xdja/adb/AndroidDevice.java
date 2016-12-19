package com.xdja.adb;

import com.android.ddmlib.IDevice;

/**
 * 用于封装AndroidDevice相关的内容
 * @author zlw
 *
 */
public class AndroidDevice{
	private IDevice device;
	
	public AndroidDevice(IDevice device) {
		this.device = device;
	}
	
	public IDevice getDevice() {
		return this.device;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof AndroidDevice)) {
			return false;
		}
		// TODO Auto-generated method stub
		String tSerialNum = this.device.getSerialNumber();
		String oSerialNum = ((AndroidDevice) obj).getDevice().getSerialNumber();
		
		if (tSerialNum == null || oSerialNum == null || "".equals(tSerialNum) || "".equals(oSerialNum)) {
			return false;
		}
		
		return tSerialNum.equals(oSerialNum);
	}
	
	@Override
	public int hashCode() {
		// TODO Auto-generated method stub
		String tSerialNum = this.device.getSerialNumber();
		if (tSerialNum == null || "".equals(tSerialNum)) {
			return super.hashCode();
		}
		
		return tSerialNum.hashCode();
		
	}
}
