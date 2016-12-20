package com.xdja.collectdata;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import com.xdja.constant.Constants;
import com.xdja.log.LoggerManager;
import com.xdja.util.CommonUtil;

/**
 *  获取和Android相关的一些信息
 * @author zlw
 *
 */
public class AndroidUtil {
	private final static String LOGTAG = AndroidUtil.class.getSimpleName();
	private static CommandResult commandMemoryResult,  commandFpsResult, commandFlowResult, commandKpiResult, commandCpuResult= null;
	private static CommandResult commandPidResult, commandUidResult;
	private static FlowData flowData = null;

	/**
	 * 获取对应包名的pid
	 * @param packageName
	 * @return
	 */
	public static int getPid(String packageName) {
		String cmd = "adb shell ps | grep "+ packageName +" | awk '{print $2}'";
//		String cmd = "ps";
		commandPidResult = AdbUtil.execCmdCommand(cmd, false, true);
		if (commandPidResult == null) {
			return 0;
		}
		
		if (commandPidResult.errorMsg != null && !"".equals(commandPidResult.errorMsg)) {
			LoggerManager.logError(LOGTAG, "getPid", commandPidResult.errorMsg);
			return 0;
		}
		
		if (commandPidResult.successMsg != null && commandPidResult.successMsg != "") {
			if (commandPidResult.successMsg.split("\n").length > 1) {
				return Integer.parseInt(commandPidResult.successMsg.split("\n")[0]);
			}else {
				return Integer.parseInt(commandPidResult.successMsg.trim());
			}
			
		}
		return 0;
	}
	
	/***
	 *  用于获取帧率相关数据
	 * @param packageName
	 * @return
	 */
//	public static FpsData getFpsData(String packageName) {
//		
//	}
//	
//	public static KpiData getKpiData(String packageName){
//		
//	}
	
	/**
	 * 根据deviceNo获取当前正在运行的进程
	 * @param deviceNo
	 * @return
	 */
	public static List<String> getRunningProcess(String deviceNo){
		String systemcmd = "adb -s " + deviceNo + " shell ps | findStr \"^system\"";
		String u0cmd = "adb -s " + deviceNo + " shell ps | findStr \"^u0\"";
		CommandResult runningCmdResult = AdbUtil.execCmdCommand(systemcmd, false, true);
		List<String> runningProcess = new ArrayList<>(20);
		if (runningCmdResult == null || !"".equals(runningCmdResult.errorMsg)) {
			LoggerManager.logError(LOGTAG, "getRunningProcess", runningCmdResult.errorMsg);
			return runningProcess;
		}
		
		if (!"".equals(runningCmdResult.successMsg)) {
			String[] processes = runningCmdResult.successMsg.split("\n");
			String[] processArray = null;
			for (int i = 0; i < processes.length; i++) {
				String process = CommonUtil.formatBlanksToBlank(processes[i]);
				processArray = process.split(" ");
				process = processArray[processArray.length - 1];
				if (!"".equals(process) && !process.startsWith("/")) {
					runningProcess.add(process);
				}
			}
		}
		
		CommandResult runningU0Result = AdbUtil.execCmdCommand(u0cmd, false, true);
		if (runningU0Result == null || !"".equals(runningU0Result.errorMsg)) {
			LoggerManager.logError(LOGTAG, "getRunningProcess", runningU0Result.errorMsg);
			return runningProcess;
		}
		
		if (!"".equals(runningU0Result.successMsg)) {
			String[] processes = runningU0Result.successMsg.split("\n");
			String[] processArray = null;
			for (int i = 0; i < processes.length; i++) {
				String process = CommonUtil.formatBlanksToBlank(processes[i]);
				processArray = process.split(" ");
				process = processArray[processArray.length - 1];
				if (!"".equals(process) && !process.startsWith("/")) {
					runningProcess.add(process);
				}
			}
		}
		
		return runningProcess;
	}
	
	/**
	 * 获取当前包名消耗了多少流量
	 * 待完善
	 * @param packageName
	 * @return
	 */
	public static FlowData getFlowData(String packageName) {
		String uid = getUid(packageName);
		if(uid == null || "".equals(uid)){
			LoggerManager.logDebug(LOGTAG, "getFlowData","Uid is Null");
			flowData = new FlowData(0, 0, 0);
			return flowData;
		}
		String cmd = "cat /proc/net/xt_qtaguid/stats";
		commandFlowResult = AdbUtil.execCmdCommand(cmd, false, true);
		int flowSend = 0,flowRecv = 0;
		if (commandFlowResult == null || commandFlowResult.errorMsg != null && !"".equals(commandFlowResult.errorMsg)) {
			String cmdSnd = "cat /proc/uid_stat/"+uid+"/tcp_snd";
			String cmdRec = "cat /proc/uid_stat/"+uid+"+/tcp_rcv";
			commandFlowResult = AdbUtil.execCmdCommand(cmdSnd, false, true);
			
			if (commandFlowResult != null && !commandFlowResult.errorMsg.contains("No such file or directory")) {
				flowSend = Integer.parseInt(commandFlowResult.successMsg);
			}
			
			commandFlowResult = AdbUtil.execCmdCommand(cmdRec, false, true);
			if (commandFlowResult != null && !commandFlowResult.errorMsg.contains("No such file or directory")) {
				flowRecv = Integer.parseInt(commandFlowResult.successMsg);
			}
			
			float totalFlow = (flowSend + flowRecv) / 1024 / 1024;
			totalFlow = CommonUtil.getTwoDots(totalFlow);
			flowData = new FlowData(totalFlow, flowRecv, flowSend);
			return flowData;
		}
		
		
		if (commandFlowResult.successMsg != null && "".equals(commandFlowResult.successMsg)) {
			String cmdproc = "cat /proc/net/xt_qtaguid/stats | grep " + uid;
			commandFlowResult = AdbUtil.execCmdCommand(cmdproc, false, true);
			String netStats = commandFlowResult.successMsg;
			int totalRecv = 0;
			int totalSend = 0;
			for(String line : netStats.split("\n")){
				int recv_bytes = Integer.parseInt(line.split("")[5]);
				int send_bytes = Integer.parseInt(line.split("")[7]);
				totalRecv += recv_bytes;
				totalSend += send_bytes;
			}
			
			float totalFlows = (totalRecv + totalSend) / 1024;
			totalFlows = CommonUtil.getTwoDots(totalFlows);
			flowData = new FlowData(totalFlows, totalRecv, totalSend);
			return flowData;
		}
		
		flowData = new FlowData(0, 0, 0);
		return flowData;
	}
	
	
	/**
	 *  获取内存数据, 返回的数据单位是KB
	 * @param packageName
	 * @return
	 */
	public static float getMemoryData(String packageName) {
		String cmd = "adb shell dumpsys meminfo " + packageName;
		commandMemoryResult = AdbUtil.execCmdCommand(cmd, false, true);
		if (commandMemoryResult == null) {
			return 0;
		}
		
		if (commandMemoryResult.errorMsg != null && !"".equals(commandMemoryResult.errorMsg)) {
			LoggerManager.logError(LOGTAG, "getMemoryData", commandMemoryResult.errorMsg);
			return 0;
		}
		
		if (commandMemoryResult.successMsg != null && commandMemoryResult.successMsg != "") {
			String regRex = ".*(Dalvik Heap.*)";
			Pattern pattern = Pattern.compile(regRex);
			Matcher mat = pattern.matcher(commandMemoryResult.successMsg);
			boolean rs = mat.find();
			if (rs) {
				String memoryStr = mat.group(0);
				memoryStr = CommonUtil.formatBlanksToBlank(memoryStr);
				String[] memorys = memoryStr.split(" ");
				float memory = Float.parseFloat(memorys[memorys.length -2]);
				return CommonUtil.getTwoDots(memory);
			}
		}
		return 0;
	}
	
	/***
	 *  用于根据包名获取cpu利用率,每隔Constant.collectInterval时间采集一次数据
	 *  @param packageName 测试的包名
	 */
	public static float getCpuUsage(String packageName) {
		int startProcTotal = getCpuTotal();
		int startProcPid   = getProcData(packageName);
		
		try {
			Thread.sleep(Constants.collectInterval * 1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		int endProcTotal = getCpuTotal();
		int endProcPid   = getProcData(packageName);
		
		// 保留2位小数
		float cpuData = (endProcPid - startProcPid) / (startProcTotal - endProcTotal) * 100;
		return CommonUtil.getTwoDots(cpuData);
	}
	
	/***
	 *  通过/proc/pid/stat来查看进程相关的信息
        14659 (a.android.rimet) S 311 310 0 0 -1 4194624 1445971 495 3 0 14873 1054 0 2
        14 -6 118 0 10247632 1173536768 28547 18446744073709551615 2864488448 2864496639
        4293931824 4293923376 4149515148 0 4612 4096 50426 18446744073709551615 0 0 17
        5 0 0 0 0 0 2864500104 2864500736 2871308288 4293933758 4293933845 4293933845 42
        93935070 0
        cutime ： 所有已死线程在用户态运行的时间
        cstime ： 所有已死线程在核心态运行的时间
	 * @return
	 */
	private static int getProcData(String packageName) {
		int pid = getPid(packageName);
		String cmd = "adb shell cat /proc/"+ pid +"/stat";
		commandCpuResult = AdbUtil.execCmdCommand(cmd, false, true);
		
		if (commandCpuResult == null) {
			return 0;
		}
		
		if (commandCpuResult.errorMsg != null && !"".equals(commandCpuResult.errorMsg)) {
			LoggerManager.logError(LOGTAG, "getProcData", commandCpuResult.errorMsg);
			return 0;
		}
		if (commandCpuResult.successMsg != null && !"".equals(commandCpuResult.successMsg)) {
			String[] times = commandCpuResult.successMsg.split(" ");
			int utime = Integer.parseInt(times[13]);
			int stime = Integer.parseInt(times[14]);
			
			return utime + stime;
		}
		
		return 0;
		
	}
	
	/***
	 *   获取总的cpu使用情况
	 * @return
	 */
	private static int getCpuTotal(){
		// 累积了从系统启动到现在的cpu总的信息
		String cmd = "adb shell cat /proc/stat";
		commandCpuResult = AdbUtil.execCmdCommand(cmd, false, true);
		if (commandCpuResult == null) {
			return 0;
		}
		
		if (commandCpuResult.errorMsg != null && !"".equals(commandCpuResult.errorMsg)) {
			LoggerManager.logError(LOGTAG, "getCpuTotal", commandCpuResult.errorMsg);
			return 0;
		}
		
		if (commandCpuResult.successMsg != null && commandCpuResult.successMsg != "") {
			String totalCpu = commandCpuResult.successMsg.split("\n")[0].trim();
			String[] cpus = totalCpu.split(" ");
			int totalCpus = 0;
			// 前面有两个空格
			for (int i = 2; i < cpus.length; i++) {
				if(cpus[i] != null){
					totalCpus += Integer.parseInt(cpus[i].trim());
				}
			}
			
			return totalCpus;
		}
		
		return 0;
	}
	
	/**
	 * 获取当前应用的Uid
	 * @param packageName
	 */
	private static String getUid(String packageName){
		int pid = getPid(packageName);
		String cmd = "adb shell cat /proc/"+pid+"/status | grep Uid";
		commandUidResult = AdbUtil.execCmdCommand(cmd, false, true);
		if (commandUidResult == null) {
			return "";
		}
		
		if (commandUidResult.errorMsg != null && !"".equals(commandUidResult.errorMsg)) {
			LoggerManager.logError(LOGTAG, "getCpuTotal", commandUidResult.errorMsg);
			return "";
		}
		
		if (commandUidResult.successMsg != null && commandUidResult.successMsg != "") {
			return commandUidResult.successMsg.split("\t")[1];
		}
		
		return "";
	}
	
	public static void main(String[] args) {
		List myList = AndroidUtil.getRunningProcess("ab6e5736");
		System.out.println(myList);
		System.out.println(myList.size());
	}
}
