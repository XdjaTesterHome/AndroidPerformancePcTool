package com.xdja.adb;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.android.ddmlib.AndroidDebugBridge;
import com.android.ddmlib.IDevice;
import com.android.ddmlib.AndroidDebugBridge.IDeviceChangeListener;
import com.android.ddmlib.IDevice.DeviceState;
import com.sun.corba.se.impl.orbutil.closure.Constant;
import com.xdja.constant.Constants;

/***
 * 用于管理连接上pc的device以及device对应的process 底层是和AndroidDeviceBridge进行交互
 * 
 * @author zlw
 *
 */
public class DeviceManager {
	private Logger logger = Logger.getLogger(DeviceManager.class);
	private final static int TRY_COUNT = 20;
	
	// volatile 会禁止指令重排序优化。即取值的操作必须在赋值的操作完成之后才会
	private volatile static DeviceManager mInstance;
	private DeviceStateListener mDeviceStateChange;
	private ArrayList<AndroidDevice> devices = new ArrayList<AndroidDevice>();

	private DeviceManager() {
		addDeviceChangeListener();
	}

	/**
	 * 获取DeviceManager的一个单例
	 * 
	 * @return
	 */
	public static DeviceManager getInstance() {
		if (mInstance == null) {
			synchronized (DeviceManager.class) {
				if (mInstance == null) {
					mInstance = new DeviceManager();
				}
			}

		}
		return mInstance;
	}

	/***
	 * 用于监听Device 状态变化
	 * 
	 * @author zlw
	 *
	 */
	public interface DeviceStateListener {
		public void deviceDisconnected(AndroidDevice ad);

		public void deviceConnected(AndroidDevice idevice);
	}

	/**
	 * 用于外部调用者设置设备的监听
	 * 
	 * @param listener
	 */
	public void setDeviceStateListener(DeviceStateListener listener) {
		mDeviceStateChange = listener;
	}

	/**
	 * 返回当前的设备列表
	 * 
	 * @return
	 */
	public ArrayList<AndroidDevice> getAndroidDevice() {
		return devices;
	}

	public void addAndroidDevice(AndroidDevice device) {
		if (!devices.contains(device)) {
			devices.add(device);
		}
	}

	/**
	 * 向AndroidDeviceBridge中设置设备状况变化的监听
	 */
	private void addDeviceChangeListener() {
		AndroidDebugBridge.addDeviceChangeListener(new IDeviceChangeListener() {

			@Override
			public void deviceDisconnected(IDevice idevice) {
				for (AndroidDevice dev : devices) {
					if (dev.getDevice().getSerialNumber().equals(idevice.getSerialNumber())) {
						logger.info(String.format("device %s disconnected!", idevice.getSerialNumber()));
						mDeviceStateChange.deviceConnected(dev);
						break;
					}
				}
			}

			@Override
			public void deviceConnected(IDevice idevice) {
				if (idevice.isOnline()) {
					logger.info(String.format("device %s connected!", idevice.getSerialNumber()));
					AndroidDevice device = new AndroidDevice(idevice);
					mDeviceStateChange.deviceDisconnected(device);
				}
			}

			@Override
			public void deviceChanged(IDevice idevice, int i) {
				if (i != IDevice.CHANGE_STATE) {
					return;
				}
				DeviceState state = idevice.getState();
				AndroidDevice device = new AndroidDevice(idevice);
				if (state == DeviceState.ONLINE) {
					logger.info(String.format("device %s changed state: ONLINE", idevice.getSerialNumber()));
					mDeviceStateChange.deviceConnected(device);
				} else if (state == DeviceState.OFFLINE) {
					logger.info(String.format("device %s changed state: OFFLINE", idevice.getSerialNumber()));
					mDeviceStateChange.deviceDisconnected(device);
				}
			}
		});
	}

	/***
	 * 用于返回设备的标识
	 * @param device 设备封装
	 * @return 设备名_设备号
	 */
	public String getDeviceName(AndroidDevice device) {
		String serialnum  = device.getDevice().getSerialNumber();
		String deviceName = device.getDevice().getName(); 
		if (serialnum == null || deviceName == null) {
			return "unknow";
		}
		String comboDevicesName = deviceName + "_" + serialnum;
		return comboDevicesName;
	}
	
	/**
	 * 主动去获取连接到pc的设备
	 * 在launchView第一次打开时去获取
	 * @return
	 */
	public IDevice[] getDeviceListDirect() {
		AndroidDebugBridge.init(false);
		AndroidDebugBridge bridge = AndroidDebugBridge.createBridge(Constants.ADB_PATH, false);
		//不断尝试获取设备
		int count = 0;
		while(!bridge.hasInitialDeviceList()){
			try {
				Thread.sleep(1000);
				count++;
			} catch (Exception e) {
				// TODO: handle exception
			}
			
			if (count > TRY_COUNT) {
				break;
			}
		}
		
		IDevice devices[] = bridge.getDevices();
		return devices;
	}
}
