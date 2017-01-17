package com.xdja.view;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.sql.SQLException;

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
import com.xdja.collectdata.handleData.HandleDataResult;
import com.xdja.constant.GlobalConfig;

public class CpuView extends BaseChartView {

	/**
	 * serial UID auto generated
	 */
	private static final long serialVersionUID = -9002331611054515951L;
	private TimeSeries totalcpu;
	private Thread cpuThread;
	private boolean stopFlag = false;
	private CpuData mCurCpuData = null;
	public static int cpuCount = 0; // 添加静态计数器，用作条件判断，何时采集CPU数据用于数据模型的判断

	public CpuView(String chartContent, String title, String yaxisName) {
		super();
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
		if (mHandleDataList.size() > 0) {
			mHandleDataList.clear();
		}

		cpuThread = new Thread(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				stopFlag = false;
				mCurCpuData = null;
				while (true) {
					if (stopFlag) {
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
					handleData(mCurCpuData, mCurCpuData.cpuUsage);

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
	public void handleData(CpuData cpuData, double cpu) {
		// 对数据进行判断
		HandleDataResult handleDataResult = HandleDataManager.getInstance().handleCpu(cpuData, cpu);
		if (handleDataResult == null) {
			return;
		}
		// 记录数据
		mHandleDataList.add(handleDataResult);
		
		// 填充错误信息
		if (handleDataResult != null && !handleDataResult.result) {
			appendErrorInfo(formatErrorInfo(handleDataResult, String.valueOf(cpuData.cpuUsage), "cpu使用率过高"));
		}
	}

	/**
	 * 保存到数据库，savetodb()
	 */

	/**
	 * 停止任务，标记线程停止，并将测试数据插入数据库。
	 * 
	 * @throws SQLException
	 * @throws ClassNotFoundException
	 */
	public void stop() {
		stopFlag = true;
	}
}
