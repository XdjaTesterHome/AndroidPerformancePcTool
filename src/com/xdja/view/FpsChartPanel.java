package com.xdja.view;

import org.apache.log4j.Logger;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.time.Millisecond;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;

import com.xdja.constant.Constants;
import com.xdja.monitor.ControllerMonitor;

public class FpsChartPanel extends ChartPanel {

	/**
	 * serial UID is auto generated
	 */
	private static final long serialVersionUID = 6214606803165478469L;
	private Logger logger = Logger.getLogger(FpsChartPanel.class);
	private Thread fpsThread;
	private static TimeSeries timeSeries;
	private boolean stopFlag = false;

	public FpsChartPanel(String chartContent, String title, String yaxisName) {
		this(createChart(chartContent, title, yaxisName));
	}

	public FpsChartPanel(JFreeChart chart) {
		super(chart);
	}

	public void start() {
		fpsThread = new Thread(new Runnable() {
			@Override
			public void run() {
				stopFlag = false;
				while (true) {
					if (!stopFlag) {
						try {
							String activityName = ControllerMonitor.getInstance().getFpsController().getActivityName();
							double info = ControllerMonitor.getInstance().getFpsController().getInfo(activityName);
							timeSeries.add(new Millisecond(), info);
							logger.info(String.format("Package \"%s\" Battery: %f%%", activityName, info));
							Thread.sleep(500);
						} catch (InterruptedException e) {
							logger.error(e.getMessage(), e.getCause());
							e.printStackTrace();
						}
					} else {
						logger.info("Fps Chart Panel test is stoped!");
						break;
					}
				}
			}
		});
		fpsThread.start();
	}

	public void stop() {
		stopFlag = true;
	}

	public static JFreeChart createChart(String chartContent, String title,
			String yaxisName) {
		timeSeries = new TimeSeries(chartContent, Millisecond.class);
		TimeSeriesCollection dataset = new TimeSeriesCollection(timeSeries);
		// params:图表标题，图表x轴，图标y轴，数据集，显示图例，采用标准生成器，是否生成超链接
		JFreeChart timeSeriesChart = ChartFactory.createTimeSeriesChart(title,
				Constants.TIME_UNIT, yaxisName, dataset, true, true, false);
		// 获取plot对象
		XYPlot xyplot = timeSeriesChart.getXYPlot();
		// 获取x轴对象
		ValueAxis valueaxis = xyplot.getDomainAxis();
		// 自动设置数据轴数据范围
		valueaxis.setAutoRange(true);
		// 数据轴固定数据范围 30s
		valueaxis.setFixedAutoRange(60000D);
		// 获取y轴对象
		valueaxis = xyplot.getRangeAxis();
		return timeSeriesChart;
	}
}