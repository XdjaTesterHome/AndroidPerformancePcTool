package com.xdja.adb;

import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.util.Locale;

import com.android.ddmlib.Client;
import com.android.ddmlib.IDevice;
import com.android.ddmlib.logcat.LogCatListener;

/**
 *  封装AndroidDevice相关的数据
 * @author zlw
 *
 */
public interface IAndroidDevice {
	  /**
	   * test only
	   */
	  IDevice getDevice();

	  String getSerialNumber();

	  Locale getLocale();

	  String getName();

	  Dimension getScreenSize();

	  void tap(int x, int y);

	  void swipe(int x1, int y1, int x2, int y2);

	  /**
	   * @see KeyEvent
	   */
	  void inputKeyevent(int value);

	  BufferedImage takeScreenshot();

	  void takeScreenshot(String fileUrl);

	  boolean isDeviceReady();

	  boolean isScreenOn();

	  String currentActivity();

	  void invokeActivity(String activity);

	  /**
	   * dump current activity view xml
	   */
	  String getDump();

	  boolean handlePopBox(String deviceBrand);

	  /**
	   * io.appium.unlock/.Unlock
	   */
	  void unlock();

	  boolean isInstalled(String appBasePackage);


	  void uninstall(String appBasePackage);

	  void forwardPort(int local, int remote);

	  void removeForwardPort(int local);

	  void clearUserData(String appBasePackage);

	  void kill(String appBasePackage);

	  String runAdbCommand(String parameter);

	  String getExternalStoragePath();

	  String getCrashLog();

	  boolean isWifiOff();

	  void restartADB();

	  /**
	   *
	   * @param logCatListener
	   */
	  void addLogCatListener(LogCatListener logCatListener);

	  void removeLogCatListener(LogCatListener logCatListener);

	  /**
	   * return all Dalvik/ART VM processes
	   */
	  Client[] getAllClient();

	  /**
	   *
	   * @param appName
	   * @return
	   */
	  Client getClientByAppName(String appName);
}
