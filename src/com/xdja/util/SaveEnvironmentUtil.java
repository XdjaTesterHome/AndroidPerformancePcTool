package com.xdja.util;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.xdja.adb.AndroidSdk;
import com.xdja.constant.Constants;

/***
 * 用于保存测试场景的工具类
 * 
 * @author zlw
 *
 */
public class SaveEnvironmentUtil {
	private static SaveEnvironmentUtil mInstance = null;
	
	private SaveEnvironmentUtil(){}
	
	public static SaveEnvironmentUtil getInstance(){
		if (mInstance == null) {
			synchronized (SaveEnvironmentUtil.class) {
				if (mInstance == null) {
					mInstance = new SaveEnvironmentUtil();
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
		return "unkonw" + "_" + timestamp;
	}

	/**
	 * 抓取hprof
	 * @param data
	 *            抓取的hprof数据
	 * @param type
	 *            测试类型
	 */
	public void writeHprofToLocal(byte[] data, String type) {
		if (data == null) {
			return;
		}

		String fileName = getSuggestedName(type);
		CommonUtil.writeDataToLocal(data, Constants.MEMORY_DUMP, fileName + ".hprof");
		//转换hprof的格式
		covertHprof(Constants.MEMORY_DUMP + File.separator + fileName + ".hprof");
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
	
	public static void main(String[] args) {
		SaveEnvironmentUtil.getInstance().covertHprof("memorydump/battery_2017.01.04_15.25.53.hprof");
	}
}
