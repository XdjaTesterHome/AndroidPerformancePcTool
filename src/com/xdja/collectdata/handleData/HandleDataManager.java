package com.xdja.collectdata.handleData;

import java.util.ArrayList;
import java.util.List;

import com.xdja.collectdata.entity.FpsData;
import com.xdja.collectdata.entity.KpiData;
import com.xdja.collectdata.CollectDataImpl;
import com.xdja.collectdata.SaveEnvironmentManager;
import com.xdja.collectdata.entity.BatteryData;
import com.xdja.collectdata.entity.CpuData;
import com.xdja.collectdata.entity.MemoryData;
import com.xdja.collectdata.handleData.entity.BatteryHandleResult;
import com.xdja.collectdata.handleData.entity.CpuHandleResult;
import com.xdja.collectdata.handleData.entity.FlowHandleResult;
import com.xdja.collectdata.handleData.entity.FpsHandleResult;
import com.xdja.collectdata.handleData.entity.KpiHandleResult;
import com.xdja.collectdata.handleData.entity.MemoryHandleResult;
import com.xdja.constant.Constants;
import com.xdja.constant.GlobalConfig;
import com.xdja.util.CommonUtil;

/**
 * 问题模型：用于对上报的数据进行处理，判断是否存在问题。
 * 
 * @author zlw
 *
 */
public class HandleDataManager {
	private static HandleDataManager mInstance = null;
	// CPU相关配置常量
	private final static int CPU_MAX = 50;
	private final static int CPU_CONTINUE_MAX = 30;
	private final static float CPU_SLIENT_VALUE = 0.5f;
	private static int cpuCount = 0;
	private static int slientCount = 0;
	// 内存相关配置常量
	// 默认内存上下波动不超过2M
	private final static int MEMORY_STANDARD = 2;
	// 内存抖动不超过3次
	private final static int MEMORY_SHAKECOUNT = 4;
	// kpi相关配置
	// kpi数据超过多少判定有问题，单位是ms
	private final static int KPI_TIME = 2000;

	// flow相关配置 单位是MB
	private final static int FLOW_VALUE = 1024;
	private final static float FLOW_SLIENT_VALUE = 512;

	// fps相关配置
	private final static int FPS_COUNT = 40;

	// 存放错误数据列表
	private List<KpiHandleResult> kpiErrorList = new ArrayList<>(12);
	private List<CpuHandleResult> cpuErrorList = new ArrayList<>(12);
	private List<FlowHandleResult> flowErrorList = new ArrayList<>(12);
	private List<MemoryHandleResult> memoryErrorList = new ArrayList<>(12);

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

	// 用于判定内存是否存在抖动。
	private long memoryInterval = 30 * 1000;
	// 标记是否在测试内存
	private boolean memoryTestNow = false;
	// 用于存放10s内收集的memory数据
	private List<MemoryData> memoryList = new ArrayList<>(24);

	// 用于存放CPU数据
	private List<Float> cpuList = new ArrayList<Float>();
	private List<CpuData> slientCpuList = new ArrayList<>(4);
	// 用于存放kpi数据
	private List<KpiHandleResult> kpiList = new ArrayList<>(12);
	private List<KpiData> kpidataList = new ArrayList<>(12);

	// 声明处理数据结果的对象
	private MemoryHandleResult memoryResult = null;
	private KpiHandleResult mKpiHandleResult = null;
	private FpsHandleResult mFpsHandleResult = null;
	private FlowHandleResult mFlowHandleSlientResult = null;
	private FlowHandleResult mFlowHandleResult = null;
	private String mCurTestPackage;
	// 公共配置常量
	private long lastTime = 0;

	private long nowTime = 0;

	private HandleDataManager() {
		mCurTestPackage = GlobalConfig.getTestPackageName();
	}

	// cpuList列表中依次添加元素，直到添加长度为3的元素后，每次只更新列表元素，删除第一个和添加最后一个，列表长度适中为3//
	public void cpuList(CpuData cpuData) {
		cpuCount = cpuCount + 1; // 在采集频率的方法中添加逻辑判断计数器,添加计时器参数;这部分计数器也可以放在主线程循环中
		if (cpuCount > 10) {
			cpuCount = 1;
		}
		if (cpuCount == 10) { // 在主线程中添加逻辑判断，条件满足时执行相关方法
			if (cpuData != null) {
				int i = cpuList.size();
				if (i < 3) {
					cpuList.add(cpuData.cpuUsage);
				} else {
					cpuList.remove(0);
					cpuList.add(cpuData.cpuUsage);
				}
			}
		}
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
				if (Math.abs(nowData - nextData) > MEMORY_STANDARD) {
					num += 1;
				}
			}
		}
		return num;
	}

	/**
	 * 处理电量相关数据 问题模型：暂时还没确定，电量场景比较多，标准不好定
	 */
	public List<BatteryHandleResult> handleBatteryData(List<BatteryData> batteryDatas, String packageName,
			String version) {
		// 用于存放Battery数据
		List<BatteryHandleResult> batteryList = new ArrayList<>(12);
		BatteryHandleResult batteryHandleResult = null;
		if (batteryDatas == null || batteryDatas.size() < 1) {
			return batteryList;
		}

		for (BatteryData batteryData : batteryDatas) {
			batteryHandleResult = new BatteryHandleResult();
			batteryHandleResult.setResult(true);
			batteryHandleResult.setUid(batteryData.uid);
			batteryHandleResult.setTestValue(String.valueOf(batteryData.batteryValue));
			batteryHandleResult.setPackageName(packageName);
			batteryHandleResult.setVersion(version);
			batteryHandleResult.setAppPackageName(batteryData.appPackageName);
			batteryHandleResult.setDetailInfo(batteryData.detailInfo);
			batteryList.add(batteryHandleResult);
		}

		return batteryList;
	}

	/**
	 * handleCpu处理非静默测试的数据模型,原则上2点：其一大于50%；其二连续三次数据大于30%
	 * 
	 * @param cpuData
	 * @param cpu
	 * @return
	 */
	public CpuHandleResult handleCpu(CpuData cpuData, float cpu) {
		cpuList(cpuData);
		CpuHandleResult handleResult = new CpuHandleResult();
		handleResult.setTestValue(String.valueOf(CommonUtil.getTwoDots(cpu)));
		handleResult.setActivityName(CollectDataImpl.getCurActivity());
		if (cpuData.cpuUsage > CPU_MAX) {

			// 设置测试结果
			handleResult.setResult(false);
			// 判断错误列表中是否存在
			if (cpuErrorList.contains(handleResult)) {
				return handleResult;
			}

			// 设置需要显示错误信息
			handleResult.setShowErrorMsg(true);
			// 保存测试环境
			handleResult = saveCpuEnvironment(handleResult);

			// 不存在就添加到ErrorList中
			cpuErrorList.add(handleResult);

		} else {
			int i = cpuList.size();
			if (i == 3) {
				if (cpuList.get(0) > CPU_CONTINUE_MAX && cpuList.get(1) > CPU_CONTINUE_MAX
						&& cpuList.get(2) > CPU_CONTINUE_MAX) {
					// 设置测试结果
					handleResult.setResult(false);
					// 判断错误列表中是否存在
					if (cpuErrorList.contains(handleResult)) {
						return handleResult;
					}

					// 设置需要显示错误信息
					handleResult.setShowErrorMsg(true);
					// 保存测试环境
					handleResult = saveCpuEnvironment(handleResult);

					// 不存在就添加到ErrorList中
					cpuErrorList.add(handleResult);
				} else {
					// 设置测试结果
					handleResult.setResult(true);
					handleResult.setShowErrorMsg(false);
				}
			} else {
				// 设置测试结果
				handleResult.setResult(true);
				handleResult.setShowErrorMsg(false);
			}
		}
		return handleResult;
	}

	// 处理静默CPU数据，异常捕获模型，返回处理后认为有问题的数据,修改传入参数;//
	// 以下是对问题模型的处理，简单的问题模型，默认只要CPU的数据大于1%，就认为可能存在异常;//
	/**
	 * 逻辑待优化，因为判定cpu静默的逻辑需要优化
	 * 
	 * @param cpuData
	 * @param value
	 * @return
	 */
	public CpuHandleResult handleCpusilence(CpuData cpuData, float value) {
		if (cpuData == null) {
			return null;
		}

		// 静默测试数据的判断//
		CpuHandleResult upcpu = new CpuHandleResult(true);
		upcpu.setTestValue(String.valueOf(value));

		slientCount += 1;
		if (slientCount == 5) {
			slientCpuList.add(cpuData);
			slientCount = 0;
		}

		int valueCount = 0;
		if (slientCpuList.size() == 4) {
			for (CpuData cpuData2 : slientCpuList) {
				if (cpuData2.cpuUsage > CPU_SLIENT_VALUE) {
					valueCount += 1;
				}
			}

			slientCpuList.clear();
		}

		/**
		 * 超过三次就认为是有耗时行为
		 */
		if (valueCount >= 3) {
			// 设置测试结果
			upcpu.setResult(false);
			// 判断错误列表中是否存在
			if (cpuErrorList.contains(upcpu)) {
				return upcpu;
			}

			// 设置需要显示错误信息
			upcpu.setShowErrorMsg(true);
			// 保存测试环境
			upcpu = saveCpuEnvironment(upcpu);

			// 不存在就添加到ErrorList中
			cpuErrorList.add(upcpu);
		} else {
			// 设置测试结果
			upcpu.setResult(true);
			upcpu.setShowErrorMsg(false);
		}

		return upcpu;
	}

	/**
	 * 处理流量数据
	 * 
	 * @param flowData
	 * @return
	 */
	public FlowHandleResult handleFlowData(float flowData) {
		mFlowHandleResult = new FlowHandleResult();
		// 设置公共的值
		mFlowHandleResult.setActivityName(CollectDataImpl.getCurActivity());
		mFlowHandleResult.setTestValue(String.valueOf(flowData));
		if (flowData > FLOW_VALUE) {

			mFlowHandleResult.setResult(false);
			if (flowErrorList.contains(mFlowHandleResult)) {
				return mFlowHandleResult;
			}

			// 保存测试环境
			String logPath = SaveEnvironmentManager.getInstance().saveCurrentLog(GlobalConfig.DeviceName,
					mCurTestPackage, Constants.TYPE_FLOW);
			mFlowHandleSlientResult.setLogPath(logPath);

			// 设置显示错误信息
			mFlowHandleResult.setShowErrorMsg(true);
			flowErrorList.add(mFlowHandleResult);

			return mFlowHandleResult;
		}

		mFlowHandleResult.setResult(true);
		return mFlowHandleResult;
	}

	/**
	 * 对于静默测试时，测试流量场景 和正常测试分开，考虑之后的扩展
	 * 
	 * @return
	 */
	public FlowHandleResult handleFlowSlientData(float flowData) {
		mFlowHandleResult = new FlowHandleResult();
		// 设置公共的值
		mFlowHandleResult.setActivityName(CollectDataImpl.getCurActivity());
		mFlowHandleResult.setTestValue(String.valueOf(flowData));
		if (flowData > FLOW_SLIENT_VALUE) {

			mFlowHandleResult.setResult(false);
			if (flowErrorList.contains(mFlowHandleResult)) {
				return mFlowHandleResult;
			}

			// 保存测试环境
			String logPath = SaveEnvironmentManager.getInstance().saveCurrentLog(GlobalConfig.DeviceName,
					mCurTestPackage, Constants.TYPE_FLOW);
			mFlowHandleSlientResult.setLogPath(logPath);

			// 设置显示错误信息
			mFlowHandleResult.setShowErrorMsg(true);
			flowErrorList.add(mFlowHandleResult);

			return mFlowHandleResult;
		}

		mFlowHandleResult.setResult(true);
		return mFlowHandleResult;
	}

	/**
	 * 对采集到的fps数据进行处理 问题判定标准： 当fps < 40
	 * 
	 * @param cpuData
	 * @return
	 */
	public FpsHandleResult handleFpsData(FpsData fpsData) {
		if (fpsData == null) {
			return null;
		}
		// 创建一个结果对象
		mFpsHandleResult = new FpsHandleResult();
		mFpsHandleResult.setActivityName(fpsData.activityName);
		mFpsHandleResult.setTestValue(String.valueOf(fpsData.fps));
		mFpsHandleResult.setDropCount(fpsData.dropcount);
		mFpsHandleResult.setFrameCount(fpsData.framecount);
		mFpsHandleResult.setPackageName(GlobalConfig.TestPackageName);
		mFpsHandleResult.setVersion(GlobalConfig.TestVersion);

		// 判断是否有问题
		if (fpsData.fps < FPS_COUNT) {
			mFpsHandleResult.setResult(false);
		} else {
			mFpsHandleResult.setResult(true);
		}
		return mFpsHandleResult;
	}

	/**
	 * 处理kpi相关的数据 判断问题的标准： 页面加载时间大于2s，但是暂时还没有区分首次启动、冷启动
	 * 这个方法按照目前的方法效率低。但貌似没好的解决方案了。
	 * @param cpuData
	 * @return
	 */
	public List<KpiHandleResult> handleKpiData(List<KpiData> KpiDatas) {
		// 首先将所有的值都记录下来
		kpidataList.addAll(KpiDatas);
		// 过滤重复的项
		KpiDatas = handleKpiHandleList(kpidataList);
		
		if (KpiDatas == null || KpiDatas.size() < 1) {
			return kpiList;
		}
		
		for (KpiData kpiData : KpiDatas) {
			mKpiHandleResult = new KpiHandleResult();
			mKpiHandleResult.setActivityName(kpiData.currentPage);
			mKpiHandleResult.setTestValue(String.valueOf(kpiData.loadTime));
			// 判断是否有问题
			if (kpiData.loadTime > KPI_TIME) {
				// 记录是否展示错误信息
				if (kpiErrorList.contains(mKpiHandleResult)) {
					continue;
				}
				mKpiHandleResult.setShowErrorMsg(true);
				kpiErrorList.add(mKpiHandleResult);

				// 保存log
				String logPath = SaveEnvironmentManager.getInstance().saveCurrentLog(GlobalConfig.DeviceName,
						mCurTestPackage, Constants.TYPE_KPI);
				// 保存method trace
				mKpiHandleResult.setLogPath(logPath);
				mKpiHandleResult.setResult(false);
				mKpiHandleResult.setMethodTracePath("");

			} else {
				mKpiHandleResult.setResult(true);
			}
			
			// 若存在，则直接修改值
			if (kpiList.contains(mKpiHandleResult)) {
				kpiList.set(kpiList.indexOf(mKpiHandleResult), mKpiHandleResult);
			} else {
				kpiList.add(mKpiHandleResult);
			}
		}

		return kpiList;
	}

	/**
	 * 处理结果列表中重复的元素，取平均值
	 * 
	 */
	private List<KpiData> handleKpiHandleList(List<KpiData> KpiDatas) {
		if (KpiDatas == null || KpiDatas.size() < 1) {
			return null;
		}
		KpiData kpiData = null, kpiData2 = null;
		List<KpiData> tempKpiData = new ArrayList<>(12);
		int count = 1;
		int kpi = 0;
		for (int i = 0; i < KpiDatas.size(); i++) {
			kpiData = KpiDatas.get(i);
			if (tempKpiData.contains(kpiData)) {
				continue;
			}
			if (kpiData == null) {
				continue;
			}
			kpi += kpiData.loadTime;

			for (int j = i + 1; j < KpiDatas.size(); j++) {
				kpiData2 = KpiDatas.get(j);
				if (kpiData2 == null) {
					continue;
				}

				if (kpiData.equals(kpiData2)) {
					count += 1;
					kpi += kpiData2.loadTime;
					KpiDatas.remove(kpiData2);
				}
				
			}
			kpi = kpi / count;
			kpiData.setLoadTime(kpi);
			if (!tempKpiData.contains(kpiData)) {
				tempKpiData.add(kpiData);
			}
			kpi = 0;
			count = 1;
		}
		
		
		return tempKpiData;
	}

	/**
	 * 处理得到的内存数据 内存的问题暂时有如下几种 待扩展： 一、对当前版本的内存数据进行判定
	 * 1.内存抖动。（暂定的标准是10s内超过2次内存波动），我们只记录判定为内存抖动时的页面。
	 * 2.内存泄露。通过工具不太好判定内存泄露，准备用LeakCanary + monkey跑
	 * 
	 * 二、版本间进行数据对比 1.每次启动应用后，Heap内存相比之前版本稳定增长。这通常是因为增加了新的功能或者代码造成的。
	 * 2.对比版本数据，Heap Alloc的变化不大，但进程的Dalvik Heap pss
	 * 内存明显增加，这主要是因为分配了大量小对象造成的内存碎片。 上面两种情况，暂时还不能抽象
	 * 
	 * 修改，采集数据的时间间隔换成5s。内存泄露数据采集改为30s
	 * 
	 * @param cpuData
	 * @return null 就跳过这个结果数据不处理
	 */
	public MemoryHandleResult handleMemoryData(MemoryData memoryData, float value) {
		if (memoryData == null) {
			return null;
		}
		if (!memoryTestNow) {
			lastTime = System.currentTimeMillis();
			memoryTestNow = true;
		}

		nowTime = System.currentTimeMillis();
		memoryResult = new MemoryHandleResult();
		// 设置公共的值
		memoryResult.setTestValue(String.valueOf(CommonUtil.getTwoDots(value)));
		memoryResult.setActivityName(CollectDataImpl.getCurActivity());
		/**
		 * 如果超过30s，清空条件，重新开始。
		 */
		if (nowTime - lastTime > memoryInterval) {
			memoryTestNow = false;
			lastTime = 0;
			nowTime = 0;
			int shakeCount = getShakeCount();
			memoryList.clear();
			if (shakeCount > MEMORY_SHAKECOUNT) {

				// 设置结果为false
				memoryResult.setResult(false);
				if (memoryErrorList.contains(memoryResult)) {
					return memoryResult;
				}
				
				// 设置显示错误信息
				memoryResult.setShowErrorMsg(true);
				// 保存测试环境
				memoryResult = saveMemoryEnvironment(memoryResult);
				// 将结果放到错误列表中
				memoryErrorList.add(memoryResult);
				
				return memoryResult;
			}
		} else {
			// 设置显示错误信息
			memoryResult.setShowErrorMsg(false);
			memoryResult.setResult(true);
			memoryList.add(memoryData);
		}

		return memoryResult;
	}

	/***
	 * cpu测试保存测试环境
	 * 
	 * @param result
	 */
	private CpuHandleResult saveCpuEnvironment(CpuHandleResult handleResult) {
		// String screenshotsPath =
		// SaveEnvironmentManager.getInstance().screenShots(GlobalConfig.DeviceName,
		// Constants.TYPE_CPU);
		String logPath = SaveEnvironmentManager.getInstance().saveCurrentLog(GlobalConfig.DeviceName, mCurTestPackage,
				Constants.TYPE_CPU);
		// 暂时不抓取method trace，会影响采集数据
		// String methodTrace =
		// SaveEnvironmentManager.getInstance().methodTracing(GlobalConfig.DeviceName,
		// GlobalConfig.PackageName, Constants.TYPE_CPU);
		handleResult.setLogPath(logPath);
		handleResult.setMethodTracePath("");
		return handleResult;
	}

	/**
	 * memory测试保存结果
	 * 
	 * @param result
	 * @return
	 */
	private MemoryHandleResult saveMemoryEnvironment(MemoryHandleResult memoryResult) {
		// dumpsys memory
		// String filePath =
		// SaveEnvironmentManager.getInstance().dumpMemory(GlobalConfig.DeviceName,
		// GlobalConfig.PackageName, Constants.TYPE_MEMORY);
		String logPath = SaveEnvironmentManager.getInstance().saveCurrentLog(GlobalConfig.DeviceName, mCurTestPackage,
				Constants.TYPE_MEMORY);
		// String screenPath =
		// SaveEnvironmentManager.getInstance().screenShots(GlobalConfig.DeviceName,
		// Constants.TYPE_MEMORY);

		memoryResult.setMemoryHprofPath("");
		memoryResult.setMethodTracePath("");
		memoryResult.setLogPath(logPath);

		return memoryResult;
	}
	
	/**
	 *  清空测试数据
	 */
	public void destoryData(){
		if (kpiErrorList != null) {
			kpiErrorList.clear();
		}
		
		if (cpuErrorList != null) {
			cpuErrorList.clear();
		}
		
		if (flowErrorList != null) {
			flowErrorList.clear();
		}
		if (memoryList != null) {
			memoryErrorList.clear();
		}
		if (memoryErrorList != null) {
			memoryErrorList.clear();
		}
		
		if (cpuList != null) {
			cpuList.clear();
		}
		
		if (kpiList != null) {
			kpiList.clear();
		}
		
		if (slientCpuList != null) {
			slientCpuList.clear();
		}
		
		if (kpidataList != null) {
			kpidataList.clear();
		}
	}
}
