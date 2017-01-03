package com.xdja.util;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.Properties;

/**
 * 用于读取配置文件的工具
 * 
 * @author zlw
 *
 */
public class ProPertiesUtil {

	private static ProPertiesUtil mInstance;
	private final static String NAME = "tools.properties";
	private Properties mProperties;
	

	private ProPertiesUtil() {
		mProperties = new Properties();
	}

	public static ProPertiesUtil getInstance() {
		if (mInstance == null) {
			synchronized (ProPertiesUtil.class) {
				if (mInstance == null) {
					mInstance = new ProPertiesUtil();
				}
			}
		}

		return mInstance;
	}

	public void writeProperties(String key, String value) {
		FileOutputStream oFile;
		try {
			oFile = new FileOutputStream(NAME);
			mProperties.setProperty(key, value);
			mProperties.store(oFile, "Update '" + key + "' value  " + value);
			oFile.flush();
			oFile.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} // true表示追加打开
		catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * 读取key对应的值
	 * 
	 * @param key
	 * @return
	 */
	public String getProperties(String key) {
		if (CommonUtil.strIsNull(key)) {
			return "";
		}
		try {
			InputStream in = new BufferedInputStream(new FileInputStream(NAME));
			mProperties.load(in); /// 加载属性列表
			Iterator<String> it = mProperties.stringPropertyNames().iterator();
			while (it.hasNext()) {
				String ikey = it.next();
				if (ikey != null && ikey.equals(key)) {
					return mProperties.getProperty(ikey);
				}
			}
			in.close();
		} catch (Exception e) {
			// TODO: handle exception
		}
		
		return "";
	}
}
