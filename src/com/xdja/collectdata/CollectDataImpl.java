package com.xdja.collectdata;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.xdja.collectdata.entity.BaseTestInfo;
import com.xdja.collectdata.entity.CommandResult;
import com.xdja.collectdata.entity.CpuData;
import com.xdja.collectdata.entity.FlowData;
import com.xdja.collectdata.entity.FpsData;
import com.xdja.collectdata.entity.KpiData;
import com.xdja.log.LoggerManager;
import com.xdja.util.CommonUtil;
import com.xdja.util.ExecShellUtil;
import com.xdja.util.ExecShellUtil.GetDataInterface;
import com.xdja.view.main.LaunchView;;
/**
 * 获取和Android相关的一些信息
 * 
 * @author zlw
 *
 */
public class CollectDataImpl {
	private final static String LOGTAG = CollectDataImpl.class.getSimpleName();
	private static CommandResult commandMemoryResult, commandFpsResult, commandFlowResult, commandCpuResult = null;
	private static CommandResult commandPidResult, commandUidResult;
	private static FlowData flowData = null;
	private static KpiData kpiData = null;
	private static FpsData fpsData = null;
	private static List<KpiData> kpiList = new ArrayList<>(12);
	private static List<FpsData> fpsList = new ArrayList<>(12);

	private static GetDataInterface mGetDataListener = new GetDataInterface() {

		@Override
		public void getString(String content) {
			// TODO Auto-generated method stub
			System.out.println("content = " + content);
			if (content != null && content.contains("Displayed")) {
				String[] contents = content.split("Displayed");
				if (contents.length > 1) {
					String kpiStr = contents[1].trim();
					String nowPageName = kpiStr.split(":")[0].trim();
					int index = nowPageName.lastIndexOf(".");
					nowPageName = nowPageName.substring(index + 1);
					
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
	 * 用于获取kpi数据,仅仅用于启动收集kpi数据
	 * 
	 * @param packageName
	 * @return
	 */
	public static void startCollectKpiData(String packageName) {
		kpiList.clear();
		String cmd = "adb logcat -v time -s ActivityManager | grep " + packageName;
		System.out.println(cmd);
		String clearcmd = "adb logcat -c";
		ExecShellUtil.getInstance().execShellCommand(clearcmd);
		System.out.println(clearcmd);
		ExecShellUtil.getInstance().execCmdCommand(cmd, mGetDataListener);
	}
	
	/**
	 *  停止收集Kpi数据
	 */
	public static void stopCollectKpiData(){
		ExecShellUtil.getInstance().stopProcess();
	}

	/***
	 * 获取kpi的数据
	 * 
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
	public static List<FpsData> getFpsData(String packageName) {
		String cmd = "shell dumpsys gfxinfo " + packageName;
		commandFpsResult = ExecShellUtil.getInstance().execShellCommand(cmd);
		if (commandFpsResult == null || !"".equals(commandFpsResult.errorMsg)) {
			LoggerManager.logDebug(LOGTAG, "getFpsData", "get fps is wrong");
			return null;
		}
		return handleFpsData(commandFpsResult.successMsg);
		// return null;
	}

	/**
	 * 设备ID编号处理，处理为进程保活方法所需参数，可以使用的设备号
	 * 
	 * @param devicedo
	 * @return lzz
	 */
	public static String devicesdo(Object selected) {
		String str = (String) selected;
		String pattern = "-(.*)-(.*)";
		Pattern r = Pattern.compile(pattern);
		Matcher m = r.matcher(str);
		if (m.find()) {
			selected = m.group(2);
		} else {
			// System.out.println("devicesdo NO MATCH");
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
		String systemcmd = "ps | grep \"^system\"";
		String u0cmd = "ps | grep \"^u0\"";
		CommandResult runningCmdResult = ExecShellUtil.getInstance().execShellCommand(systemcmd);
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

		CommandResult runningU0Result = ExecShellUtil.getInstance().execShellCommand(u0cmd);
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
			// LoggerManager.logDebug(LOGTAG, "getFlowData", "Uid is Null");
			flowData = new FlowData(0, 0, 0);
			return flowData;
		}
		String cmd = "cat /proc/net/xt_qtaguid/stats";
		commandFlowResult = ExecShellUtil.getInstance().execShellCommand(cmd);
		int flowSend = 0, flowRecv = 0;
		if (commandFlowResult == null || CommonUtil.strIsNull(commandFlowResult.successMsg) || commandFlowResult.successMsg.contains("No such file or directory")) {
			String cmdSnd = "cat /proc/uid_stat/" + uid + "/tcp_snd";
			String cmdRec = "cat /proc/uid_stat/" + uid + "+/tcp_rcv";
			commandFlowResult = ExecShellUtil.getInstance().execShellCommand(cmdSnd);

			if (commandFlowResult != null && !commandFlowResult.errorMsg.contains("No such file or directory")) {
				flowSend = Integer.parseInt(commandFlowResult.successMsg);
			}

			commandFlowResult = ExecShellUtil.getInstance().execShellCommand(cmdRec);
			if (commandFlowResult != null && !commandFlowResult.errorMsg.contains("No such file or directory")) {
				flowRecv = Integer.parseInt(commandFlowResult.successMsg);
			}
			flowData = new FlowData(getTwoPointsWithMB(flowSend + flowRecv), getTwoPointsWithMB(flowRecv), getTwoPointsWithMB(flowSend));
			return flowData;
		}

		if (commandFlowResult.successMsg != null && !"".equals(commandFlowResult.successMsg)) {
			String cmdproc = "cat /proc/net/xt_qtaguid/stats | grep " + uid;
			commandFlowResult = ExecShellUtil.getInstance().execShellCommand(cmdproc);
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
			flowData = new FlowData(getTwoPointsWithKB(totalRecv + totalSend), getTwoPointsWithKB(totalRecv), getTwoPointsWithMB(totalSend));
			return flowData;
		}

		flowData = new FlowData(0, 0, 0);
		return flowData;
	}
	
	/**
	 * 将int值转换成MB
	 * @param value
	 * @return
	 */
	private static float getTwoPointsWithMB(int value){
		float fValue = (float) (value / 1024.0 / 1024.0);
		fValue = CommonUtil.getTwoDots(fValue);
		return fValue;
	}
	
	/**
	 * 将int值转换成MB
	 * @param value
	 * @return
	 */
	private static float getTwoPointsWithKB(int value){
		float fValue = (float) (value / 1024.0);
		fValue = CommonUtil.getTwoDots(fValue);
		return fValue;
	}
	/**
	 * 获取内存数据, 返回的数据单位是KB
	 * 
	 * @param packageName
	 * @return
	 */
	public static float getMemoryData(String packageName) {
		String cmd = "dumpsys meminfo " + packageName;
		commandMemoryResult = ExecShellUtil.getInstance().execShellCommand(cmd);
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
	public static CpuData getCpuUsage(String packageName, int lastProcTotal, int lastProcPid) {
		int startProcTotal = getCpuTotal();
		int startProcPid = getProcData(packageName);
		// 保留2位小数
		float cpuData = 0;
		// 防止分母为0的情况存在
		if (startProcTotal - lastProcTotal != 0) {
			cpuData = (float) (startProcPid - lastProcPid) / (startProcTotal - lastProcTotal) * 100;
		}

		return new CpuData(startProcTotal, startProcPid, CommonUtil.getTwoDots(cpuData));
	}

	/***
	 * 获取当前的页面,只返回Activity的名字
	 * 
	 * @return
	 */
	public static String getCurActivity() {
		String cmd = "dumpsys activity | grep mFocusedActivity";
		CommandResult activityResult = ExecShellUtil.getInstance().execShellCommand(cmd);

		if (activityResult != null && !"".equals(activityResult.successMsg)) {
			String activityName = CommonUtil.formatBlanksToBlank(activityResult.successMsg);
			activityName = activityName.trim();
			activityName = activityName.split("/")[1];
			activityName = activityName.split(" ")[0];
			activityName = activityName.substring(activityName.lastIndexOf(".")+1);
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
		String cmd = "cat /proc/" + pid + "/stat";
		commandCpuResult = ExecShellUtil.getInstance().execShellCommand(cmd);

		if (commandCpuResult == null) {
			return 0;
		}

		if (commandCpuResult.errorMsg != null && !"".equals(commandCpuResult.errorMsg)) {
			LoggerManager.logError(LOGTAG, "getProcData", commandCpuResult.errorMsg);
			return 0;
		}

		if (!CommonUtil.strIsNull(commandCpuResult.successMsg)) {
			if (commandCpuResult.successMsg.contains("No such file or directory")) {
				return 0;
			}
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
		String cmd = "cat /proc/stat";
		commandCpuResult = ExecShellUtil.getInstance().execShellCommand(cmd);
		if (commandCpuResult == null) {
			return 0;
		}

		if (commandCpuResult.errorMsg != null && !"".equals(commandCpuResult.errorMsg)) {
			LoggerManager.logError(LOGTAG, "getCpuTotal", commandCpuResult.errorMsg);
			return 0;
		}

		if (commandCpuResult.successMsg != null && commandCpuResult.successMsg != "") {
			String totalCpu = commandCpuResult.successMsg.trim().split("\n")[0].trim();
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
		if (packageName ==null){  //条件语句处理空指针，处理package空指针异常
			packageName = LaunchView.getSelectPkg();
		}
		int pid = getPid(packageName);
		String cmd = "cat /proc/" + pid + "/status | grep Uid";
		commandUidResult = ExecShellUtil.getInstance().execShellCommand(cmd);
		if (commandUidResult == null) {
			return "";
		}

		if (commandUidResult.errorMsg != null && !"".equals(commandUidResult.errorMsg)) {
			LoggerManager.logError(LOGTAG, "getCpuTotal", commandUidResult.errorMsg);
			return "";
		}

		if (commandUidResult.successMsg != null && commandUidResult.successMsg != "") {
//			System.out.println("commandUidResult.successMsg:"+commandUidResult.successMsg);//
			return commandUidResult.successMsg.split("\t")[1];
		}

		return "";
	}

	private static String handleFps(String getfpsdata) {
		String line = getfpsdata;
		String resu = null;
		String pattern = "Execute[\\s\\S]*View hierarchy";
		// 创建 Pattern 对象
		Pattern r = Pattern.compile(pattern);
		// 现在创建 matcher 对象
		Matcher m = r.matcher(line);
		if (m.find()) {
			resu = (String) m.group(0);
		} else {
			System.out.println("handleFps NO MATCH");
		}
		return resu;

	}

	/**
	 * 处理Fps的数据，抓取自己关心的数据
	 * 
	 * @param content
	 * @return
	 */
	private static List<FpsData> handleFpsData(String content) {
		if (content == null || "".equals(content)) {
			return fpsList;
		}
		String activityName = CollectDataImpl.getCurActivity();
		content = handleFps(content);
		if (content == null) {
			return fpsList;
		}

		String[] firstSpilt = content.split("Execute");

		if (firstSpilt.length > 0) {
			String[] secondSpilt = firstSpilt[1].trim().split("View hierarchy");
			if (secondSpilt.length > 0) { // 表示是否有帧率数据
				String[] thirdSpilt = secondSpilt[0].trim().split("\n");
				if (thirdSpilt == null || thirdSpilt.length < 1) {
					System.out.println("handleFpsData thirdSpilt == null");
					// LoggerManager.logDebug(LOGTAG, "handleFpsData", "no
					// operation mobilephone");
					return fpsList;
				}

				String[] fpsSplit;
				int frameCount = thirdSpilt.length;
				int jank_count = 0;
				int vsync_count = 0;

				for (int i = 0; i < thirdSpilt.length; i++) {
					fpsSplit = thirdSpilt[i].split("\t");
					// 判断是否是4列数字，不是4列数字，直接跳过
					if (fpsSplit.length % 4 != 0) {
						System.out.println("handleFpsData fpsSplit.length % 4");
						// LoggerManager.logDebug(LOGTAG, "handleFpsData", "fps
						// count is wrong");
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

				fpsData = new FpsData(fps, jank_count, frameCount, activityName);
				System.out.println(fpsData);
				if (fpsList.contains(fpsData)) {
					int lastjankCount = fpsList.get(fpsList.indexOf(fpsData)).dropcount;
					int lastframeCount = fpsList.get(fpsList.indexOf(fpsData)).framecount;
					int lastfps = fpsList.get(fpsList.indexOf(fpsData)).fps;
					fpsData.fps = (fpsData.fps + lastfps) / 2;
					fpsData.framecount = (fpsData.framecount + lastframeCount) / 2;
					fpsData.dropcount = (fpsData.dropcount + lastjankCount) / 2;
				}

				fpsList.add(fpsData);
			}
		}
		return fpsList;
	}

	/**
	 * 获取对应包名的pid
	 * 
	 * @param packageName
	 * @return
	 */
	public static int getPid(String packageName) {
		if (CommonUtil.strIsNull(packageName)) {
			return 0;
		}
		String cmd = "ps | grep " + packageName;
		// String cmd = "ps";
		commandPidResult = ExecShellUtil.getInstance().execShellCommand(cmd);
		if (commandPidResult == null) {
			return 0;
		}

		if (commandPidResult.errorMsg != null && !"".equals(commandPidResult.errorMsg)) {
			LoggerManager.logError(LOGTAG, "getPid", commandPidResult.errorMsg);
			return 0;
		}

		if (commandPidResult.successMsg != null && commandPidResult.successMsg != "") {
			String[] lines = commandPidResult.successMsg.trim().split("\n");
			String result = CommonUtil.formatBlanksToBlank(lines[0].trim());
			String[] results = result.split(" ");
			if (results.length > 1) {
				return Integer.parseInt(results[1]);
			}

		}
		return 0;
	}
	
	/**
	 *  清理电量数据
	 */
	public static boolean clearBatteryData(){
		String enableBatteryCmd = "dumpsys batterystats --enable full-wake-history";
		String getBatteryCmd = "dumpsys batterystats --reset";
		boolean isTrue = false;
		
		try {
			ExecShellUtil.getInstance().execShellCommand(enableBatteryCmd, false);
			ExecShellUtil.getInstance().execShellCommand(getBatteryCmd, false);
			isTrue = true;
		} catch (Exception e) {
			// TODO: handle exception
			isTrue = false;
		}
		return isTrue;
	}
	
	/**
	 *  获取基本的测试信息
	 *  这里可能会有GlobalConfig没有设置的情况存在。
	 */
	public static BaseTestInfo getBaseTestInfo(String packageName) {
		if (CommonUtil.strIsNull(packageName)) {
			return null;
		}
		String cmd = "dumpsys package " + packageName;
		CommandResult packageInfo = ExecShellUtil.getInstance().execShellCommand(cmd, true);
		if (packageInfo != null && !CommonUtil.strIsNull(packageInfo.successMsg)) {
			Pattern pattern = Pattern.compile("versionName=(\\d.+)");
			Matcher matcher = pattern.matcher(packageInfo.successMsg);
			if (matcher.find()) {
				String result = matcher.group(0);
				result = result.split("=")[1];
				BaseTestInfo baseTestInfo = new BaseTestInfo(packageName, result);
				return baseTestInfo;
			}
		}
		
		return null;
	}
	/**
	 * 将timeStr转成毫秒
	 * 
	 * @param timeStr
	 * @return
	 */
	private static int formatStrToms(String timeStr) {
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
		CollectDataImpl.getCurActivity();
	}

}
