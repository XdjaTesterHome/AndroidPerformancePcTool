package com.xdja.view;

//import org.apache.log4j.Logger;
//import org.jfree.chart.ChartFactory;
//import org.jfree.chart.ChartPanel;
//import org.jfree.chart.JFreeChart;
//import org.jfree.chart.axis.ValueAxis;
//import org.jfree.chart.plot.XYPlot;
//import org.jfree.data.time.TimeSeries;
//import org.jfree.data.time.Millisecond;
//import org.jfree.data.time.TimeSeriesCollection;
//
//import com.xdja.constant.Constants;
//import com.xdja.monitor.ControllerMonitor;
//
//public class BatteryView extends ChartPanel {
//
//	/**
//	 * serial UID is auto generated
//	 */
//	private static final long serialVersionUID = 6214606803165478469L;
//	private Logger logger = Logger.getLogger(BatteryView.class);
//	private Thread batteryThread;
//	private static TimeSeries timeSeries;
//	private boolean stopFlag = false;
//	
//	public BatteryView(String chartContent, String title, String yaxisName) {
//		this(createChart(chartContent, title, yaxisName));
//	}
//
//	public BatteryView(JFreeChart chart) {
//		super(chart);
//	}
//	
//	public void start(final String packageName) {
//		batteryThread = new Thread(new Runnable() {
//			@Override
//			public void run() {
//				stopFlag = false;
//				while(true) {
//					if (!stopFlag) {
//						try {
//							double info = ControllerMonitor.getInstance().getBatteryController().getInfo(packageName);
//							timeSeries.add(new Millisecond(), info);
//							logger.info(String.format("Package \"%s\" Battery: %f%%", packageName, info));
//							Thread.sleep(500);
//						} catch (InterruptedException e) {
//							logger.error(e.getMessage(), e.getCause());
//							e.printStackTrace();
//						}
//					} else {
//						logger.info("Battery View test is stoped!");
//						break;
//					}
//				}
//			}
//		});
//		batteryThread.start();
//	}
//	
//	public void stop() {
//		stopFlag = true;
//	}
//	
//	public static JFreeChart createChart(String chartContent, String title, String yaxisName) {
//		timeSeries = new TimeSeries(chartContent, Millisecond.class);
//		TimeSeriesCollection dataset = new TimeSeriesCollection(timeSeries); 
//		//params:图表标题，图表x轴，图标y轴，数据集，显示图例，采用标准生成器，是否生成超链接
//		JFreeChart timeSeriesChart = ChartFactory.createTimeSeriesChart(title, Constants.TIME_UNIT, yaxisName, dataset, true, true, false);
//		// 获取plot对象
//		XYPlot xyplot = timeSeriesChart.getXYPlot();
//		// 获取x轴对象
//		ValueAxis valueaxis = xyplot.getDomainAxis();
//		// 自动设置数据轴数据范围
//		valueaxis.setAutoRange(true);
//		// 数据轴固定数据范围 30s
//		valueaxis.setFixedAutoRange(60000D);
//		// 获取y轴对象
//		valueaxis = xyplot.getRangeAxis();
//		return timeSeriesChart;
//	}

//}



import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.Timer;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.StandardChartTheme;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.labels.StandardCategoryItemLabelGenerator;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.title.LegendTitle;
import org.jfree.chart.title.TextTitle;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;

import com.xdja.collectdata.CollectDataImpl;
import com.xdja.collectdata.CCRDFile;
import com.xdja.collectdata.FpsData;
import com.xdja.collectdata.KpiData;
import com.xdja.constant.Constants;
import com.xdja.constant.GlobalConfig;



public class BatteryView extends BaseChartView {

	/**
	 * serial UID auto generated
	 */
	private static final long serialVersionUID = -9002331611054515951L;
	private boolean stopFlag = false;
	private Thread batteryThread;
	private String[][] batteryData ;
	
	
	private CategoryPlot mPlot;
	private DefaultCategoryDataset mDataset  = null;
	
	
	public BatteryView(String chartContent, String title, String yaxisName) {
		super();
		mDataset = new DefaultCategoryDataset();
		JFreeChart mBarchart = ChartFactory.createBarChart(title, chartContent, yaxisName, mDataset,
				PlotOrientation.VERTICAL, // 图表方向
				true, // 是否生成图例
				true, // 是否生成提示工具
				false // 是否生成url连接
		);
		// 图表标题设置
		TextTitle mTextTitle = mBarchart.getTitle();
		mTextTitle.setFont(new Font("黑体", Font.BOLD, 20));
		// 图表图例设置
		LegendTitle mLegend = mBarchart.getLegend();
		if (mLegend != null) {
			mLegend.setItemFont(new Font("宋体", Font.CENTER_BASELINE, 15));
		}

		// 设置柱状图轴
		CategoryPlot mPlot = mBarchart.getCategoryPlot();
		// x轴
		CategoryAxis mDomainAxis = mPlot.getDomainAxis();
		mDomainAxis.setLabelFont(new Font("宋体", Font.PLAIN, 15));
		// 设置x轴坐标字体
		mDomainAxis.setTickLabelFont(new Font("宋体", Font.PLAIN, 15));

		// y轴
		ValueAxis mValueAxis = mPlot.getRangeAxis();
		mValueAxis.setLabelFont(new Font("宋体", Font.PLAIN, 15));
		mValueAxis.setTickLabelFont(new Font("宋体", Font.PLAIN, 15));

		// 柱体显示数值
		BarRenderer mRenderer = new BarRenderer();
		mRenderer.setMaximumBarWidth(1);
		mRenderer.setBaseItemLabelGenerator(new StandardCategoryItemLabelGenerator());
		mRenderer.setBaseItemLabelsVisible(true);
		mPlot.setRenderer(mRenderer);
		
		//将freechart添加到面板中
		ChartPanel chartPanel = new ChartPanel(mBarchart);
		chartPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4),
				BorderFactory.createLineBorder(Color.black)));
		add(chartPanel);
	}

	/**
	 * Adds an observation to the 'total memory' time series.
	 *
	 * @param fpsdata
	 *            the total memory used.FpsData
	 */
		
	/**
	 * 开始测试
	 * 
	 * @param packageName
	 * @throws InterruptedException 
	 */
	public void start(String packageName) throws InterruptedException {
		
		batteryThread = new Thread(new Runnable() {
			@Override
			public void run() {
				// TODO Auto-generated method stub
		        stopFlag = false;
				try {
					batteryData = CCRDFile.getpowerdata(packageName);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
					System.out.println(batteryData);
					if (batteryData != null) {
						for (int i=0;i<batteryData.length;i++){
						mDataset.addValue(Double.parseDouble((batteryData[i][2])), "uid",batteryData[i][0]);
						}
						if (mPlot != null) {
							mPlot.setDataset(mDataset);
						}
					}
				}
			
		});
		batteryThread.start();
	}

	public void stop() {
		stopFlag = true;
    }
	
	

}
