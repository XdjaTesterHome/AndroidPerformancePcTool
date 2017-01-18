package com.xdja.adb;

import java.util.TreeSet;

import com.android.ddmlib.AndroidDebugBridge;
import com.android.ddmlib.AndroidDebugBridge.IDebugBridgeChangeListener;
import com.android.ddmlib.Client;
import com.android.ddmlib.DdmPreferences;
import com.android.ddmlib.IDevice;
import com.xdja.collectdata.entity.CommandResult;
import com.xdja.collectdata.thread.DumpMemoryThread;
import com.xdja.collectdata.thread.ScreenCaptureThread;
import com.xdja.collectdata.thread.TraceMethodThread;
import com.xdja.constant.GlobalConfig;
import com.xdja.util.CommonUtil;
import com.xdja.util.ExecShellUtil;

/***
 * 用于Adb相关的操作，用到了ddmlib
 * 
 * @author zlw
 *
 */
public class AdbManager implements IDebugBridgeChangeListener {
	private final static String LOGTAG = AdbManager.class.getSimpleName();
	private static AdbManager mInstance;
	private TreeSet<AndroidDevice> devices = new TreeSet<>();
	AndroidDebugBridge myBridge = null;
	private DumpMemoryThread dumpMemoryThread = null;
	private TraceMethodThread traceMethodThread = null;
	private ScreenCaptureThread screenCaptureThread = null;

	public static AdbManager getInstance() {
		if (mInstance == null) {
			synchronized (AdbManager.class) {
				if (mInstance == null) {
					mInstance = new AdbManager();
				}
			}
		}

		return mInstance;
	}

	private AdbManager() {
	}

	/**
	 * 对Adb进行初始化
	 */
	public void init() {
		setDefaultSetting();
		createBridge();
		AndroidDebugBridge.addDebugBridgeChangeListener(this);
	}

	/**
	 * 创建AndroidBridge
	 */
	private void createBridge() {
		try {
			AndroidDebugBridge.init(true);
		} catch (IllegalStateException e) {
			e.printStackTrace();
		}

		myBridge = AndroidDebugBridge.getBridge();
		if (myBridge == null) {
			myBridge = AndroidDebugBridge.createBridge(AndroidSdk.adb().getAbsolutePath(), false);
		}
		long timeout = System.currentTimeMillis() + 60000;
		while (!myBridge.hasInitialDeviceList() && System.currentTimeMillis() < timeout) {
			try {
				Thread.sleep(50);
			} catch (InterruptedException e) {
				throw new RuntimeException(e);
			}
		}
	}

	/**
	 * 获取当前连接到手机的Devices
	 * 
	 * @return
	 */
	public TreeSet<AndroidDevice> getDevices() {
		if (myBridge != null) {
			IDevice[] origindevices = myBridge.getDevices();
			AndroidDevice device = null;
			if (devices.size() > 0) {
				devices.clear();
			}
			System.out.println("origindevices.length = " + origindevices.length);
			for (int i = 0; i < origindevices.length; i++) {
				device = new AndroidDevice(origindevices[i]);
				devices.add(device);
			}

		}
		return devices;
	}

	/***
	 * 通过设备名称来查找对应的IDevice
	 * 
	 * @param sn
	 * @return
	 */
	public IDevice getIDevice(String name) {

		if (CommonUtil.strIsNull(name)) {
			return null;
		}

		if (devices == null) {
			devices = getDevices();
		}

		for (IAndroidDevice device : devices) {
			if (name.equals(device.getName())) {
				return device.getDevice();
			}
		}
		return null;
	}

	/***
	 * 通过设备名称来获取设备序列号
	 * 
	 * @param name
	 * @return
	 */
	public String getSerialNumber(String name) {
		if (CommonUtil.strIsNull(name)) {
			return null;
		}

		if (devices == null) {
			devices = getDevices();
		}

		for (IAndroidDevice device : devices) {
			if (name.equals(device.getName())) {
				return device.getSerialNumber();
			}
		}
		return null;
	}

	/**
	 * 引起GC
	 * 
	 * @param name
	 *            设备的名称
	 */
	public void causeGC(String packageName) {
		Client client = getClient(GlobalConfig.DeviceName, packageName);
		if (client != null) {
			client.executeGarbageCollector();
		}
	}

	/**
	 * 获取内存分配的信息 public static final String HEAP_OBJECTS_ALLOCATED =
	 * "objectsAllocated"; //$NON-NLS-1$ public static final String
	 * HEAP_MAX_SIZE_BYTES = "maxSizeInBytes"; //$NON-NLS-1$ public static final
	 * String HEAP_BYTES_ALLOCATED = "bytesAllocated"; //$NON-NLS-1$ public
	 * static final String HEAP_SIZE_BYTES = "sizeInBytes"; //$NON-NLS-1$
	 * 
	 * @param name
	 * @param packageName
	 */
	public void getAllocInfo(String name, String packageName) {
		Client client = getClient(name, packageName);
		if (client != null) {
			client.updateHeapInfo();
		}
	}

	/**
	 * 获取对应应用在ddms中的一个Client实例
	 * 
	 * @param name
	 * @param packageName
	 * @return
	 */
	public Client getClient(String name, String packageName) {
		IDevice device = getIDevice(name);
		if (device != null) {
			Client client = device.getClient(packageName);
			return client;
		}
		return null;
	}

	/**
	 * 抓取内存快照
	 * 
	 * @param deviceName
	 * @param packageName
	 */
	public void dumpMemory(String deviceName, String packageName, String filePath) {
		Client client = getClient(deviceName, packageName);
		if (client != null) {
			dumpMemoryThread = new DumpMemoryThread(client, filePath);
			dumpMemoryThread.start();
		}
	}

	/**
	 * 进行截屏
	 * 
	 * @param deviceName
	 * @param type
	 *            测试的类型
	 * @param needSave
	 *            是否需要自己保存
	 */
	public ScreenCaptureThread screenCapture(String deviceName, String filePath, boolean needSave) {
		screenCaptureThread = new ScreenCaptureThread(deviceName, needSave, filePath); 
		screenCaptureThread.start();
		
		return screenCaptureThread;
	}

	/**
	 * 对Method进行trace
	 * 
	 * @param deviceName
	 * @param packageName
	 * @param filePath
	 *            测试类型，为了标记结果数据
	 */
	public void memthodTracing(String deviceName, String packageName, String filePath) {
		Client client = getClient(deviceName, packageName);
		if (client != null) {
			traceMethodThread = new TraceMethodThread(filePath, client);
			traceMethodThread.start();
		}
	}

	/**
	 * 将sdcard中的文件copy到本地
	 * 
	 * @param srcPath
	 * @param desPath
	 */
	public void copyFiles(String srcPath, String desPath) {
		if (CommonUtil.strIsNull(srcPath)) {
			return;
		}

		if (CommonUtil.strIsNull(desPath)) {
			return;
		}
		String cmd = "cp " + srcPath + " " + desPath;
		ExecShellUtil.getInstance().execShellCommand(cmd, false);
	}

	/**
	 * 设置默认的debug的port
	 * 
	 * @param port
	 */
	private void setDefaultSetting() {
		DdmPreferences.setDebugPortBase(GlobalConfig.BASEPORT);
		DdmPreferences.setSelectedDebugPort(GlobalConfig.DEBUGPORT);
		DdmPreferences.setUseAdbHost(true);
		DdmPreferences.setInitialThreadUpdate(true);
		DdmPreferences.setInitialHeapUpdate(true);
	}

	@Override
	public void bridgeChanged(AndroidDebugBridge bridge) {
		// TODO Auto-generated method stub
		myBridge = bridge;
	}

	/***
	 * 释放资源
	 */
	public void release() {
		AndroidDebugBridge.removeDebugBridgeChangeListener(this);
	}
	
	/**
	 *  让设备进入静默状态
	 */
	public void makeDeviceSlient(){
		String pressHomeKeyCmd = "input keyevent 3";
		String pressPowerKeyCmd = "input keyevent 26";
		ExecShellUtil.getInstance().execShellCommand(pressHomeKeyCmd, false);
		if (isScrrenOn()) {
			ExecShellUtil.getInstance().execShellCommand(pressPowerKeyCmd, false);
		}
	}
	
	/**
	 *  判断屏幕是否亮着
	 * @return
	 */
	public boolean isScrrenOn(){
		String screenOnCmd = "dumpsys window policy | grep mScreenOnFully";
		CommandResult cmdResult = ExecShellUtil.getInstance().execShellCommand(screenOnCmd, true);
		if (cmdResult != null && !CommonUtil.strIsNull(cmdResult.successMsg)) {
			String tempStr = CommonUtil.formatBlanksToBlank(cmdResult.successMsg).trim();
			String[] cmdResults = tempStr.split(" ");
			String value = cmdResults[0].split("=")[1];
			if ("false".equals(value.trim())) {
				return false;
			}
			
			if ("true".equals(value.trim())) {
				return true;
			}
		}
		
		
		return false;
	}
}
