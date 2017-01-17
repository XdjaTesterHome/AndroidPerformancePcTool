package com.xdja.view;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.sql.SQLException;
import java.sql.Statement;
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
import com.xdja.collectdata.handleData.HandleDataResult;
import com.xdja.constant.GlobalConfig;
import com.xdja.database.performancedata;

public class CpuView extends BaseChartView {

	/**
	 * serial UID auto generated
	 */
	private static final long serialVersionUID = -9002331611054515951L;
	private TimeSeries totalcpu;
	private Thread cpuThread;
	private boolean stopFlag = false;
	private CpuData mCurCpuData = null;
	public static int i =0;  //添加静态计数器，用作条件判断，何时采集CPU数据用于数据模型的判断
	public static HandleDataResult errorRe ;//定义一个数据处理对象，用于接收和处理数据模型中的数据
	public static ArrayList<ArrayList<String>> cpudbdata;//定义二维数组列表，用于接收测试数据并存储，后续批量插入数据库
	public static ArrayList<String> cpudbd;//定义一维数组列表，用于接收测试数据并存储，后续批量插入数据库
	
	HandleDataManager handle = new HandleDataManager();

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
		
		// 对数据进行判断
		HandleDataResult handleDataResult = HandleDataManager.getInstance().handleCpu(cpuData);
		// 记录数据
		mHandleDataList.add(handleDataResult);
		if (handleDataResult != null && !handleDataResult.result) {
			// 填充错误信息
			if (mShowMessageView != null) {
				mShowMessageView.append(formatErrorInfo(handleDataResult, cpuData));
			}
		}
	}

	/**
	 * 格式化错误信息
	 * 
	 * @param result
	 * @return
	 */
	private String formatErrorInfo(HandleDataResult result, CpuData cpuData) {
		StringBuilder sbBuilder = new StringBuilder("===================== \n");
		sbBuilder.append("ActivityName = ").append(result.activityName);
		sbBuilder.append("当前测试值              = ").append(cpuData.cpuUsage);
		sbBuilder.append("Logfile      = ").append(result.logPath);
		sbBuilder.append("截屏路径                  = ").append(result.screenshotsPath);
		sbBuilder.append("methodTrace  = ").append(result.methodTracePath);
		sbBuilder.append("===================== \n\n\n\n");
		return sbBuilder.toString();
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

		if (mShowMessageView != null) {
			mShowMessageView.setText("");
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

					addTotalObservation(mCurCpuData);//添加当前CPU到动态图表中
					
					errorRe = handleData(mCurCpuData);//处理采集到的CPU数据，建立问题模型处理机制
					System.out.println(errorRe.CPUString());
					cpudbd.add(errorRe.CPUString());//转化结果为字符串类型
					cpudbd.add(errorRe.activityName);//转化结果为字符串类型
					cpudbd.add(errorRe.reString());//转化结果为字符串类型
					cpudbd.add((String)errorRe.screenshotsPath);//转化结果为字符串类型
					cpudbd.add(errorRe.logPath);//转化结果为字符串类型
					cpudbd.add(errorRe.methodTracePath);//转化结果为字符串类型
					cpudbd.add(errorRe.memoryTracePath);//转化结果为字符串类型
					cpudbdata.add(cpudbd);//添加到二维列表
					cpudbd.clear();//清理一维度列表元素
					
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
	public HandleDataResult handleData(CpuData cpuData) {
		i =i+1;     //在采集频率的方法中添加逻辑判断计数器,添加计时器参数;这部分计数器也可以放在主线程循环中
    	if (i>10){
    		i=1;
    	}
    	if (i ==10){     //在主线程中添加逻辑判断，条件满足时执行相关方法
    		handle.cpuList(cpuData);
		}
    	HandleDataResult abresult = handle.handleCpu(cpuData);
    	return abresult;
	}
	
	/**
	 * 保存到数据库，savetodb()
	 */
	
	
	/**
	 * 停止任务，标记线程停止，并将测试数据插入数据库。
	 * @throws SQLException 
	 * @throws ClassNotFoundException 
	 */
	public void stop()  {
		stopFlag = true;
		performancedata perfor = new performancedata();
		try {
			perfor.conperformance("test");
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			perfor.creatperformance(performancedata.stat,performancedata.conn);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}//创建数据库performance
		try {
			perfor.conperformance("performance");
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} //连接数据库performance
		perfor.CPUteble(performancedata.stat,"cputable","com.xdja.actoma","V3.3056.1","CPU");//创建数据表cputable
		try {
			perfor.insertDatas(performancedata.stat,cpudbdata,"cputable");
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}//批量插入数据
		perfor.closeperformance(performancedata.conn,performancedata.stat,performancedata.result);//关闭数据库连接
	}

	/**
	 * 获取处理过的数据
	 * 
	 * @return
	 */
	public List<HandleDataResult> getHandleResult() {
		
		return mHandleDataList;
	}
}
