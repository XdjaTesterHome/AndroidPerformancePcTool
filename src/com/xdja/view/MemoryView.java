package com.xdja.view;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.time.Millisecond;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.ui.RectangleInsets;

import com.android.ddmlib.AndroidDebugBridge;
import com.android.ddmlib.AndroidDebugBridge.IClientChangeListener;
import com.android.ddmlib.Client;
import com.android.ddmlib.ClientData;
import com.xdja.adb.AdbManager;
import com.xdja.collectdata.entity.MemoryData;
import com.xdja.collectdata.handleData.HandleDataManager;
import com.xdja.collectdata.handleData.HandleDataResult;
import com.xdja.constant.GlobalConfig;
import com.xdja.util.SwingUiUtil;

public class MemoryView extends BaseChartView implements IClientChangeListener {

	/**
	 * serial UID auto generated
	 */
	private static final long serialVersionUID = -9002331611054515951L;
	private TimeSeries totalAlloc;
	private TimeSeries freeAlloc;
	private boolean stopFlag = false;
	protected Client mCurClient = null;
	private Thread memoryThread;
	private MemoryData mMemoryData = null;
	private HandleDataResult mHandleDataResult = null;
	
	public MemoryView(String chartContent, String title, String yaxisName) {
		super();
		this.totalAlloc = new TimeSeries("Alloc memory");
		this.freeAlloc  = new TimeSeries("Free memory");
		TimeSeriesCollection dataset = new TimeSeriesCollection();
		dataset.addSeries(this.totalAlloc);
		dataset.addSeries(this.freeAlloc);
		
		DateAxis domain = new DateAxis("Time");
		NumberAxis range = new NumberAxis("Memory(MB)");
		domain.setTickLabelFont(new Font("SansSerif", Font.PLAIN, 12));
		range.setTickLabelFont(new Font("SansSerif", Font.PLAIN, 12));
		domain.setLabelFont(new Font("SansSerif", Font.PLAIN, 14));
		range.setLabelFont(new Font("SansSerif", Font.PLAIN, 14));

		XYItemRenderer renderer = new XYLineAndShapeRenderer(true, false);
		renderer.setSeriesPaint(0, Color.red);
		renderer.setSeriesPaint(1, Color.green);
		renderer.setSeriesStroke(0, new BasicStroke(3F));
		renderer.setSeriesStroke(1, new BasicStroke(3F));

		XYPlot plot = new XYPlot(dataset, domain, range, renderer);
		plot.setBackgroundPaint(Color.lightGray);
		plot.setDomainGridlinePaint(Color.white);
		plot.setRangeGridlinePaint(Color.white);
		plot.setAxisOffset(new RectangleInsets(5.0, 5.0, 5.0, 5.0));
		domain.setAutoRange(true);
		domain.setLowerMargin(0.0);
		domain.setUpperMargin(0.0);
		domain.setTickLabelsVisible(true);

		range.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
		
		JFreeChart chart = ChartFactory.createTimeSeriesChart("应用内存分配情况", 
				"时间(s)", "内存值(MB)", dataset, true, true, false);
		chart.setBackgroundPaint(Color.white);
		ChartPanel chartPanel = new ChartPanel(chart);
		chartPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4),
				BorderFactory.createLineBorder(Color.black)));
		chartPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
		//添加单独的图标
		JButton gcButton = SwingUiUtil.getInstance().createBtnWithColor("GC", Color.green);
		gcButton.setLocation(0, 30);
		gcButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				AdbManager.getInstance().causeGC(GlobalConfig.PackageName);
			}
		});
		chartPanel.add(gcButton);
		
		add(chartPanel);
	}

	/**
	 * Adds an observation to the 'total memory' time series.
	 *
	 * @param y
	 *            the total memory used.
	 */
	private void addTotalObservation(double totalMemory, double freeMemory) {
		this.totalAlloc.addOrUpdate(new Millisecond(), totalMemory);
		this.freeAlloc.addOrUpdate(new Millisecond(), freeMemory);
	}

	/**
	 * 开启进行测试
	 * 
	 * @param packageName
	 */
	public void start(String packageName) {
		if (mCurClient == null) {
			mCurClient = AdbManager.getInstance().getClient(GlobalConfig.DeviceName, GlobalConfig.PackageName);
		}
		if (mCurClient != null) {
			mCurClient.setHeapInfoUpdateEnabled(true);
			AndroidDebugBridge.addClientChangeListener(this);
		}
		memoryThread = new Thread(new Runnable() {
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
				stopFlag = false;
				while (true) {
					if (stopFlag) {
						break;
					}
					
					AdbManager.getInstance().getAllocInfo(GlobalConfig.DeviceName, packageName);
					try {
						Thread.sleep(GlobalConfig.collectInterval);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		});
		memoryThread.start();
	}

	public void stop() {
		stopFlag = true;
		if (mCurClient != null) {
			mCurClient.setHeapInfoUpdateEnabled(false);
			AndroidDebugBridge.removeClientChangeListener(this);
		}

	}

	@Override
	public void clientChanged(Client client, int changeMask) {
		// TODO Auto-generated method stub
		if (mCurClient != null && mCurClient == client) {
			if ((changeMask & Client.CHANGE_HEAP_DATA) != 0) {
				// if (client.isHeapUpdateEnabled()) {
				float freeMb = 0.0f;
				float allocMb = 0.0f;
				if (client != null) {
					ClientData.HeapInfo m = client.getClientData().getVmHeapInfo(1);
					if (m != null) {
						allocMb = m.bytesAllocated / (1024.f * 1024.f);
						freeMb = m.sizeInBytes / (1024.f * 1024.f) - allocMb;
						mMemoryData = new MemoryData(allocMb, freeMb);
						//处理有问题的数据
						mHandleDataResult = HandleDataManager.getInstance().handleMemoryData(mMemoryData);
						handleResult(mHandleDataResult);
						addTotalObservation(allocMb,freeMb);
					}
				}
			}
			// }
		}
	}
	
	
	/**
	 *  处理问题模型返回的结果。有问题进行展示
	 * @param result
	 */
	private void handleResult(HandleDataResult result){
		if (result == null || result.result) {
			return ;
		}
		
		//在界面上展示问题数据
		
	}
}
