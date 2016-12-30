package com.xdja.adb;

import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.util.Locale;

import com.android.ddmlib.Client;
import com.android.ddmlib.IDevice;
import com.android.ddmlib.logcat.LogCatListener;

public class AndroidDevice implements IAndroidDevice, Comparable<AndroidDevice>{
	private IDevice device;
	
	public AndroidDevice(IDevice device) {
		super();
		this.device = device;
	}

	@Override
	public IDevice getDevice() {
		// TODO Auto-generated method stub
		return device;
	}

	@Override
	public String getSerialNumber() {
		// TODO Auto-generated method stub
		if (device != null) {
			String serialNum = device.getSerialNumber();
			return serialNum;
		}
		return null;
	}

	@Override
	public Locale getLocale() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		if (device != null) {
			return device.getName();
		}
		return null;
	}

	@Override
	public Dimension getScreenSize() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void tap(int x, int y) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void swipe(int x1, int y1, int x2, int y2) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void inputKeyevent(int value) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public BufferedImage takeScreenshot() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void takeScreenshot(String fileUrl) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean isDeviceReady() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isScreenOn() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public String currentActivity() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void invokeActivity(String activity) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String getDump() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean handlePopBox(String deviceBrand) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void unlock() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean isInstalled(String appBasePackage) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void uninstall(String appBasePackage) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void forwardPort(int local, int remote) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void removeForwardPort(int local) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void clearUserData(String appBasePackage) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void kill(String appBasePackage) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String runAdbCommand(String parameter) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getExternalStoragePath() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getCrashLog() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isWifiOff() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void restartADB() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void addLogCatListener(LogCatListener logCatListener) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void removeLogCatListener(LogCatListener logCatListener) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Client[] getAllClient() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Client getClientByAppName(String appName) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((getSerialNumber() == null) ? 0 : getSerialNumber().hashCode());
		return result;
	}
	
	@Override
	public boolean equals(Object obj) {
		// TODO Auto-generated method stub
		return getSerialNumber().equals(((AndroidDevice)obj).getSerialNumber());
	}

	@Override
	public int compareTo(AndroidDevice o) {
		// TODO Auto-generated method stub
		return this.getSerialNumber().compareTo(o.getSerialNumber());
	}
}
