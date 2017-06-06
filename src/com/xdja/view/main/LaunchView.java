package com.xdja.view.main;

import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeSet;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import org.apache.log4j.Logger;

import com.android.ddmlib.AndroidDebugBridge;
import com.android.ddmlib.AndroidDebugBridge.IDeviceChangeListener;
import com.android.ddmlib.IDevice;
import com.xdja.adb.AdbManager;
import com.xdja.adb.AndroidDevice;
import com.xdja.collectdata.CollectDataImpl;
import com.xdja.collectdata.entity.BaseTestInfo;
import com.xdja.collectdata.handleData.HandleDataManager;
import com.xdja.collectdata.handleData.entity.CpuHandleResult;
import com.xdja.collectdata.handleData.entity.FlowHandleResult;
import com.xdja.collectdata.handleData.entity.KpiHandleResult;
import com.xdja.collectdata.handleData.entity.MemoryHandleResult;
import com.xdja.constant.Constants;
import com.xdja.constant.GlobalConfig;
import com.xdja.database.PerformanceDB;
import com.xdja.database.SaveLocalManager;
import com.xdja.exception.SettingException;
import com.xdja.log.LoggerManager;
import com.xdja.util.CommonUtil;
import com.xdja.util.ExecShellUtil;
import com.xdja.util.ProPertiesUtil;
import com.xdja.util.SwingUiUtil;
import com.xdja.util.SwingUiUtil.ClickDialogBtnListener;
import com.xdja.view.PerformanceSettingDialog;
import com.xdja.view.SaveTestDataSettingDialog;
import com.xdja.view.ToolsView;
import com.xdja.view.chart.BatteryView;
import com.xdja.view.chart.CpuView;
import com.xdja.view.chart.FlowView;
import com.xdja.view.chart.FpsView;
import com.xdja.view.chart.KpiTestView;
import com.xdja.view.chart.MemoryView;

public class LaunchView extends JFrame implements IDeviceChangeListener {

	/**
	 * serial Version UID is auto generated
	 */
	private static final long serialVersionUID = 7845872493970114091L;
	private Logger logger = Logger.getLogger(LaunchView.class);
	private String author;
	private JPanel frame;
	private static JButton jb1, jb2, slientBtn;
	private MemoryView viewMemory;
	private FlowView viewFlow;
	private CpuView viewCpu;
	private KpiTestView kpiTestView;
	private FpsView viewFps;
	private BatteryView viewBattery;
	private ToolsView toolsView;
	private static JComboBox<String> comboDevices;
	private static JComboBox<String> comboProcess;
	private JTabbedPane jTabbedPane = new JTabbedPane();
	private String[] tabNames = { "   内    存   ", "     cpu    ", "   电   量   ", "    加载时间     ", "   帧   率   ",
			"   流   量   ", "    实用工具     " };
	private final static int WIDTH = 1248;
	private final static int HEIGHT = 760;
	// 静默测试时，过十分钟之后再采集数据
	private final static int SLIENT_TIME_INTERVAL = 5 * 1000;
	private Timer mSlientWaitTimer = null;
	// 当前选择的测试包名
	private String mCurTestPackageName;
	// 设置在更新设备的时候，不处理进程列表的点击事件
	public volatile boolean myIgnoreActionEvents = false;

	/**
	 * constructor to init a LaunchView instance create a JPanel instance to put
	 * other controller parts
	 *
	 * @param name:
	 *            author name
	 */
	public LaunchView() {
		this.author = Constants.PRODUCT_NAME;
		this.frame = new JPanel();
		String version = ProPertiesUtil.getInstance().getProperties(Constants.TOOLSVERSION);
		setTitle(String.format("%s %s", author, version));
		setBounds(100, 50, WIDTH, HEIGHT);
		createTopMenu();
		add(frame);
		AndroidDebugBridge.addDeviceChangeListener(this);
	}

	/**
	 * constructor to init a LaunchView instance create a JPanel instance to put
	 * other controller parts 用于创建一个父控件来摆放其他的控件，这里是用来显示其他的测试项
	 */
	public void createParts() {
		// 必须显式设置布局格式为空，否则不会按照我们设置好的格式布局
		frame.setLayout(null);
		// combo box to select device sn
		comboDevices = new JComboBox<String>();
		frame.add(comboDevices);
		Rectangle rect = new Rectangle(0, 0, 300, 30);// 设定绝对位置
		comboDevices.setBounds(rect);// 添加位置
		comboDevices.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// comboProcess.removeAllItems();
				if (myIgnoreActionEvents) {
					return;
				}
				updateClientList();
			}
		});

		// 用于展示设备的进程
		comboProcess = new JComboBox<String>();
		frame.add(comboProcess);
		Rectangle rectProcess = new Rectangle(320, 0, 420, 30);
		comboProcess.setBounds(rectProcess);
		comboProcess.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				if (myIgnoreActionEvents) {
					return;
				}
				// TODO Auto-generated method stub
				String packageName = (String) comboProcess.getSelectedItem();

				if (!CommonUtil.strIsNull(packageName)) {
					mCurTestPackageName = packageName;
					GlobalConfig.TestPackageName = packageName;
					BaseTestInfo baseTestInfo = CollectDataImpl.getBaseTestInfo(packageName);
					if (baseTestInfo != null) {
						GlobalConfig.TestVersion = baseTestInfo.versionName;
					}
					// 将选择的包名记录到本地
					ProPertiesUtil.getInstance().writeProperties(Constants.CHOOSE_PACKAGE, packageName);
				}

			}
		});
		// 开始监控按钮
		jb1 = new JButton("开始监控");
		Rectangle rectjb1 = new Rectangle(800, 0, 100, 30);
		frame.add(jb1);
		jb1.setBounds(rectjb1);
		jb1.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Thread thread = new Thread(new Runnable() {

					@Override
					public void run() {
						// TODO Auto-generated method stub
						jb2.setEnabled(true);
						jb1.setEnabled(false);
						slientBtn.setEnabled(false);
						if (viewFps != null) {
							viewFps.setBtnEnable(false);
						}
						if (viewBattery != null) {
							viewBattery.setBtnEnable(true);
						}
						comboDevices.setEnabled(false);
						comboProcess.setEnabled(false);
						startTest();
					}
				});

				thread.start();
			}

		});

		// 停止监控按钮
		jb2 = new JButton("停止监控");
		Rectangle rectjb2 = new Rectangle(920, 0, 100, 30);
		frame.add(jb2);
		jb2.setBounds(rectjb2);
		jb2.setEnabled(false);

		jb2.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Thread thread = new Thread(new Runnable() {

					@Override
					public void run() {
						// TODO Auto-generated method stub
						jb1.setEnabled(true);
						jb2.setEnabled(false);
						slientBtn.setEnabled(true);
						comboDevices.setEnabled(true);
						comboProcess.setEnabled(true);
						if (viewFps != null) {
							viewFps.setBtnEnable(true);
						}
						if (viewBattery != null) {
							viewBattery.setEnabled(true);
						}
						HandleDataManager.getInstance().destoryData();
						stopTest();
					}
				});

				thread.start();
			}
		});

		// 静默测试
		slientBtn = new JButton("静默测试");
		Rectangle slientRect = new Rectangle(1040, 0, 120, 30);
		frame.add(slientBtn);
		slientBtn.setBounds(slientRect);
		slientBtn.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				String cmd = e.getActionCommand();
				switch (cmd) {
					case "开始静默测试":
						Thread startThread = new Thread(new Runnable() {

							@Override
							public void run() {
								// TODO Auto-generated method stub
								jb1.setEnabled(false);
								jb2.setEnabled(false);
								comboDevices.setEnabled(false);
								comboProcess.setEnabled(false);
								slientBtn.setText("停止静默测试");
								startSlientTest();
								if (viewFps != null) {
									viewFps.setBtnEnable(false);
								}

								SwingUiUtil.getInstance().showTipsDialog(LaunchView.this, "提示", "静默测试，5分钟后开始采集数据", "我知道了",
										null);
							}
						});

						startThread.start();
						break;
					case "停止静默测试":
						Thread stopThread = new Thread(new Runnable() {

							@Override
							public void run() {
								// TODO Auto-generated method stub
								jb1.setEnabled(true);
								jb2.setEnabled(true);
								comboDevices.setEnabled(true);
								comboProcess.setEnabled(true);
								slientBtn.setText("开始静默测试");
								stopSlientTest();
								if (viewFps != null) {
									viewFps.setBtnEnable(true);
								}
								HandleDataManager.getInstance().destoryData();
							}
						});

						stopThread.start();
						break;
					default:
						break;
				}
			}
		});

		layoutTabComponents();
	}

	/**
	 * 开启静默测试 静默测试目前只针对CPU 和 Flow 两种类型的数据
	 */
	private void startSlientTest() {
		mSlientWaitTimer =new Timer();
		mSlientWaitTimer.schedule(new TimerTask() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				// 让设备进入静默状态
				AdbManager.getInstance().makeDeviceSlient();

				if (viewCpu != null) {
					viewCpu.setSlientTest(true);
					viewCpu.start(mCurTestPackageName);
				}

				if (viewFlow != null) {
					viewFlow.setSlient(true);
					viewFlow.start(mCurTestPackageName);
				}
			}
		}, SLIENT_TIME_INTERVAL);

		mSlientWaitTimer = null;
	}

	/**
	 * 停止静默测试
	 */
	private void stopSlientTest() {
		if (mSlientWaitTimer != null) {
			mSlientWaitTimer.cancel();
			mSlientWaitTimer = null;
		}

		if (viewCpu != null) {
			viewCpu.stop();
		}

		if (viewFlow != null) {
			viewFlow.stop();
		}

		saveSilentData();
	}

	// 保存静默数据
	private void saveSilentData() {
		String saveWay = ProPertiesUtil.getInstance().getProperties(Constants.DBSAVE_CHOOSE);
		if ("true".equals(saveWay)) {
			saveSlientDataToDb();

			// 关闭数据库
			PerformanceDB.getInstance().closeDB();
		} else {
			saveSilentDataToLocal();
		}
	}

	/**
	 * 保存静默数据到本地
	 */
	private void saveSilentDataToLocal() {
		SaveLocalManager.getInstance().setTestPackageAndVersion(GlobalConfig.TestPackageName,
				GlobalConfig.TestVersion);

		try {
			if (viewCpu != null) {
				SaveLocalManager.getInstance().saveSilentCpuDataToLocal(viewCpu.getHandleResult());
			}

			if (viewFlow != null) {
				SaveLocalManager.getInstance().saveSilentFlowDataToLocal(viewFlow.getHandleResultList());
			}
		} catch (SettingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * 将静态数据保存到数据库中
	 */
	private void saveSlientDataToDb() {
		saveCommonData(mCurTestPackageName);
		if (viewCpu != null) {
			List<CpuHandleResult> handleSlientList = viewCpu.getHandleResult();
			PerformanceDB.getInstance().insertSlientCpuData(handleSlientList);
		}

		if (viewFlow != null) {
			List<FlowHandleResult> handleFlowList = viewFlow.getHandleResultList();
			PerformanceDB.getInstance().insertSlientFlowData(handleFlowList);
		}
	}

	/**
	 * 用来摆放tab的组件 tabNames = {"内存", "cpu", "电量", "kpitest", "帧率", "流量"};
	 */
	private void layoutTabComponents() {
		Rectangle rect = new Rectangle(100, 100, 600, 200);
		// 1.内存
		// memory chart view
		viewMemory = new MemoryView(Constants.MEMORY, Constants.MEMORYContent, Constants.MEMORY_UNIT);
		viewMemory.setBounds(rect);
		jTabbedPane.addTab(tabNames[0], viewMemory);

		// 2.cpu
		viewCpu = new CpuView(Constants.CPU, Constants.CPU, Constants.CPU_UNIT);
		viewCpu.setBounds(rect);
		jTabbedPane.addTab(tabNames[1], viewCpu);

		// 3.kpiTest
		kpiTestView = new KpiTestView(Constants.KPI, Constants.KPITITLE, Constants.KPI);
		kpiTestView.setBounds(rect);
		jTabbedPane.addTab(tabNames[3], kpiTestView);

		// 4.流量
		viewFlow = new FlowView(Constants.FLOW, Constants.FLOW, Constants.FLOW_UNIT);
		viewFlow.setBounds(rect);
		jTabbedPane.addTab(tabNames[5], viewFlow);

		// 5.帧率
		viewFps = new FpsView(Constants.FPS, Constants.FPSTITLE, Constants.FPS_UNIT);
		viewFps.setBounds(rect);
		jTabbedPane.addTab(tabNames[4], viewFps);

		// 6.电量
		viewBattery = new BatteryView(Constants.BATTERY, Constants.BATTERY, Constants.BATTERY_UNIT);
		viewBattery.setBounds(rect);
		jTabbedPane.addTab(tabNames[2], viewBattery);

		// 实用工具
		toolsView = new ToolsView();
		toolsView.setBounds(rect);
		jTabbedPane.addTab(tabNames[6], toolsView);

		frame.add(jTabbedPane);

		rect = new Rectangle(20, 100, WIDTH - 50, 550);
		jTabbedPane.setBounds(rect);
	}

	/**
	 * add action listener for all the controller parts
	 */
	public void addActionListener() {

		// 主窗口添加关闭监听器
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				showExitDialog();
			}
		});
	}

	/**
	 * 初始化logger
	 */
	private void initLogger() {
		// initial Logger Manager to use log4j
		if (!LoggerManager.isInited()) {
			LoggerManager.initLogger();
		}
		logger.info("LoggerManager is inited successfully!");
	}

	/**
	 * 获取选中的device
	 *
	 * @return
	 */
	public static String getdevice() {
		String devicename = "";
		if (comboDevices != null) {
			devicename = (String) comboDevices.getSelectedItem();
		}
		GlobalConfig.DeviceName = devicename;
		return devicename;

	}

	private void showExitDialog() {
		SwingUiUtil.getInstance().showOkAndCancelDialog(this, "提示", "真想退出吗？", "确定", "取消", new ClickDialogBtnListener() {

			@Override
			public void clickOkBtn() {
				// TODO Auto-generated method stub
				logger.info("program is exited!");
				setDefaultCloseOperation(EXIT_ON_CLOSE);
				destoryViewData();
				handleExitSaveData();
			}

			@Override
			public void clickCancelBtn() {
				// TODO Auto-generated method stub
				setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE); // 这个是关键
			}
		});
	}

	/**
	 * 当退出程序或者手机设备断开连接时， 保存测试数据，避免白测的情况出现
	 */
	private void handleExitSaveData() {
		if (isNormalTestNow()) {
			stopTest();
		}

		if (isSlientTestNow()) {
			stopSlientTest();
		}

		if (isFpsTestNow()) {
			if (viewFps != null) {
				viewFps.stop();
				viewFps.saveDataToDb();
			}
		}
	}

	private void startTest() {
		if (viewCpu != null) {
			viewCpu.setSlientTest(false);
			viewCpu.start(mCurTestPackageName);
		}

		if (viewMemory != null) {
			viewMemory.start(mCurTestPackageName);
		}

		if (viewFlow != null) {
			viewFlow.setSlient(false);
			viewFlow.start(mCurTestPackageName);
		}

		if (kpiTestView != null) {
			try {
				kpiTestView.start(mCurTestPackageName);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	/**
	 * 结束测试
	 */
	private void stopTest() {
		if (viewCpu != null) {
			viewCpu.stop();
		}

		if (viewMemory != null) {
			viewMemory.stop();
		}

		if (viewFlow != null) {
			viewFlow.stop();
		}

		if (kpiTestView != null) {
			kpiTestView.stop();
		}

		saveData();
	}

	/**
	 * 保存数据
	 */
	private void saveData() {
		String saveWay = ProPertiesUtil.getInstance().getProperties(Constants.DBSAVE_CHOOSE);
		if ("true".equals(saveWay)) {
			// 将数据保存到数据库中
			saveDataToDB();

			PerformanceDB.getInstance().closeDB();
		} else {
			saveDataToLocal();
		}
	}

	/**
	 * 将测试数据保存到本地
	 *
	 */
	private void saveDataToLocal() {
		SaveLocalManager.getInstance().setTestPackageAndVersion(GlobalConfig.TestPackageName,
				GlobalConfig.TestVersion);

		try {
			if (viewMemory != null) {
				SaveLocalManager.getInstance().saveMemoryDataToLocal(viewMemory.getHandleResult());
			}

			if (viewCpu != null) {
				SaveLocalManager.getInstance().saveCpuDataToLocal(viewCpu.getHandleResult());
			}

			if (viewFlow != null) {
				SaveLocalManager.getInstance().saveFlowDataToLocal(viewFlow.getHanResultList());
			}

			if (kpiTestView != null) {
				List<KpiHandleResult> kpiHandleResults = kpiTestView.getHandleKpiList();
				if (kpiHandleResults != null && kpiHandleResults.size() > 0) {
					SaveLocalManager.getInstance().saveKpiDataToLocal(kpiTestView.getHandleKpiList());
				}
			}
		} catch (SettingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	/**
	 * 将所有的测试数据保存到数据库中
	 *
	 */
	private void saveDataToDB() {
		saveCommonData(mCurTestPackageName);
		// cpu
		if (viewCpu != null) {
			List<CpuHandleResult> cpuList = viewCpu.getHandleResult();
			if (cpuList != null && cpuList.size() > 0) {
				PerformanceDB.getInstance().insertCpuData(cpuList);
			}
		}

		// memory
		if (viewMemory != null) {
			List<MemoryHandleResult> memoryList = viewMemory.getHandleResult();
			if (memoryList != null && memoryList.size() > 0) {
				PerformanceDB.getInstance().insertMemoryData(memoryList);
			}
		}

		// 保存KPi数据
		if (kpiTestView != null) {
			List<KpiHandleResult> kpiHandleResults = kpiTestView.getHandleKpiList();
			if (kpiHandleResults != null && kpiHandleResults.size() > 0) {
				PerformanceDB.getInstance().insertKpiData(kpiHandleResults);
			}

		}
		if (viewFlow != null) {
			List<FlowHandleResult> flowHandleResults = viewFlow.getHanResultList();
			if (flowHandleResults != null && flowHandleResults.size() > 0) {
				PerformanceDB.getInstance().insertFlowData(flowHandleResults);
			}
		}
	}

	@Override
	public void deviceConnected(IDevice device) {
		// TODO Auto-generated method stub
		updateDeviceList();
	}

	@Override
	public void deviceDisconnected(IDevice device) {
		// TODO Auto-generated method stub
		updateDeviceList();
		//
	}

	@Override
	public void deviceChanged(IDevice device, int changeMask) {
		// TODO Auto-generated method stub
		if ((changeMask & IDevice.CHANGE_CLIENT_LIST) != 0) {
			updateClientList();
		} else if ((changeMask & IDevice.CHANGE_STATE) != 0) {
			updateDeviceList();
		}
	}

	/**
	 * 将测试的包名保存起来
	 */
	private void saveCommonData(String packageName) {
		if (CommonUtil.strIsNull(packageName)) {
			return;
		}

		String version = "";
		BaseTestInfo baseTestInfo = CollectDataImpl.getBaseTestInfo(packageName);
		if (baseTestInfo != null) {
			version = baseTestInfo.versionName;
		}

		// 保存测试的基本信息
		PerformanceDB.getInstance().saveCommonData(packageName, version);
	}

	/***
	 * 更新DeviceList
	 */
	private void updateDeviceList() {
		System.out.println("updateDeviceList .....");
		myIgnoreActionEvents = true;
		if (comboDevices != null && !comboDevices.isEnabled()) {
			return;
		}
		TreeSet<AndroidDevice> devices = AdbManager.getInstance().getDevices();
		List<String> snList = new ArrayList<>(2);
		for (AndroidDevice device : devices) {
			snList.add(device.getName());
		}

		if (comboDevices != null) {
			comboDevices.removeAllItems();
		}
		for (String sn : snList) {
			comboDevices.addItem(sn);
		}

		myIgnoreActionEvents = false;
	}

	/**
	 * 根据Device来获取进程
	 */
	private synchronized void updateClientList() {
		System.out.println("updateClientList .....");
		myIgnoreActionEvents = true;
		if (comboProcess != null && !comboProcess.isEnabled()) {
			return;
		}
		String selectDevice = getdevice();
		if (!CommonUtil.strIsNull(selectDevice)) {
			String devicesid = AdbManager.getInstance().getSerialNumber(selectDevice);
			IDevice dev = AdbManager.getInstance().getIDevice(selectDevice);
			ExecShellUtil.getInstance().setDevice(dev);
			List<String> respack = CollectDataImpl.getRunningProcess(devicesid);
			if (respack.size() > 0 && comboProcess != null) {
				comboProcess.removeAllItems();
			}
			// 对得到的列表按照首字母排序
			Collections.sort(respack, latterComparator);
			for (String sn : respack) {
				comboProcess.addItem(sn);
			}

			String packageName = ProPertiesUtil.getInstance().getProperties(Constants.CHOOSE_PACKAGE);
			if (!CommonUtil.strIsNull(packageName)) {
				comboProcess.setSelectedItem(packageName);
			}
		}

		myIgnoreActionEvents = false;
	}

	/**
	 * 进程名称的首字母排序
	 */
	private Comparator<String> latterComparator = new Comparator<String>() {

		@Override
		public int compare(String o1, String o2) {
			// TODO Auto-generated method stub
			return o1.compareTo(o2);
		}
	};

	/**
	 * 初始化AdbManager
	 */
	private void initAdbManager() {
		AdbManager.getInstance().init();
	}

	private void createTopMenu() {
		JMenuBar menuBar = new JMenuBar();
		setJMenuBar(menuBar);

		// 添加菜单选项
		JMenu aboutMenu = new JMenu("帮助");
		JMenu settingMenu = new JMenu("设置");

		JMenuItem aboutItem = new JMenuItem("关于");
		aboutItem.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				SwingUiUtil.getInstance().showTipsDialog(LaunchView.this, "关于", Constants.ABOUT, "我知道了", null);
			}
		});
		JMenuItem helpItem = new JMenuItem("使用帮助");
		helpItem.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				SwingUiUtil.getInstance().showTipsDialog(LaunchView.this, "使用帮助", Constants.HELP, "我知道了", null);
			}
		});

		aboutMenu.add(aboutItem);
		aboutMenu.add(helpItem);

		// 处理设置选项
		JMenuItem performanceItem = new JMenuItem("性能阈值");
		performanceItem.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				performanceSetting();
			}
		});

		JMenuItem saveDataItem = new JMenuItem("存储测试数据");
		saveDataItem.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				saveTestDataSetting();
			}
		});

		settingMenu.add(performanceItem);
		settingMenu.add(saveDataItem);

		// 将menu添加到工具栏
		menuBar.add(settingMenu);
		menuBar.add(aboutMenu);
	}

	/**
	 * 展示PerformanceSetting的配置页面
	 */
	private void performanceSetting() {
		PerformanceSettingDialog performanceSettingDialog = new PerformanceSettingDialog(this, "设置性能指标阈值");
		performanceSettingDialog.setVisible(true);
	}

	/**
	 * 保存测试数据的配置页面
	 */
	private void saveTestDataSetting() {
		SaveTestDataSettingDialog saveTestSettingDialog = new SaveTestDataSettingDialog(this, "设置保存数据的方式");
		saveTestSettingDialog.setVisible(true);
	}

	/**
	 * 销毁view中的数据
	 */
	private void destoryViewData() {
		if (viewFlow != null) {
			viewFlow.destoryData();
		}

		if (viewMemory != null) {
			viewMemory.destoryData();
		}

		if (viewCpu != null) {
			viewCpu.destoryData();
		}

		if (viewBattery != null) {
			viewBattery.destoryData();
		}

		if (viewFps != null) {
			viewFps.destoryData();
		}

		if (kpiTestView != null) {
			kpiTestView.destoryData();
		}

		if (mSlientWaitTimer != null) {
			mSlientWaitTimer.cancel();
		}
		// 清空选择的包名数据
		ProPertiesUtil.getInstance().removeValue(Constants.CHOOSE_PACKAGE);
		ProPertiesUtil.getInstance().removeValue(Constants.LAST_PACKAGENAME);
		GlobalConfig.TestPackageName = "";
		GlobalConfig.TestVersion = "";
		HandleDataManager.getInstance().destoryData();
	}

	/**
	 * 判断是否有任务在进行
	 *
	 * @return
	 */
	private boolean isNormalTestNow() {
		if (viewMemory.isRunning && viewCpu.isRunning && viewFlow.isRunning && kpiTestView.isRunning) {
			return true;
		}

		return false;
	}

	/**
	 * 判断静态任务是否在进行
	 *
	 * @return
	 */
	private boolean isSlientTestNow() {
		if (viewCpu.isRunning && viewFlow.isRunning && viewCpu.slient && viewFlow.isRunning) {
			return true;
		}

		return false;
	}

	/**
	 * 判断Fps是否在测试
	 *
	 * @return
	 */
	private boolean isFpsTestNow() {
		if (viewFps.isRunning) {
			return true;
		}

		return false;
	}

	public static void main(String[] args) {
		LaunchView launch = new LaunchView();
		launch.createParts();
		launch.initLogger();
		launch.initAdbManager();
		launch.addActionListener();
		launch.setVisible(true);
	}

	public static String getSelectPkg() {
		String pkg = comboProcess.getSelectedItem().toString();
		return pkg;
	}

	/**
	 * 设置设备和进程是否可以选择
	 *
	 * @param enable
	 */
	public static void setComboxEnable(boolean enable) {
		if (comboDevices != null) {
			comboDevices.setEnabled(enable);
		}

		if (comboProcess != null) {
			comboProcess.setEnabled(enable);
		}
	}

	/**
	 * 设置开始、结束、静默开关是否可以点击
	 *
	 * @param enable
	 */
	public static void setBtnEnable(boolean enable) {
		if (enable) {
			jb1.setEnabled(true);
			jb2.setEnabled(false);
			slientBtn.setEnabled(true);
		} else {
			jb1.setEnabled(false);
			jb2.setEnabled(false);
			slientBtn.setEnabled(false);
		}
	}
}