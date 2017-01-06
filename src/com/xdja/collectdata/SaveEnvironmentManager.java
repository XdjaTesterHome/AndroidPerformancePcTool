package com.xdja.collectdata;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import com.xdja.adb.AndroidSdk;
import com.xdja.constant.Constants;
import com.xdja.constant.GlobalConfig;
import com.xdja.util.CommonUtil;

/***
 * 用于保存测试场景的工具类
 * 
 * @author zlw
 *
 */
public class SaveEnvironmentManager {
	private static SaveEnvironmentManager mInstance = null;
	private static Thread mSaveLogThread;
	private static LogRunnable mSaveLogRunnable;
	
	private SaveEnvironmentManager(){}
	
	public static SaveEnvironmentManager getInstance(){
		if (mInstance == null) {
			synchronized (SaveEnvironmentManager.class) {
				if (mInstance == null) {
					mInstance = new SaveEnvironmentManager();
				}
			}
		}
		
		return mInstance;
	}
	/**
	 * 保存文件时的名字
	 * 
	 * @param type
	 *            测试的类型
	 * @return
	 */
	public String getSuggestedName(String type) {
		String timestamp = new SimpleDateFormat("yyyy.MM.dd_HH.mm.ss").format(new Date());
		if (!CommonUtil.strIsNull(type)) {
			return type + "_" + timestamp;
		}
		return "default" + "_" + timestamp;
	}

	/**
	 * 抓取hprof
	 * @param data
	 *            抓取的hprof数据
	 * @param type
	 *            测试类型
	 */
	public String writeHprofToLocal(byte[] data, String type) {
		if (data == null) {
			return "";
		}

		String fileName = getSuggestedName(type);
		String filePath = Constants.MEMORY_DUMP + File.separator + fileName + ".hprof";
		CommonUtil.writeDataToLocal(data, Constants.MEMORY_DUMP, fileName + ".hprof");
		//转换hprof的格式
		covertHprof(filePath);
		
		return filePath;
	}
	
	
	/**
	 * 抓取hprof
	 * @param data
	 *            抓取的hprof数据
	 * @param type
	 *            测试类型
	 * @return 返回文件存放的路径
	 */
	public String writeTraceToLocal(byte[] data, String type) {
		if (data == null) {
			return "";
		}

		String fileName = getSuggestedName(type);
		String filePath = Constants.METHOD_TRACE + File.separator + fileName + ".trace";
		CommonUtil.writeDataToLocal(data, Constants.METHOD_TRACE, fileName + ".trace");
		
		return filePath;
	}
	
	
	/**
	 * 将hprofPath对应的hprof文件转换成MAT可识别的格式 
	 * 加-z命令是为了避免系统方法的干扰
	 * @param hprofPath
	 */
	private void covertHprof(String hprofPath){
		String cmdPath = AndroidSdk.hprofConv().getAbsolutePath();
		if (!CommonUtil.strIsNull(cmdPath)) {
			String cmd = cmdPath + " -z " + hprofPath + " " + hprofPath + ".temp";
			CommonUtil.execCmd(cmd, false);
		}
		
		File file = new File(hprofPath);
		File tempFile = new File(hprofPath + ".temp");
		if (file.exists()) {
			file.delete();
		}
		
		if (tempFile.exists()) {
			tempFile.renameTo(file);
		}
	}
	
	/**
	 * 保存当前的日志信息
	 * @param type 测试的类型
	 */
	public void saveCurrentLog(int pid, String type){
		
		if (mSaveLogThread != null && mSaveLogThread.isAlive()) {
			return;
		}
		mSaveLogRunnable = new LogRunnable(pid, Constants.ANDROID_LOG, type);
		mSaveLogThread = new Thread(mSaveLogRunnable);
		mSaveLogThread.start();
		startTimer();
	}
	/**
	 *  启动一个定时器检查是否停止进程
	 */
	public void startTimer(){
		Timer timer = new Timer();
		timer.schedule(new TimerTask() {
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
				// 销毁进程
				if (mSaveLogRunnable != null) {
					mSaveLogRunnable.destoryProcess();
				}
				
				// 停止线程
				if (mSaveLogThread != null) {
					if (mSaveLogThread.isAlive()) {
						System.out.println("mSaveLogThread isAlive");
						mSaveLogThread.interrupt();
						mSaveLogThread = null;
					}
				}
			}
		}, 30*1000);
	}
	
	public static void main(String[] args) {
		int pid = CollectDataImpl.getPid(GlobalConfig.PackageName);
		SaveEnvironmentManager.getInstance().saveCurrentLog(pid, Constants.TYPE_BATTERY);
	}
}
