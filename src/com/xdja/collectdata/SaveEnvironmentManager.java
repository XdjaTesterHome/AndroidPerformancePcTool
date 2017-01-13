package com.xdja.collectdata;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.xdja.adb.AdbManager;
import com.xdja.adb.AndroidSdk;
import com.xdja.collectdata.thread.SaveLogThread;
import com.xdja.constant.Constants;
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
	 * 
	 * @param pathType 生成哪种数据的path
	 * @param testtype 进行的哪种测试
	 * @param regx  保存文件的后缀
	 * @return
	 */
	public String getSuggestedPath(String pathType,  String testtype, String regx){
		String seggestName = getSuggestedName(testtype);
		if (CommonUtil.strIsNull(pathType)) {
			pathType = "commonFile";
		}
		
		// 默认用这个.zip暂时
		if (CommonUtil.strIsNull(regx)) {
			regx = ".zip";
		}
		
		// 判断path是否存在，不存在要new
		File file = new File(pathType);
		if (!file.exists()) {
			file.mkdirs();
		}
		
		String filePath = pathType + File.separator + seggestName + regx;
		
		return filePath;
	}
	
	/**
	 * 抓取hprof
	 * @param data
	 *            抓取的hprof数据
	 * @param type
	 *            测试类型
	 */
	public String writeHprofToLocal(byte[] data, String filePath) {
		if (data == null) {
			return "";
		}

		CommonUtil.writeDataToLocal(data, filePath);
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
	public String writeTraceToLocal(byte[] data, String filePath) {
		if (data == null) {
			return "";
		}

		CommonUtil.writeDataToLocal(data, filePath);
		
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
	 * @return 文件路径  不过有可能出现错误之后，文件保存是null的
	 */
	public String saveCurrentLog(String deviceName, String packageName, String type){
		String filePath = getSuggestedPath(Constants.ANDROID_LOG, type, ".log");
		mSaveLogThread = new SaveLogThread(deviceName, packageName, type);
		mSaveLogThread.start();
		
		return filePath;
	}
	
	
	
	/**
	 * 截图
	 * @param deviceName  当前设备的名称
	 * @param testType
	 * @return 保存文件的路径
	 */
	public String screenShots(String deviceName, String testType){
		String filePath = getSuggestedPath(Constants.SCREEN_SHOTS, testType, ".png");
		AdbManager.getInstance().screenCapture(deviceName, filePath, false);
		
		return filePath;
	}
	
	/**
	 *  dump memory
	 * @param deviceName
	 * @param packageName
	 * @param type
	 */
	public String dumpMemory(String deviceName, String packageName,  String type){
		String filePath = getSuggestedPath(Constants.MEMORY_DUMP, type, ".hprof");
		AdbManager.getInstance().dumpMemory(deviceName, packageName, filePath);
		
		return filePath;
	}
	
	/**
	 *  抓取当前的方法栈
	 * @param deviceName
	 * @param packageName
	 * @param type
	 */
	public String methodTracing(String deviceName, String packageName, String type){
		String filePath = getSuggestedPath(Constants.METHOD_TRACE, type, ".trace");
		AdbManager.getInstance().memthodTracing(deviceName, packageName, filePath);
		
		return filePath;
	}
}
