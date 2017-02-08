package com.xdja.view.chart;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;

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

import com.xdja.collectdata.CollectDataImpl;
import com.xdja.collectdata.entity.CpuData;
import com.xdja.collectdata.handleData.HandleDataManager;
import com.xdja.collectdata.handleData.entity.CpuHandleResult;
import com.xdja.constant.GlobalConfig;

public class CpuView extends BaseChartView {

	/**
	 * serial UID auto generated
	 */
	private static final long serialVersionUID = -9002331611054515951L;
	private TimeSeries totalcpu;
	private Thread cpuThread;
	private CpuData mCurCpuData = null;
	public static int cpuCount = 0; // 添加静态计数器，用作条件判断，何时采集CPU数据用于数据模型的判断
	private List<CpuHandleResult> cpuHandleResults = new ArrayList<>(20);
	public boolean slient = false; // 是否是静默测试

	public CpuView(String chartContent, String title, String yaxisName, boolean slient) {
		super();
		initView(chartContent, title, yaxisName);
	}

	public CpuView(String chartContent, String title, String yaxisName) {
		super();
		initView(chartContent, title, yaxisName);
	}

	private void initView(String chartContent, String title, String yaxisName) {
		this.totalcpu = new TimeSeries("应用CPU占用率");
		TimeSeriesCollection dataset = new TimeSeriesCollection();
		dataset.addSeries(this.totalcpu);

		DateAxis domain = new DateAxis("Time");
		NumberAxis range = new NumberAxis("CPU(%)");
		domain.setTickLabelFont(new Font("SansSerif", Font.PLAIN, 12));
		range.setTickLabelFont(new Font("SansSerif", Font.PLAIN, 12));
		domain.setLabelFont(new Font("SansSerif", Font.PLAIN, 14));
		range.setLabelFont(new Font("SansSerif", Font.PLAIN, 14));

		XYItemRenderer renderer = new XYLineAndShapeRenderer(true, false);
		renderer.setSeriesPaint(0, Color.red);
		// renderer.setSeriesPaint(1, Color.green);
		renderer.setSeriesStroke(0, new BasicStroke(3F));

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

		JFreeChart chart = new JFreeChart("应用CPU使用情况", new Font("SansSerif", Font.BOLD, 24), plot, true);
		chart.setBackgroundPaint(Color.white);
		ChartPanel chartPanel = new ChartPanel(chart);
		chartPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4),
				BorderFactory.createLineBorder(Color.black)));
		addJpanel(chartPanel);
	}

	/**
	 * 设置是否是静默测试
	 * 
	 * @param isSlient
	 *            是否是静默测试
	 */
	public void setSlientTest(boolean isSlient) {
		slient = isSlient;
	}

	/**
	 * 
	 * Adds an observation to the 'total memory' time series.
	 *
	 * @param y
	 *            the total memory used.
	 */
	private void addTotalObservation(CpuData cpuData) {
		if (cpuData == null) {
			return;
		}
		this.totalcpu.addOrUpdate(new Millisecond(), cpuData.cpuUsage);
	}

	/**
	 * 开启任务
	 * 
	 * @param packageName
	 */
	public void start(String packageName) {
		// 清空上次记录的数据
		if (cpuHandleResults.size() > 0) {
			cpuHandleResults.clear();
		}

		cpuThread = new Thread(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				stopFlag = false;
				mCurCpuData = null;
				isRunning = true;
				// 判断错误提示区域显示内容
				if (slient) {
					String slientStr = "===========开始CPU静默测试=============\n\n";
					appendErrorInfo(slientStr);
				} else {
					String normalStr = "===========开始CPU测试=============\n\n";
					appendErrorInfo(normalStr);
				}
				while (true) {
					if (stopFlag) {
						isRunning = false;
						break;
					}

					if (mCurCpuData == null) {
						mCurCpuData = CollectDataImpl.getCpuUsage(packageName, 0, 0);
						continue;
					}

					mCurCpuData = CollectDataImpl.getCpuUsage(packageName, mCurCpuData.lastProcTotal,
							mCurCpuData.lastProcPid);

					addTotalObservation(mCurCpuData);// 添加当前CPU到动态图表中

					// 处理采集到的CPU数据，建立问题模型处理机制
					if (slient) {
						handleSlientData(mCurCpuData);
					}else {
						handleData(mCurCpuData);
					}

					try {
						Thread.sleep(GlobalConfig.collectInterval);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		});

		cpuThread.start();
	}

	/**
	 * 处理采集到的CPU数据的，存放数据处理逻辑和方法,返回异常数据处理结果，无CPU的值，可以为true,也可以为FALSE
	 */
	public void handleData(CpuData cpuData) {
		// 对数据进行判断
		CpuHandleResult handleDataResult = HandleDataManager.getInstance().handleCpu(cpuData, cpuData.cpuUsage);
		if (handleDataResult == null) {
			return;
		}
		// 记录数据
		cpuHandleResults.add(handleDataResult);

		// 填充错误信息
		if (handleDataResult.isShowErrorMsg) {
			appendErrorInfo(formatErrorInfo(handleDataResult, String.valueOf(cpuData.cpuUsage), "cpu使用率过高"));
		}
	}

	/**
	 * 处理静默状态的数据
	 * 当10s内，cpu一直大于0.5时，我们认为是不正常的。
	 * @param cpuData
	 */
	public void handleSlientData(CpuData cpuData) {
		CpuHandleResult handleCpu = HandleDataManager.getInstance().handleCpusilence(cpuData, cpuData.cpuUsage);
		if (handleCpu == null) {
			return;
		}

		// 记录数据
		cpuHandleResults.add(handleCpu);
		// 填充错误信息
		if (handleCpu != null && !handleCpu.result) {
			appendErrorInfo(formatErrorInfo(handleCpu, String.valueOf(cpuData.cpuUsage), "静默状态使用CPU"));
		}
	}

	/**
	 * 停止任务，标记线程停止，并将测试数据插入数据库。
	 * 
	 * @throws SQLException
	 * @throws ClassNotFoundException
	 */
	public void stop() {
		stopFlag = true;
		// 判断错误提示区域显示内容
		if (slient) {
			String slientStr = "===========结束静默测试=============\n\n";
			appendErrorInfo(slientStr);
		} else {
			String normalStr = "===========结束测试=============\n\n";
			appendErrorInfo(normalStr);
		}
	}

	public List<CpuHandleResult> getHandleResult() {
		return cpuHandleResults;
	}

	public void destoryData() {
		if (cpuHandleResults != null) {
			cpuHandleResults.clear();
			cpuHandleResults = null;
		}
	}
}
