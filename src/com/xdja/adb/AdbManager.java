package com.xdja.adb;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.TreeSet;

import javax.imageio.ImageIO;

import com.android.ddmlib.AdbCommandRejectedException;
import com.android.ddmlib.AndroidDebugBridge;
import com.android.ddmlib.AndroidDebugBridge.IDebugBridgeChangeListener;
import com.android.ddmlib.Client;
import com.android.ddmlib.DdmPreferences;
import com.android.ddmlib.IDevice;
import com.android.ddmlib.RawImage;
import com.android.ddmlib.TimeoutException;
import com.xdja.collectdata.SaveEnvironmentManager;
import com.xdja.collectdata.thread.DumpMemoryThread;
import com.xdja.collectdata.thread.TraceMethodThread;
import com.xdja.constant.Constants;
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
	public void dumpMemory(String deviceName, String packageName, final String type) {
		Client client = getClient(deviceName, packageName);
		if (client != null) {
			dumpMemoryThread = new DumpMemoryThread(client, type);
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
	public BufferedImage screenCapture(String deviceName, String type, boolean needSave) {
		IDevice device = getIDevice(deviceName);
		BufferedImage myImage = null;
		if (device != null) {
			try {
				RawImage rawImage = device.getScreenshot();
				if (rawImage != null) {
					myImage = new BufferedImage(rawImage.width, rawImage.height, BufferedImage.TYPE_INT_ARGB);
					for (int y = 0; y < rawImage.height; y++) {
						for (int x = 0; x < rawImage.width; x++) {
							int argb = rawImage.getARGB((x + y * rawImage.width) * (rawImage.bpp / 8));
							myImage.setRGB(x, y, argb);
						}
					}

					if (needSave) {
						return myImage;
					}

					String fileName = SaveEnvironmentManager.getInstance().getSuggestedName(type) + ".png";
					File file = new File(Constants.SCREEN_SHOTS);
					if (!file.exists()) {
						file.mkdirs();
					}

					ImageIO.write(myImage, "PNG", new File(Constants.SCREEN_SHOTS + File.separator + fileName));
				}
			} catch (TimeoutException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (AdbCommandRejectedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		return myImage;
	}

	/**
	 * 对Method进行trace
	 * 
	 * @param deviceName
	 * @param packageName
	 * @param type
	 *            测试类型，为了标记结果数据
	 */
	public void memthodTracing(String deviceName, String packageName, String type) {
		Client client = getClient(deviceName, packageName);
		if (client != null) {
			traceMethodThread = new TraceMethodThread(type, client);
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
}
