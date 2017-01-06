package com.xdja.collectdata.thread;

import java.io.IOException;

import com.android.ddmlib.Client;
import com.android.ddmlib.ClientData;
import com.android.ddmlib.ClientData.IMethodProfilingHandler;
import com.xdja.collectdata.SaveEnvironmentManager;
import com.xdja.constant.Constants;
import com.xdja.log.LoggerManager;
import com.xdja.util.CommonUtil;

/**
 *  用于Method Trace的线程
 * @author zlw
 *
 */
public class TraceMethodThread extends Thread implements IMethodProfilingHandler{
	private final static String LOGTAG = TraceMethodThread.class.getSimpleName();
	private String mType; //测试类型
	private Client mCurClient;
	
	public TraceMethodThread(String mType, Client client) {
		super();
		this.mType = mType;
		mCurClient = client;
		setName("TraceMethodThread---" + mType + "---");
	}
	
	@Override
	public void run() {
		// TODO Auto-generated method stub
		try {
			if (CommonUtil.strIsNull(mType)) {
				mType = Constants.TYPE_KPI; //默认是kpi时间
			}
			
			ClientData.setMethodProfilingHandler(this);
			mCurClient.startMethodTracer();
			
			//默认采集10s的method数据
			Thread.sleep(10 * 1000);
			
			mCurClient.stopMethodTracer();
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void onSuccess(String remoteFilePath, Client client) {
		// TODO Auto-generated method stub
		LoggerManager.logError(LOGTAG, "onsuccess", "Method profiling: Older devices (API level < 10) are not supported yet. Please manually retrieve the file " +
	              remoteFilePath +
	              " from the device and open the file to view the results.");
	}

	@Override
	public void onSuccess(byte[] data, Client client) {
		// TODO Auto-generated method stub
		if (data != null) {
			System.out.println("I am ok ");
			SaveEnvironmentManager.getInstance().writeTraceToLocal(data, mType);
		}
	}

	@Override
	public void onStartFailure(Client client, String message) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onEndFailure(Client client, String message) {
		// TODO Auto-generated method stub
		
	}
	
	
	
}
