package com.xdja.collectdata.handleData;

import java.util.ArrayList;
import java.util.List;

import com.xdja.collectdata.entity.FlowData;
import com.xdja.collectdata.entity.FpsData;
import com.xdja.collectdata.entity.KpiData;
import com.xdja.collectdata.CollectDataImpl;
import com.xdja.collectdata.SaveEnvironmentManager;
import com.xdja.collectdata.entity.CpuData;
import com.xdja.collectdata.entity.MemoryData;
import com.xdja.collectdata.handleData.HandleDataResult;
import com.xdja.constant.Constants;
import com.xdja.constant.GlobalConfig;

/**
 * 问题模型：用于对上报的数据进行处理，判断是否存在问题。
 * 
 * @author zlw
 *
 */
public class HandleDataManager {
	private static HandleDataManager mInstance = null;
	// 用于判定内存是否存在抖动。
	private long memoryInterval = 10 * 1000;
	// 标记是否在测试内存
	private boolean memoryTestNow = false;
	// 用于存放10s内收集的memory数据
	private List<MemoryData> memoryList = new ArrayList<>(24);
	private HandleDataResult memoryResult = null;
	private List<Float> cpuList = new ArrayList<Float>();// 用于存放CPU数据

	private HandleDataManager() {

	}

	public static HandleDataManager getInstance() {
		if (mInstance == null) {
			synchronized (HandleDataManager.class) {
				if (mInstance == null) {
					mInstance = new HandleDataManager();
				}
			}
		}
		return mInstance;
	}

	// 处理静默CPU数据，异常捕获模型，返回处理后认为有问题的数据,修改传入参数;//
	// 以下是对问题模型的处理，简单的问题模型，默认只要CPU的数据大于1%，就认为可能存在异常;//
	public HandleDataResult handleCpusilence(CpuData cpuData) {
		// 静默测试数据的判断//
		boolean result;
		HandleDataResult upcpu = null;
		if (cpuData.cpuUsage > 1) {
			upcpu = saveCpuEnvironment(false);
		} else {
			result = true;
			upcpu = new HandleDataResult(result);
		}

		return upcpu;
	}

	// handleCpu处理非静默测试的数据模型,原则上2点：其一大于50%；其二连续三次数据大于30%//
	public HandleDataResult handleCpu(CpuData cpuData) {
		// 静默测试数据的判断//
		boolean result;
		HandleDataResult upcpu = null;
		cpuList(cpuData);
		if (cpuData.cpuUsage > 50) {
			upcpu = saveCpuEnvironment(false);
		} else {
			int i = cpuList.size();
			if (i == 3) {
				if (cpuList.get(0) > 30 && cpuList.get(1) > 30 && cpuList.get(2) > 30) {
					upcpu = saveCpuEnvironment(false);
				} else {
					result = true;
					upcpu = new HandleDataResult(result);
				}
			}

		}

		return upcpu;
	}

	/***
	 * cpu测试保存测试环境
	 * 
	 * @param result
	 */
	private HandleDataResult saveCpuEnvironment(boolean result) {
		String activityName = CollectDataImpl.getCurActivity();
		String screenshotsPath = SaveEnvironmentManager.getInstance().screenShots(GlobalConfig.DeviceName,
				Constants.TYPE_CPU);
		String logPath = SaveEnvironmentManager.getInstance().saveCurrentLog(GlobalConfig.DeviceName,
				GlobalConfig.PackageName, Constants.TYPE_CPU);
		String methodTrace = SaveEnvironmentManager.getInstance().methodTracing(GlobalConfig.DeviceName,
				GlobalConfig.PackageName, Constants.TYPE_CPU);
		HandleDataResult handleResult = new HandleDataResult(result);
		handleResult.setActivityName(activityName);
		handleResult.setLogPath(logPath);
		handleResult.setMethodTracePath(methodTrace);
		handleResult.setScreenshotsPath(screenshotsPath);
		return handleResult;
	}

	// cpuList列表中依次添加元素，直到添加长度为3的元素后，每次只更新列表元素，删除第一个和添加最后一个，列表长度适中为3//
	public List<Float> cpuList(CpuData cpuData) {
		if (cpuData != null) {
			int i = cpuList.size();
			if (i < 3) {
				cpuList.add(cpuData.cpuUsage);
			} else {
				cpuList.remove(0);
				cpuList.add(cpuData.cpuUsage);
			}
		}
		return cpuList;
	}

	public HandleDataResult handleFlowData(FlowData flowData) {
		return null;
	}

	/**
	 * 
	 * @param cpuData
	 * @return
	 */
	public HandleDataResult handleFpsData(FpsData fpsData) {
		return null;
	}

	/**
	 * 
	 * @param cpuData
	 * @return
	 */
	public HandleDataResult handleKpiData(KpiData kpiData) {
		return null;
	}

	/**
	 * 处理得到的内存数据 内存的问题暂时有如下几种 待扩展： 一、对当前版本的内存数据进行判定
	 * 1.内存抖动。（暂定的标准是10s内超过5次内存波动），我们只记录判定为内存抖动时的页面。
	 * 2.内存泄露。通过工具不太好判定内存泄露，准备用LeakCanary + monkey跑
	 * 
	 * 二、版本间进行数据对比 1.每次启动应用后，Heap内存相比之前版本稳定增长。这通常是因为增加了新的功能或者代码造成的。
	 * 2.对比版本数据，Heap Alloc的变化不大，但进程的Dalvik Heap pss
	 * 内存明显增加，这主要是因为分配了大量小对象造成的内存碎片。 上面两种情况，暂时还不能抽象
	 * 
	 * 
	 * @param cpuData
	 * @return null 就跳过这个结果数据不处理
	 */
	public HandleDataResult handleMemoryData(MemoryData memoryData) {
		if (memoryData == null) {
			return null;
		}
		long lastTime = 0;
		long nowTime = 0;
		if (!memoryTestNow) {
			lastTime = System.currentTimeMillis();
			memoryTestNow = true;
		}

		nowTime = System.currentTimeMillis();
		/**
		 * 如果超过10s，清空条件，重新开始。
		 */
		if (nowTime - lastTime > memoryInterval) {
			memoryTestNow = false;
			lastTime = 0;
			nowTime = 0;
			int shakeCount = getShakeCount();
			if (shakeCount > 5) {
				memoryResult = saveMemoryEnvironment(false);
				return memoryResult;
			}
			memoryList.clear();
		} else {
			memoryList.add(memoryData);
		}
		memoryResult = new HandleDataResult(true);
		return memoryResult;
	}
	
	/**
	 * memory测试保存结果
	 * @param result
	 * @return
	 */
	private HandleDataResult saveMemoryEnvironment(boolean result){
		HandleDataResult memoryResult = new HandleDataResult(false);
		// dumpsys memory
		String filePath = SaveEnvironmentManager.getInstance().dumpMemory(GlobalConfig.DeviceName,
				GlobalConfig.PackageName, Constants.TYPE_MEMORY);
		String logPath = SaveEnvironmentManager.getInstance().saveCurrentLog(GlobalConfig.DeviceName,
				GlobalConfig.PackageName, Constants.TYPE_MEMORY);
		String screenPath = SaveEnvironmentManager.getInstance().screenShots(GlobalConfig.DeviceName, Constants.TYPE_MEMORY);
		memoryResult.setMemoryTracePath(filePath);
		memoryResult.setLogPath(logPath);
		memoryResult.setScreenshotsPath(screenPath);
		
		return memoryResult;
	}
	/**
	 * 获取shakeCount的次数
	 * 
	 * @return
	 */
	private int getShakeCount() {
		int num = 0;
		if (memoryList.size() > 1) {
			for (int i = 0; i < memoryList.size() - 1; i++) {
				float nowData = memoryList.get(i).memAlloc;
				float nextData = memoryList.get(i + 1).memAlloc;
				// 这里的单位都是M
				if (Math.abs(nowData - nextData) > 2) {
					num += 1;
				}
			}
		}
		return num;
	}

}
