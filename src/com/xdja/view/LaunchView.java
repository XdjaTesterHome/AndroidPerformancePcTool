package com.xdja.view;

import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import org.apache.log4j.Logger;
import com.android.ddmlib.IDevice;
import com.android.ddmlib.AndroidDebugBridge;
import com.android.ddmlib.AndroidDebugBridge.IDeviceChangeListener;
import com.xdja.adb.AdbManager;
import com.xdja.adb.AndroidDevice;
import com.xdja.collectdata.CollectDataImpl;
import com.xdja.collectdata.CollectDataUtil;
import com.xdja.constant.Constants;
import com.xdja.constant.GlobalConfig;
import com.xdja.log.LoggerManager;
import com.xdja.util.CommonUtil;
import com.xdja.util.SwingUiUtil;
import com.xdja.util.SwingUiUtil.ClickDialogBtnListener;

public class LaunchView extends JFrame implements IDeviceChangeListener {

	/**
	 * serial Version UID is auto generated
	 */
	private static final long serialVersionUID = 7845872493970114091L;
	private Logger logger = Logger.getLogger(LaunchView.class);
	private String author;
	private JPanel frame;
	private JButton jb1, jb2;
	private MemoryView viewMemory;
	private FlowView viewFlow;
	private CpuView viewCpu;
	private KpiTestView kpiTestView;
	private FpsView viewFps;
	private BatteryView viewBattery;
	private static JComboBox<String> comboDevices;
	private JComboBox<String> comboProcess;
	private JTabbedPane jTabbedPane = new JTabbedPane();
	private String[] tabNames = { "   内    存   ", "     cpu    ", "   电   量   ", "    加载时间     ", "   帧   率   ",
			"   流   量   " };

	/**
	 * constructor to init a LaunchView instance create a JPanel instance to put
	 * other controller parts
	 * 
	 * @param name:
	 *            author name
	 */
	public LaunchView(String name) {
		this.author = name;
		this.frame = new JPanel();
		setTitle(String.format("%s v1.0", author));
		setBounds(100, 50, 1249, 760);
		add(frame);
		setVisible(true);
		AndroidDebugBridge.addDeviceChangeListener(this);
	}

	/**
	 * constructor to init a LaunchView instance create a JPanel instance to put
	 * other controller parts 用于创建一个父控件来摆放其他的控件，这里是用来显示其他的测试项
	 */
	@SuppressWarnings({ "rawtypes", "unchecked", "serial" })
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
				// TODO Auto-generated method stub
				String packageName = (String) comboProcess.getSelectedItem();
				if (packageName != null) {
					GlobalConfig.PackageName = packageName;
				}
			}
		});

		// 开始监控按钮
		jb1 = new JButton("开始监控");
		Rectangle rectjb1 = new Rectangle(860, 0, 100, 30);
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
		Rectangle rectjb2 = new Rectangle(980, 0, 100, 30);
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
						comboDevices.setEnabled(true);
						comboProcess.setEnabled(true);
						stopTest();
					}
				});

				thread.start();
			}
		});

		layoutTabComponents();
	}

	/**
	 * 用来摆放tab的组件 tabNames = {"内存", "cpu", "电量", "kpitest", "帧率", "流量"};
	 */
	private void layoutTabComponents() {
		Rectangle rect = new Rectangle(100, 100, 800, 200);
		// 1.内存
		// memory chart view
		viewMemory = new MemoryView(Constants.MEMORY, Constants.MEMORYContent, Constants.MEMORY_UNIT);
		viewMemory.setBounds(rect);
		jTabbedPane.addTab(tabNames[0], viewMemory);

		// 2.cpu
		viewCpu = new CpuView(Constants.CPU, Constants.CPU, Constants.CPU_UNIT);
		viewCpu.setBounds(rect);
		jTabbedPane.addTab(tabNames[1], viewCpu);

		// 3.电量
		viewBattery = new BatteryView(Constants.BATTERY, Constants.BATTERY, Constants.BATTERY_UNIT);
		viewBattery.setBounds(rect);
		jTabbedPane.addTab(tabNames[2], viewBattery);

		// 4.kpiTest
		kpiTestView = new KpiTestView(Constants.KPITITLE, Constants.KPI);
		kpiTestView.setBounds(rect);
		jTabbedPane.addTab(tabNames[3], kpiTestView);

		// 5.帧率
		viewFps = new FpsView(Constants.FPS, Constants.FPSTITLE, Constants.FPS_UNIT);
		viewFps.setBounds(rect);
		jTabbedPane.addTab(tabNames[4], viewFps);

		// 6.流量
		viewFlow = new FlowView(Constants.FLOW, Constants.FLOW, Constants.FLOW_UNIT);
		viewFlow.setBounds(rect);
		jTabbedPane.addTab(tabNames[5], viewFlow);

		frame.add(jTabbedPane);
		rect = new Rectangle(20, 100, 1100, 600);
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
			}

			@Override
			public void clickCancelBtn() {
				// TODO Auto-generated method stub
				setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE); // 这个是关键
			}
		});
	}

	private void startTest() {
		if (viewCpu != null) {
			viewCpu.start(GlobalConfig.PackageName);
		}

		if (viewMemory != null) {
			viewMemory.start(GlobalConfig.PackageName);
		}

		if (viewFlow != null) {
			viewFlow.start(GlobalConfig.PackageName);
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

	/***
	 * 更新DeviceList
	 */
	private void updateDeviceList() {
		if (comboDevices != null && !comboDevices.isEnabled()) {
			return ;
		}
		TreeSet<AndroidDevice> devices = AdbManager.getInstance().getDevices();
		System.out.println("updateDeviceList = " + devices.size());
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
	}

	/**
	 * 根据Device来获取进程
	 */
	private void updateClientList() {
		if (comboProcess != null && !comboProcess.isEnabled()) {
			return ;
		}
		String selectDevice = getdevice();
		if (!CommonUtil.strIsNull(selectDevice)) {
			String devicesid = AdbManager.getInstance().getSerialNumber(selectDevice);
			IDevice dev = AdbManager.getInstance().getIDevice(selectDevice);
			CollectDataUtil.setDevice(dev);
			List<String> respack = CollectDataImpl.getRunningProcess(devicesid);
			if (respack.size() > 0 && comboProcess != null) {
				comboProcess.removeAllItems();
			}
			for (String sn : respack) {
				comboProcess.addItem(sn);
			}
		}
	}
	
	/**
	 *  初始化AdbManager
	 */
	private void initAdbManager(){
		AdbManager.getInstance().init();
	}
	
	
	public static void main(String[] args) {
		LaunchView launch = new LaunchView(Constants.PRODUCT_NAME);
		launch.createParts();
		launch.initLogger();
		launch.initAdbManager();
		launch.addActionListener();
		launch.setVisible(true);

	}

}
