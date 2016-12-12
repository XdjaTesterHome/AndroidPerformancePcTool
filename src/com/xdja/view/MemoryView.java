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

public class MemoryView extends ChartPanel{
	
	/**
	 * serial UID auto generated
	 */
	private static final long serialVersionUID = -9002331611054515951L;
	private Logger logger = Logger.getLogger(MemoryView.class);
	private static TimeSeries timeSeries; 
	private Thread memoryThread;
	private boolean stopFlag = false;
	
	public MemoryView(String chartContent,String title,String yaxisName)  
    {  
        this(createChart(chartContent,title,yaxisName));  
    }
	
	public MemoryView(JFreeChart chart) {
		super(chart);
	}
	
	public void start(final String packageName) {
		memoryThread = new Thread(new Runnable() {
			@Override
			public void run() {
				stopFlag = false;
				while (true) {
					if (!stopFlag) {
						try {
							double info = ControllerMonitor.getInstance().getMemoryController().getInfo(packageName);
							timeSeries.add(new Millisecond(), info);
							logger.info(String.format("package \"%s\" memory: %f kb", packageName, info));
							Thread.sleep(500);
						} catch (InterruptedException e) {
							logger.error(e.getMessage(), e.getCause());
							e.printStackTrace();
						}
					} else {
						logger.info("Memory View test is stoped!");
						break;
					}
				}
			}
		});
		memoryThread.start();
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
