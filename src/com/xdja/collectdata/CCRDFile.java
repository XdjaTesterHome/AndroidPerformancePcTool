package com.xdja.collectdata;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.xdja.collectdata.entity.BaseTestInfo;
import com.xdja.collectdata.entity.BatteryData;
import com.xdja.collectdata.entity.CommandResult;
import com.xdja.collectdata.handleData.HandleDataManager;
import com.xdja.collectdata.handleData.entity.BatteryHandleResult;
import com.xdja.database.PerformanceDB;
import com.xdja.util.CommonUtil;
import com.xdja.util.ExecShellUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;

//创建新文件和目录
public class CCRDFile {
	// 验证字符串是否为正确路径名的正则表达式
	private static String matches = "[A-Za-z]:\\\\[^:?\"><*]*";
	// 通过 sPath.matches(matches) 方法的返回值判断是否正确
	// sPath 为路径字符串
	boolean flag = false;
	private static BatteryData batteryData = null;
	private static String nowTestPackageName = "";
	
	public boolean DeleteFolder(String deletePath) {// 根据路径删除指定的目录或文件，无论存在与否
		flag = false;
		if (deletePath.matches(matches)) {
			File file = new File(deletePath);
			if (!file.exists()) {// 判断目录或文件是否存在
				return flag; // 不存在返回 false
			} else {

				if (file.isFile()) {// 判断是否为文件
					return deleteFile(deletePath);// 为文件时调用删除文件方法
				} else {
					return deleteDirectory(deletePath);// 为目录时调用删除目录方法
				}
			}
		} else {
			System.out.println("要传入正确路径！");
			return false;
		}
	}

	public boolean deleteFile(String filePath) {// 删除单个文件
		flag = false;
		File file = new File(filePath);
		if (file.isFile() && file.exists()) {// 路径为文件且不为空则进行删除
			file.delete();// 文件删除
			flag = true;
		}
		return flag;
	}

	public boolean deleteDirectory(String dirPath) {// 删除目录（文件夹）以及目录下的文件
		// 如果sPath不以文件分隔符结尾，自动添加文件分隔符
		if (!dirPath.endsWith(File.separator)) {
			dirPath = dirPath + File.separator;
		}
		File dirFile = new File(dirPath);
		// 如果dir对应的文件不存在，或者不是一个目录，则退出
		if (!dirFile.exists() || !dirFile.isDirectory()) {
			return false;
		}
		flag = true;
		File[] files = dirFile.listFiles();// 获得传入路径下的所有文件
		for (int i = 0; i < files.length; i++) {// 循环遍历删除文件夹下的所有文件(包括子目录)
			if (files[i].isFile()) {// 删除子文件
				flag = deleteFile(files[i].getAbsolutePath());
				System.out.println(files[i].getAbsolutePath() + " 删除成功");
				if (!flag)
					break;// 如果删除失败，则跳出
			} else {// 运用递归，删除子目录
				flag = deleteDirectory(files[i].getAbsolutePath());
				if (!flag)
					break;// 如果删除失败，则跳出
			}
		}
		if (!flag)
			return false;
		if (dirFile.delete()) {// 删除当前目录
			return true;
		} else {
			return false;
		}
	}

	// 创建单个文件
	public static boolean createFile(String filePath) {
		File file = new File(filePath);
		if (file.exists()) {// 判断文件是否存在
			System.out.println("目标文件已存在" + filePath);
			return false;
		}
		if (filePath.endsWith(File.separator)) {// 判断文件是否为目录
			System.out.println("目标文件不能为目录！");
			return false;
		}
		if (!file.getParentFile().exists()) {// 判断目标文件所在的目录是否存在
			// 如果目标文件所在的文件夹不存在，则创建父文件夹
			System.out.println("目标文件所在目录不存在，准备创建它！");
			if (!file.getParentFile().mkdirs()) {// 判断创建目录是否成功
				System.out.println("创建目标文件所在的目录失败！");
				return false;
			}
		}
		try {
			if (file.createNewFile()) {// 创建目标文件
				System.out.println("创建文件成功:" + filePath);
				return true;
			} else {
				System.out.println("创建文件失败！");
				return false;
			}
		} catch (IOException e) {// 捕获异常
			e.printStackTrace();
			System.out.println("创建文件失败！" + e.getMessage());
			return false;
		}
	}

	// 创建目录
	public static boolean createDir(String destDirName) {
		File dir = new File(destDirName);
		if (dir.exists()) {// 判断目录是否存在
			System.out.println("创建目录失败，目标目录已存在！");
			return false;
		}
		if (!destDirName.endsWith(File.separator)) {// 结尾是否以"/"结束
			destDirName = destDirName + File.separator;
		}
		if (dir.mkdirs()) {// 创建目标目录
			System.out.println("创建目录成功！" + destDirName);
			return true;
		} else {
			System.out.println("创建目录失败！");
			return false;
		}
	}

	// 创建临时文件
	public static String createTempFile(String prefix, String suffix, String dirName) {
		File tempFile = null;
		if (dirName == null) {// 目录如果为空
			try {
				tempFile = File.createTempFile(prefix, suffix);// 在默认文件夹下创建临时文件
				return tempFile.getCanonicalPath();// 返回临时文件的路径
			} catch (IOException e) {// 捕获异常
				e.printStackTrace();
				System.out.println("创建临时文件失败：" + e.getMessage());
				return null;
			}
		} else {
			// 指定目录存在
			File dir = new File(dirName);// 创建目录
			if (!dir.exists()) {
				// 如果目录不存在则创建目录
				if (CCRDFile.createDir(dirName)) {
					System.out.println("创建临时文件失败，不能创建临时文件所在的目录！");
					return null;
				}
			}
			try {
				tempFile = File.createTempFile(prefix, suffix, dir);// 在指定目录下创建临时文件
				return tempFile.getCanonicalPath();// 返回临时文件的路径
			} catch (IOException e) {// 捕获异常
				e.printStackTrace();
				System.out.println("创建临时文件失败!" + e.getMessage());
				return null;
			}
		}
	}

	// get_package_name_by_uid获取当前应用包名//
	public static String get_package_name_by_uid(String uid) {
		if (CommonUtil.strIsNull(uid)) {
			return "";
		}

		String packagename = "";
		if (uid.contains("u0")) {
			uid = "u0_" + uid.substring(2);
		}
		String cmd = "adb shell ps | grep " + uid;
		CommandResult data = ExecShellUtil.getInstance().execCmdCommand(cmd, false, true);
		String result = null;
		if (data.successMsg != null) {
			result = data.successMsg;
			String pattern = "com.*";
			Pattern r = Pattern.compile(pattern);
			Matcher m = r.matcher(result);
			if (m.find()) {
				packagename = m.group(0);
			} else {
				// System.out.println("NO MATCH do");
			}
			result = packagename;
		}
		return result;

	}

	// @@get_battery_data：获取电量数据@@//
	public static CommandResult get_battery_data(String pkg_name) {
		String cmd;
		if (pkg_name == null || pkg_name == "") {
			cmd = "dumpsys batterystats";
		} else {
			cmd = "dumpsys batterystats " + pkg_name;
		}
		CommandResult data = ExecShellUtil.getInstance().execShellCommand(cmd);
		if ((data != null)) {
			return data;
		} else {
			return null;
		}
	}

	// @@writetofile：写入cmd中输出的结果到demo文件中@@//
	public static void writetofile(String a) throws IOException {
		String oldName = System.getProperty("user.dir");
		String filepath = oldName + "/powerresult/";
		File file = new File(filepath);
		if (!file.exists()) {
			file.mkdir();// 创建文件夹
		}
		// CCRDFile.createDir(filepath);// 调用方法创建目录
		String filedemopath = oldName + "/powerresult/batteryData.txt";
		File filedemo = new File(filedemopath);
		if (!filedemo.exists()) {
			FileWriter fw = new FileWriter(filepath + "batteryData.txt", true);// 创建文件
			fw.write(a);
			fw.close();
		} else {
			new CCRDFile().deleteFile(filedemopath);
			FileWriter fw = new FileWriter(filepath + "batteryData.txt", true);// 创建文件
			fw.write(a);
			fw.close();
		}
	}

	// 正则表达式处理方法//
	private static String handlere(String powerdata) {
		String line = powerdata;
		String resu = null;
		String pattern = "\\(mAh\\):([\\s\\S]*)";
		// 创建 Pattern 对象
		Pattern r = Pattern.compile(pattern);
		// 现在创建 matcher 对象
		Matcher m = r.matcher(line);
		// System.out.println(m.find());
		if (m.find()) {
			resu = (String) m.group(1);
		} else {
			System.out.println("NO MATCH");
		}
		return resu;

	}

	/**
	 * 处理采集到的电量数据
	 * 
	 * @param message
	 * @return
	 */
	public static List<BatteryData> handlePowerData(String message) {
		List<BatteryData> batteryDatas = new ArrayList<>(12);
		if (CommonUtil.strIsNull(message)) {
			return batteryDatas;
		}

		// 匹配我们关心的数据
		String result = handlere(message);
		if (CommonUtil.strIsNull(result)) {
			return batteryDatas;
		}

		String[] batterys = result.split("\n\n");
		float computerDrain = 0;
		String actualDrain = "";
		String[] captureValue = batterys[0].trim().split("\n");
		String detailInfo = batterys[1].trim();
		for (int i = 0; i < captureValue.length; i++) {
			batteryData = new BatteryData();
			if (i == 0) {
				String[] totalBatterys = captureValue[0].split(",");
				computerDrain = Float.parseFloat(totalBatterys[1].split(":")[1].trim());
				actualDrain = totalBatterys[2].split(":")[1].trim();
				if (computerDrain > 0) {
					batteryData.setBatteryValue(String.valueOf(computerDrain));
					batteryData.setAppPackageName("Computed drain");
					batteryDatas.add(batteryData);
				}

				if (!"".equals(actualDrain)) {
					batteryData = new BatteryData();
					batteryData.setBatteryValue(actualDrain);
					// 对于一般的不包含uid的我们用uid本身替代。
					batteryData.setAppPackageName("actual drain");
					batteryDatas.add(batteryData);
				}
				continue;
			}

			// 判断字符串中是否包含Uid
			if (captureValue[i].contains("Uid")) {
				String tempStr = CommonUtil.formatBlanksToBlank(captureValue[i]);
				String[] Uidbatterys = tempStr.split(" ");
				String uid = Uidbatterys[1].substring(0, Uidbatterys[1].length() - 1);
				float value = Float.parseFloat(Uidbatterys[2]);
				batteryData.setUid(uid);
				batteryData.setBatteryValue(String.valueOf(value));
				String appPackageName = get_package_name_by_uid(uid);
				if (nowTestPackageName != null && nowTestPackageName.equals(appPackageName)) {
					if (detailInfo.length() > 1024) {
						detailInfo = detailInfo.substring(0, 1024);
					}
					batteryData.setDetailInfo(detailInfo);
				}
				batteryData.setAppPackageName(appPackageName);
				batteryDatas.add(batteryData);
			} else {
				String tempStr = CommonUtil.formatBlanksToBlank(captureValue[i]);
				String[] UidBatterys = tempStr.split(":");
				String uid = UidBatterys[0].trim();
				float value = Float.parseFloat(UidBatterys[1].trim());
				batteryData.setUid(uid);
				batteryData.setBatteryValue(String.valueOf(value));
				// 对于一般的不包含uid的我们用uid本身替代。
				batteryData.setAppPackageName(uid);
				batteryDatas.add(batteryData);
			}
		}

		return batteryDatas;
	}
	
	
	public static List<BatteryData> getpowerdata(String pac) throws IOException {
		if (CommonUtil.strIsNull(pac)) {
			return null;
		}
		
		CommandResult data = get_battery_data(pac);
		writetofile(data.successMsg);
		System.out.println(data.successMsg);
		List<BatteryData> batteryDatas = handlePowerData(data.successMsg);
		return batteryDatas;
	}

	public static void main(String[] args) throws IOException {
		// CommandResult data = get_battery_data("com.xdja.safekeyservice");
		// writetofile(data.successMsg);
		String oldName = System.getProperty("user.dir");
		String filedemopath = oldName + "/powerresult/batteryData.txt";
		FileInputStream fileInputStream = new FileInputStream(filedemopath);
		byte[] buffer = new byte[1024];
		StringBuffer sbBuffer = new StringBuffer();
		int length = 0;
		while((length = fileInputStream.read(buffer, 0, buffer.length))!= -1){
			String bu = new String(buffer, 0, length);
			sbBuffer.append(bu);
		}
		
//		System.out.println(handlePowerData(sbBuffer.toString()));
		nowTestPackageName = "com.xdja.HDSafeEMailClient";
		BaseTestInfo baseTestInfo = CollectDataImpl.getBaseTestInfo(nowTestPackageName);
		String testVersion = "";
		if (baseTestInfo != null) {
			testVersion = baseTestInfo.versionName;
		}
		List<BatteryHandleResult> handleResults = HandleDataManager.getInstance().handleBatteryData(handlePowerData(sbBuffer.toString()), nowTestPackageName, testVersion);
		PerformanceDB.getInstance().insertBatteryData(handleResults);
	}
}