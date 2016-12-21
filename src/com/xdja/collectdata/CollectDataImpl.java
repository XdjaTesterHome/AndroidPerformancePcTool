package com.xdja.collectdata;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.xdja.constant.Constants;
import com.xdja.log.LoggerManager;
import com.xdja.util.CommonUtil;

/**
 * 获取和Android相关的一些信息
 * 
 * @author zlw
 *
 */
public class CollectDataImpl {
	private final static String LOGTAG = CollectDataImpl.class.getSimpleName();
	private static CommandResult commandMemoryResult, commandFpsResult, commandFlowResult,
			commandCpuResult = null;
	private static CommandResult commandPidResult, commandUidResult;
	private static FlowData flowData = null;
	private static KpiData kpiData = null;
	private static List<KpiData>kpiList = new ArrayList<>(28);
	
	private static GetDataInterface mGetDataListener = new GetDataInterface() {
		
		@Override
		public void getString(String content) {
			// TODO Auto-generated method stub
			System.out.println(content);
			if (content != null && content.contains("Displayed")) {
				String[] contents = content.split("Displayed");
				if (contents.length > 1) {
					String kpiStr = contents[1].trim();
					String nowPageName = kpiStr.split(":")[0].trim();
					nowPageName = nowPageName.split("/")[1].trim();
					if (nowPageName.startsWith(".")) {
						nowPageName = nowPageName.substring(1);
					}
					
					String costtimeStr = kpiStr.split(":")[1].trim();
					int costTime = formatStrToms(costtimeStr);
					if (nowPageName == null || "".equals(nowPageName)) {
						nowPageName = "unkonwn";
					}
					
					kpiData = new KpiData(nowPageName, costTime);
					if (kpiList.contains(kpiData)) {
						int lastCostTime = kpiList.get(kpiList.indexOf(kpiData)).loadTime;
						costTime = (costTime + lastCostTime) / 2;
						kpiData.setLoadTime(costTime);
					}
					kpiList.add(kpiData);
				}
			}
		}
		
		@Override
		public void getErrorString(String error) {
			// TODO Auto-generated method stub
			
		}
	};

	/**
	 *  用于获取kpi数据,仅仅用于启动收集kpi数据
	 * @param packageName
	 * @return
	 */
	public static void startCollectKpiData(String packageName){
		kpiList.clear();
		String cmd = "adb logcat -v time -s ActivityManager | findStr " + packageName;
		String clearcmd = "adb logcat -c";
		CollectDataUtil.execCmdCommand(clearcmd);
		CollectDataUtil.execCmdCommand(cmd, mGetDataListener);
	}
	
	/***
	 * 获取kpi的数据
	 * @return
	 */
	public static List<KpiData> getKpiData() {
		return kpiList;
	}
	/***
	 * 用于获取帧率相关数据
	 * 
	 * @param packageName
	 * @return
	 */
	public static FpsData getFpsData(String packageName) {
		String cmd = "adb shell dumpsys gfxinfo " + packageName;
		commandFpsResult = CollectDataUtil.execCmdCommand(cmd, false, true);
		if (commandFpsResult == null || !"".equals(commandFpsResult.errorMsg)) {
			LoggerManager.logDebug(LOGTAG, "getFpsData", "get fps is wrong");
			return null;
		}
		return handleFpsData(commandFpsResult.successMsg);
	}

	// public static KpiData getKpiData(String packageName){
	//
	// }
	/**
	 * 设备ID编号处理，处理为进程保活方法所需参数，可以使用的设备号
	 * @param devicedo
	 * @return lzz
	 */
	public static String devicesdo(Object selected){
		String str = (String) selected;
		String pattern = "_(.*)_(.*)";
		Pattern r = Pattern.compile(pattern);
		Matcher m = r.matcher(str);
		if (m.find( )) {
			selected = m.group(2);
		}
		 else {
	         System.out.println("NO MATCH");
	      }
		return (String) selected;
	}
	/**
	 * 根据deviceNo获取当前正在运行的进程
	 * 
	 * @param deviceNo
	 * @return
	 */
	public static List<String> getRunningProcess(String deviceNo) {
		String systemcmd = "adb -s " + deviceNo + " shell ps | findStr \"^system\"";
		String u0cmd = "adb -s " + deviceNo + " shell ps | findStr \"^u0\"";
		CommandResult runningCmdResult = CollectDataUtil.execCmdCommand(systemcmd, false, true);
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

		CommandResult runningU0Result = CollectDataUtil.execCmdCommand(u0cmd, false, true);
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
	 * 获取当前包名消耗了多少流量 待完善
	 * 
	 * @param packageName
	 * @return
	 */
	public static FlowData getFlowData(String packageName) {
		String uid = getUid(packageName);
		if (uid == null || "".equals(uid)) {
			LoggerManager.logDebug(LOGTAG, "getFlowData", "Uid is Null");
			flowData = new FlowData(0, 0, 0);
			return flowData;
		}
		String cmd = "adb shell cat /proc/net/xt_qtaguid/stats";
		commandFlowResult = CollectDataUtil.execCmdCommand(cmd, false, true);
		int flowSend = 0, flowRecv = 0;
		if (commandFlowResult == null || commandFlowResult.errorMsg != null && !"".equals(commandFlowResult.errorMsg)) {
			String cmdSnd = "adb shell cat /proc/uid_stat/" + uid + "/tcp_snd";
			String cmdRec = "adb shell cat /proc/uid_stat/" + uid + "+/tcp_rcv";
			commandFlowResult = CollectDataUtil.execCmdCommand(cmdSnd, false, true);

			if (commandFlowResult != null && !commandFlowResult.errorMsg.contains("No such file or directory")) {
				flowSend = Integer.parseInt(commandFlowResult.successMsg);
			}

			commandFlowResult = CollectDataUtil.execCmdCommand(cmdRec, false, true);
			if (commandFlowResult != null && !commandFlowResult.errorMsg.contains("No such file or directory")) {
				flowRecv = Integer.parseInt(commandFlowResult.successMsg);
			}

			float totalFlow = (flowSend + flowRecv) / 1024 / 1024;
			totalFlow = CommonUtil.getTwoDots(totalFlow);
			flowData = new FlowData(totalFlow, flowRecv, flowSend);
			return flowData;
		}

		if (commandFlowResult.successMsg != null && !"".equals(commandFlowResult.successMsg)) {
			String cmdproc = "adb shell cat /proc/net/xt_qtaguid/stats | grep " + uid;
			commandFlowResult = CollectDataUtil.execCmdCommand(cmdproc, false, true);
			String netStats = commandFlowResult.successMsg;
			int totalRecv = 0;
			int totalSend = 0;
			for (String line : netStats.split("\n")) {
				if (line == null || "".equals(line)) {
					continue;
				}
				int recv_bytes = Integer.parseInt(line.split(" ")[5]);
				int send_bytes = Integer.parseInt(line.split(" ")[7]);
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
	 * 获取内存数据, 返回的数据单位是KB
	 * 
	 * @param packageName
	 * @return
	 */
	public static float getMemoryData(String packageName) {
		String cmd = "adb shell dumpsys meminfo " + packageName;
		commandMemoryResult = CollectDataUtil.execCmdCommand(cmd, false, true);
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
				float memory = Float.parseFloat(memorys[memorys.length - 2]);
				return CommonUtil.getTwoDots(memory);
			}
		}
		return 0;
	}

	/***
	 * 用于根据包名获取cpu利用率,每隔Constant.collectInterval时间采集一次数据
	 * 
	 * @param packageName
	 *            测试的包名
	 */
	public static float getCpuUsage(String packageName) {
		int startProcTotal = getCpuTotal();
		int startProcPid = getProcData(packageName);
		
		//每隔1s取一次数据。
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		int endProcTotal = getCpuTotal();
		int endProcPid = getProcData(packageName);

		// 保留2位小数
		float cpuData = (endProcPid - startProcPid) / (startProcTotal - endProcTotal) * 100;
		return CommonUtil.getTwoDots(cpuData);
	}
	
	/***
	 *  获取当前的页面
	 * @return
	 */
	public static String getCurActivity() {
		String cmd = "adb shell dumpsys activity top | findStr ACTIVITY";
		CommandResult activityResult = CollectDataUtil.execCmdCommand(cmd, false, true);
		
		if (activityResult != null && !"".equals(activityResult.successMsg)) {
			String activityName = CommonUtil.formatBlanksToBlank(activityResult.successMsg);
			activityName = activityName.split(" ")[1];
			activityName = activityName.split("/")[1];
			if (activityName.startsWith(".")) {
				activityName = activityName.substring(1);
			}
			
			return activityName;
		}
		
		return "";
	}
	/***
	 * 通过/proc/pid/stat来查看进程相关的信息 14659 (a.android.rimet) S 311 310 0 0 -1
	 * 4194624 1445971 495 3 0 14873 1054 0 2 14 -6 118 0 10247632 1173536768
	 * 28547 18446744073709551615 2864488448 2864496639 4293931824 4293923376
	 * 4149515148 0 4612 4096 50426 18446744073709551615 0 0 17 5 0 0 0 0 0
	 * 2864500104 2864500736 2871308288 4293933758 4293933845 4293933845 42
	 * 93935070 0 cutime ： 所有已死线程在用户态运行的时间 cstime ： 所有已死线程在核心态运行的时间
	 * 
	 * @return
	 */
	private static int getProcData(String packageName) {
		int pid = getPid(packageName);
		String cmd = "adb shell cat /proc/" + pid + "/stat";
		commandCpuResult = CollectDataUtil.execCmdCommand(cmd, false, true);

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
	 * 获取总的cpu使用情况
	 * 
	 * @return
	 */
	private static int getCpuTotal() {
		// 累积了从系统启动到现在的cpu总的信息
		String cmd = "adb shell cat /proc/stat";
		commandCpuResult = CollectDataUtil.execCmdCommand(cmd, false, true);
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
				if (cpus[i] != null) {
					totalCpus += Integer.parseInt(cpus[i].trim());
				}
			}

			return totalCpus;
		}

		return 0;
	}

	/**
	 * 获取当前应用的Uid
	 * 
	 * @param packageName
	 */
	private static String getUid(String packageName) {
		int pid = getPid(packageName);
		String cmd = "adb shell cat /proc/" + pid + "/status | grep Uid";
		commandUidResult = CollectDataUtil.execCmdCommand(cmd, false, true);
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

	/**
	 * 处理Fps的数据，抓取自己关心的数据
	 * 
	 * @param content
	 * @return
	 */
	private static FpsData handleFpsData(String content) {
		if (content == null || "".equals(content)) {
			return null;
		}

		// content = CommonUtil.formatBlanksToBlank(content);
		String[] firstSpilt = content.split("Draw	Prepare	Process	Execute");
		String[] secondSpilt = firstSpilt[2].split("View hierarchy:");
		String[] thirdSpilt = secondSpilt[0].trim().split("\n\n\t");
		if (thirdSpilt == null || thirdSpilt.length < 1) {
			LoggerManager.logDebug(LOGTAG, "handleFpsData", "no operation mobilephone");
			return new FpsData(0, 0, 0);
		}
		String[] fpsSplit;
		int frameCount = thirdSpilt.length;
		int jank_count = 0;
		int vsync_count = 0;
		
		for (int i = 0; i < thirdSpilt.length; i++) {
			fpsSplit = thirdSpilt[i].split("\t");
			// 判断是否是4列数字，不是4列数字，直接跳过
			if (fpsSplit.length % 4 != 0) {
				LoggerManager.logDebug(LOGTAG, "handleFpsData", "fps count is wrong");
				continue;
			}
			// 计算一帧的渲染时间
			float totaltime = 0;
			for (int j = 0; j < fpsSplit.length; j++) {
				totaltime += Float.parseFloat(fpsSplit[j]);
			}

			// 大于16ms的帧会丢帧
			if (totaltime > 16) {
				jank_count += 1;
				if (totaltime % 16.67 == 0) {
					vsync_count += totaltime / 16.67 - 1;
				} else {
					vsync_count += totaltime / 16.67;
				}
			}
		}
		int fps = frameCount * 60 / (frameCount + vsync_count);
		FpsData fpsData = new FpsData(fps, jank_count, frameCount);
		return fpsData;
	}
	/**
	 * 获取对应包名的pid
	 * 
	 * @param packageName
	 * @return
	 */
	public static int getPid(String packageName) {
		String cmd = "adb shell ps | grep " + packageName + " | awk '{print $2}'";
		// String cmd = "ps";
		commandPidResult = CollectDataUtil.execCmdCommand(cmd, false, true);
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
			} else {
				return Integer.parseInt(commandPidResult.successMsg.trim());
			}

		}
		return 0;
	}
	
	/**
	 *  将timeStr转成毫秒
	 * @param timeStr
	 * @return
	 */
	private static int formatStrToms(String timeStr){
		if (timeStr == null || "".equals(timeStr)) {
			return 0;
		}
		
		if (timeStr.startsWith("+")) {
			timeStr = timeStr.substring(1);
		}
		
		Pattern pattern = Pattern.compile("\\d+s");
		Matcher matcher = pattern.matcher(timeStr);
		int totalTime = 0;
		if (matcher.find()) {
			String sString = matcher.group(0);
			sString = sString.split("s")[0];
			totalTime = Integer.parseInt(sString) * 1000;
		}
		
		Pattern pattern2 = Pattern.compile("\\d+ms");
		Matcher matcher2 = pattern2.matcher(timeStr);
		if (matcher2.find()) {
			String sString = matcher2.group(0);
			sString = sString.split("ms")[0];
			totalTime = Integer.parseInt(sString);
		}
		
		return totalTime;
	}
	
	
	public static void main(String[] args) {
		FlowData flowData = CollectDataImpl.getFlowData("com.xdja.HDSafeEMailClient");
		for(int i = 0; i< 20; i++){
			System.out.println(flowData);
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

}
