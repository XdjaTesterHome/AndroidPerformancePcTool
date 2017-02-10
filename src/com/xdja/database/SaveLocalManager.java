package com.xdja.database;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.xdja.collectdata.handleData.entity.BatteryHandleResult;
import com.xdja.collectdata.handleData.entity.CpuHandleResult;
import com.xdja.collectdata.handleData.entity.FlowHandleResult;
import com.xdja.collectdata.handleData.entity.FpsHandleResult;
import com.xdja.collectdata.handleData.entity.KpiHandleResult;
import com.xdja.collectdata.handleData.entity.MemoryHandleResult;
import com.xdja.constant.Constants;
import com.xdja.constant.GlobalConfig;
import com.xdja.exception.SettingException;
import com.xdja.util.CommonUtil;
import com.xdja.util.ExcelUtil;
import com.xdja.util.ProPertiesUtil;

/**
 * 读写Excel表格的工具类
 * 
 * @author zlw
 */
public class SaveLocalManager {

	private static SaveLocalManager mInstance = null;
	
	private String mFilePath;
	private ExcelUtil mExcelUtil;
	// sheetName
	private final static String MEMORY_SHEET = "内存";
	private final static String CPU_SHEET = "cpu";
	private final static String kpi_SHEET = "页面加载时间";
	private final static String FPS_SHEET = "页面流畅度";
	private final static String BATTERY_SHEET = "电量使用";
	private final static String FLOW_SHEET = "流量";
	private final static String SILENT_FLOW_SHEET = "静默流量";
	private final static String SILENT_CPU_SHEET = "静默CPU";
	
	// titles
	private final static String[] MEMORY_TITLES = {"页面名称","内存已经分配值(MB)","日志路径", "内存快照路径", "测试结果"};
	private final static String[] FLOW_TITLES = {"页面名称","流量消耗值(KB)","日志路径", "测试结果"};
	private final static String[] FPS_TITLES = {"页面名称","页面流畅度(fps)","丢帧数", "总帧数", "日志路径", "方法调用路径trace", "测试结果"};
	private final static String[] KPI_TITLES = {"页面名称","页面加载时间(ms)","日志路径", "方法调用栈", "测试结果"};
	private final static String[] CPU_TITLES = {"页面名称","cpu使用率(%)","日志路径", "方法调用栈", "测试结果"};
	private final static String[] BATTERY_TITLES = {"测试项","消耗电量值(mAh)","详细耗电信息"};
	private final static String[] CPU_SILENT_TITLES = {"页面名称", "cpu使用率(%)", "日志路径", "方法调用栈"};
	private final static String[] FLOW_SILENT_TITLES = {"页面名称", "流量消耗值(KB)", "日志路径"};
	
	private SaveLocalManager() {
		mFilePath = getFilePath(formatExcelFileName(GlobalConfig.TestPackageName, GlobalConfig.TestVersion));
		mExcelUtil = new ExcelUtil(mFilePath);
	}

	public static SaveLocalManager getInstance() {
		if (mInstance == null) {
			synchronized (SaveLocalManager.class) {
				if (mInstance == null) {
					mInstance = new SaveLocalManager();
				}
			}
		}

		return mInstance;
	}
	
	/**
	 * 
	 * @param packageName
	 * @param version
	 * @return
	 */
	private String formatExcelFileName(String packageName, String version){
		if (CommonUtil.strIsNull(packageName)) {
			packageName = "com.xdja.xxx";
		}
		
		if (CommonUtil.strIsNull(version)) {
			version = "0.0.0.0";
		}
		
		String fileName = packageName + "_" + version;
		
		return fileName;
	}
	
	/**
	 *  获取存储文件的路径
	 * @param fileName
	 * @return
	 */
	private String getFilePath(String fileName){
		String settingPath = ProPertiesUtil.getInstance().getProperties(Constants.LOCALSAVE_SETTING);
		if (CommonUtil.strIsNull(settingPath)) {
			settingPath = System.getProperty("user.home");
			settingPath = settingPath + "\\performance\\";
		}
		
		if (CommonUtil.strIsNull(fileName)) {
			return settingPath + "\\" +"default.xlsx";
		}
		
		File file = new File(settingPath);
		if (!file.exists()) {
			file.mkdirs();
		}
		
		return settingPath +"\\"+ fileName + ".xlsx";
	}
	
	/**
	 * 保存测试包名和版本号
	 * @param packageName
	 * @param version
	 */
	public void setTestPackageAndVersion(String packageName, String version){
//		mFilePath = getFilePath(formatExcelFileName(packageName, version));
//		mExcelUtil = new ExcelUtil(mFilePath);
	}
	
	/**
	 *  将内存测试结果保存到本地
	 * @param handleResults
	 * @throws SettingException 
	 */
	public void saveMemoryDataToLocal(List<MemoryHandleResult> handleResults) throws SettingException{
		if (CommonUtil.strIsNull(mFilePath)) {
			throw new SettingException("请先调用setTestPackageAndVersion方法设置保存包名和版本号");
		}
		if (handleResults == null || handleResults.size() < 1) {
			return;
		}
		
		List<String[]> tempList = new ArrayList<>(handleResults.size());
		for(MemoryHandleResult handleResult : handleResults){
			tempList.add(handleResult.formatDataToArray());
		}
		
		mExcelUtil.writeDataToExcel(MEMORY_SHEET, MEMORY_TITLES, tempList);
	}
	
	/**
	 *  存储页面加载时间数据
	 * @param handleResults
	 * @throws SettingException 
	 */
	public void saveKpiDataToLocal(List<KpiHandleResult> handleResults) throws SettingException{
		if (CommonUtil.strIsNull(mFilePath)) {
			throw new SettingException("请先调用setTestPackageAndVersion方法设置保存包名和版本号");
		}
		if (handleResults == null || handleResults.size() < 1) {
			return;
		}
		
		List<String[]> tempList = new ArrayList<>(handleResults.size());
		for(KpiHandleResult handleResult : handleResults){
			tempList.add(handleResult.formatDataToArray());
		}
		
		mExcelUtil.writeDataToExcel(kpi_SHEET, KPI_TITLES, tempList);
	}
	
	/**
	 *  保存cpu数据的结果
	 * @param handleResults
	 * @throws SettingException 
	 */
	public void saveCpuDataToLocal(List<CpuHandleResult> handleResults) throws SettingException{
		if (CommonUtil.strIsNull(mFilePath)) {
			throw new SettingException("请先调用setTestPackageAndVersion方法设置保存包名和版本号");
		}
		
		if (handleResults == null || handleResults.size() < 1) {
			return;
		}
		
		List<String[]> tempList = new ArrayList<>(handleResults.size());
		for(CpuHandleResult handleResult : handleResults){
			tempList.add(handleResult.formatDataToArray());
		}
		
		mExcelUtil.writeDataToExcel(CPU_SHEET, CPU_TITLES, tempList);
	}
	
	/**
	 *  保存cpu数据的结果
	 * @param handleResults
	 * @throws SettingException 
	 */
	public void saveFpsDataToLocal(List<FpsHandleResult> handleResults) throws SettingException{
		if (CommonUtil.strIsNull(mFilePath)) {
			throw new SettingException("请先调用setTestPackageAndVersion方法设置保存包名和版本号");
		}
		
		if (handleResults == null || handleResults.size() < 1) {
			return;
		}
		
		List<String[]> tempList = new ArrayList<>(handleResults.size());
		for(FpsHandleResult handleResult : handleResults){
			tempList.add(handleResult.formatDataToArray());
		}
		
		mExcelUtil.writeDataToExcel(FPS_SHEET, FPS_TITLES, tempList);
	}
	
	/**
	 *  保存cpu数据的结果
	 * @param handleResults
	 * @throws SettingException 
	 */
	public void saveFlowDataToLocal(List<FlowHandleResult> handleResults) throws SettingException{
		if (CommonUtil.strIsNull(mFilePath)) {
			throw new SettingException("请先调用setTestPackageAndVersion方法设置保存包名和版本号");
		}
		if (handleResults == null || handleResults.size() < 1) {
			return;
		}
		
		List<String[]> tempList = new ArrayList<>(handleResults.size());
		for(FlowHandleResult handleResult : handleResults){
			tempList.add(handleResult.formatDataToArray());
		}
		
		mExcelUtil.writeDataToExcel(FLOW_SHEET, FLOW_TITLES, tempList);
	}
	
	/**
	 *  保存cpu数据的结果
	 * @param handleResults
	 * @throws SettingException 
	 */
	public void saveBatteryDataToLocal(List<BatteryHandleResult> handleResults) throws SettingException{
		if (CommonUtil.strIsNull(mFilePath)) {
			throw new SettingException("请先调用setTestPackageAndVersion方法设置保存包名和版本号");
		}
		
		if (handleResults == null || handleResults.size() < 1) {
			return;
		}
		
		List<String[]> tempList = new ArrayList<>(handleResults.size());
		for(BatteryHandleResult handleResult : handleResults){
			tempList.add(handleResult.formatDataToArray());
		}
		
		mExcelUtil.writeDataToExcel(BATTERY_SHEET, BATTERY_TITLES, tempList);
	}
	
	
	/**
	 *  保存静默流量的测试结果
	 * @param handleResults
	 * @throws SettingException 
	 */
	public void saveSilentFlowDataToLocal(List<FlowHandleResult> handleResults) throws SettingException{
		if (CommonUtil.strIsNull(mFilePath)) {
			throw new SettingException("请先调用setTestPackageAndVersion方法设置保存包名和版本号");
		}
		
		if (handleResults == null || handleResults.size() < 1) {
			return;
		}
		
		List<String[]> tempList = new ArrayList<>(handleResults.size());
		String[] tempArray = null;
		for(FlowHandleResult handleResult : handleResults){
			tempArray = new String[]{handleResult.activityName, handleResult.testValue, handleResult.logPath};
			tempList.add(tempArray);
		}
		
		mExcelUtil.writeDataToExcel(SILENT_FLOW_SHEET, FLOW_SILENT_TITLES, tempList);
	}
	
	/**
	 *  保存静默流量的测试结果
	 * @param handleResults
	 * @throws SettingException 
	 */
	public void saveSilentCpuDataToLocal(List<CpuHandleResult> handleResults) throws SettingException{
		if (CommonUtil.strIsNull(mFilePath)) {
			throw new SettingException("请先调用setTestPackageAndVersion方法设置保存包名和版本号");
		}
		
		if (handleResults == null || handleResults.size() < 1) {
			return;
		}
		
		List<String[]> tempList = new ArrayList<>(handleResults.size());
		String[] tempArray = null;
		for(CpuHandleResult handleResult : handleResults){
			tempArray = new String[]{handleResult.activityName, handleResult.testValue, handleResult.logPath, handleResult.methodTracePath};
			tempList.add(tempArray);
		}
		
		mExcelUtil.writeDataToExcel(SILENT_CPU_SHEET, CPU_SILENT_TITLES, tempList);
	}
}
