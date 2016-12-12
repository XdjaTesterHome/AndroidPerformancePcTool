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

public class FlowView extends ChartPanel {

	/**
	 * serial UID is auto generated
	 */
	private Logger logger = Logger.getLogger(FlowView.class);
	private static final long serialVersionUID = 1719925024734975743L;
	private static TimeSeries timeSeries; 
	private Thread flowThread;
	private boolean stopFlag = false;

	public FlowView(String chartContent,String title,String yaxisName)  
    {  
        this(createChart(chartContent,title,yaxisName));  
    }
	
	public FlowView(JFreeChart chart) {
		super(chart);
	}
	
	public void start(final String packageName) {
		flowThread = new Thread(new Runnable() {
			@Override
			public void run() {
				stopFlag = false;
				while(true) {
					if (!stopFlag) {
						try {
							double info = ControllerMonitor.getInstance().getFlowController().getInfo(packageName);
							timeSeries.add(new Millisecond(), info);
							logger.info(String.format("Package \"%s\" Flow: %f kb", packageName, info));
							Thread.sleep(500);
						} catch (InterruptedException e) {
							logger.error(e.getMessage(), e.getCause());
							e.printStackTrace();
						}
					} else {
						logger.info("Flow View test is stoped!");
						break;
					}
				}
			}
		});
		flowThread.start();
	}
	
	public void stop() {
		stopFlag = true;
	}

	private static JFreeChart createChart(String chartContent, String title, String yaxisName) {
		// 创建时序图对象
		timeSeries = new TimeSeries(chartContent, Millisecond.class);
		TimeSeriesCollection timeseriescollection = new TimeSeriesCollection(timeSeries);
		JFreeChart jfreechart = ChartFactory.createTimeSeriesChart(title, Constants.TIME_UNIT, yaxisName, timeseriescollection, true, true, false);
		XYPlot xyplot = jfreechart.getXYPlot();
		// 纵坐标设定
		ValueAxis valueaxis = xyplot.getDomainAxis();
		// 自动设置数据轴数据范围
		valueaxis.setAutoRange(true);
		// 数据轴固定数据范围 30s
		valueaxis.setFixedAutoRange(60000D);
		valueaxis = xyplot.getRangeAxis();
		
		return jfreechart;
	}
	
}
