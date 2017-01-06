package com.xdja.collectdata.thread;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import com.android.ddmlib.AndroidDebugBridge;
import com.android.ddmlib.AndroidDebugBridge.IClientChangeListener;
import com.android.ddmlib.Client;
import com.android.ddmlib.ClientData;
import com.xdja.collectdata.SaveEnvironmentManager;
import com.xdja.log.LoggerManager;

/**
 *  抓取内存快照的线程
 * @author zlw
 *
 */
public class DumpMemoryThread extends Thread implements IClientChangeListener{
	private final static String LOGTAG = DumpMemoryThread.class.getSimpleName();
	private Client mClient;
	private String type;
	private CountDownLatch memoryCount;
	
	public DumpMemoryThread(Client mClient, String type) {
		super();
		this.mClient = mClient;
		this.type = type;
		setName("DumpMemoryThread---" + type + "---");
	}
	
	@Override
	public void run() {
		// TODO Auto-generated method stub
		memoryCount = new CountDownLatch(1);
		AndroidDebugBridge.addClientChangeListener(this);
		mClient.dumpHprof();

		try {
			memoryCount.await(1, TimeUnit.MINUTES);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		AndroidDebugBridge.removeClientChangeListener(this);
	}

	@Override
	public void clientChanged(Client client, int changeMask) {
		// TODO Auto-generated method stub
		if (client == mClient) {
			switch (changeMask) {
			case Client.CHANGE_HPROF:
				final ClientData.HprofData data = client.getClientData().getHprofData();
				if (data != null) {
					switch (data.type) {
					case FILE:
						// TODO: older devices don't stream back the heap data.
						// Instead they save results on the sdcard.
						// We don't support this yet.
						LoggerManager.logError(LOGTAG, "clientChanged", "dump heap memory File");
						break;
					case DATA:
						//保存hprof
						SaveEnvironmentManager.getInstance().writeHprofToLocal(data.data, type);
						break;
					}
				} else {
					LoggerManager.logError(LOGTAG, "clientChanged", "dump heap memory failure");
				}
				
				if (memoryCount != null) {
					memoryCount.countDown();
				}
				break;
			case Client.CHANGE_METHOD_PROFILING_STATUS:
				break;
			default:
				break;
			}
		}
	}
}
