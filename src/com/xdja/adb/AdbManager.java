package com.xdja.adb;

import java.util.Iterator;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeSet;

import com.android.ddmlib.AndroidDebugBridge;
import com.android.ddmlib.AndroidDebugBridge.IClientChangeListener;
import com.android.ddmlib.Client;
import com.android.ddmlib.ClientData;
import com.android.ddmlib.IDevice;
import com.github.cosysoft.device.android.AndroidDevice;
import com.github.cosysoft.device.android.impl.AndroidDeviceStore;
import com.xdja.util.CommonUtil;

/***
 * 用于Adb相关的操作，用到了一个开源库https://github.com/cosysoft/device
 * 
 * @author zlw
 *
 */
public class AdbManager {
	private static AdbManager mInstance;
	private TreeSet<AndroidDevice> devices = null;

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
		setEventListener();
	}

	/**
	 * 获取当前连接到手机的Devices
	 * 
	 * @return
	 */
	public TreeSet<AndroidDevice> getDevices() {
		devices = AndroidDeviceStore.getInstance().getDevices();
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
			devices = AndroidDeviceStore.getInstance().getDevices();
		}

		for (AndroidDevice device : devices) {
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
			devices = AndroidDeviceStore.getInstance().getDevices();
		}

		for (AndroidDevice device : devices) {
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
	public void causeGC(String name, String packageName) {
		Client client = getClient(name, packageName);
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
		client.enableAllocationTracker(true);
		new Timer().schedule(new TimerTask() {
			@Override
			public void run() {
				client.requestAllocationDetails();
				client.enableAllocationTracker(false);
			}
		}, 3000);
	}

	/**
	 * 获取对应应用在ddms中的一个Client实例
	 * 
	 * @param name
	 * @param packageName
	 * @return
	 */
	private Client getClient(String name, String packageName) {
		IDevice device = getIDevice(name);

		if (device != null) {
			Client client = device.getClient(packageName);
			client.executeGarbageCollector();
			return client;
		}
		return null;
	}

	/**
	 * 设置事件监听
	 */
	private void setEventListener() {
		AndroidDebugBridge.addClientChangeListener(new IClientChangeListener() {

			@Override
			public void clientChanged(Client client, int changeMask) {
				// TODO Auto-generated method stub
				System.out.println("changeMask = " + changeMask);
				if ((changeMask & Client.CHANGE_HEAP_ALLOCATIONS) != 0) {
					if (client.isHeapUpdateEnabled()) {
						ClientData clientData = client.getClientData();
						if (clientData != null) {
							Iterator<Integer> heapIds = clientData.getVmHeapIds();
							while (heapIds.hasNext()) {
								Integer integer = (Integer) heapIds.next();
								Map<String, Long> vmData = clientData.getVmHeapInfo(integer);
								if (vmData != null && vmData.size() > 0) {
									for (String key : vmData.keySet()) {
										System.out.println("key = " + key + ", value=" + vmData.get(key));
									}
								}
							}
						}
					}
				}
			}

		});
	}
}
