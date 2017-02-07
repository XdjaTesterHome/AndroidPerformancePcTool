
package com.xdja.view.chart;

import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.labels.StandardCategoryItemLabelGenerator;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.title.LegendTitle;
import org.jfree.chart.title.TextTitle;
import org.jfree.data.category.DefaultCategoryDataset;

import com.xdja.collectdata.CCRDFile;
import com.xdja.collectdata.CollectDataImpl;
import com.xdja.collectdata.entity.BatteryData;
import com.xdja.collectdata.handleData.HandleDataManager;
import com.xdja.collectdata.handleData.entity.BatteryHandleResult;
import com.xdja.constant.Constants;
import com.xdja.database.PerformanceDB;
import com.xdja.util.CommonUtil;
import com.xdja.util.ProPertiesUtil;
import com.xdja.util.SwingUiUtil;
import com.xdja.view.main.LaunchView;

public class BatteryView extends BaseChartView {

	/**
	 * serial UID auto generated
	 */
	private static final long serialVersionUID = -9002331611054515951L;
	private Thread batteryThread;
	private List<BatteryData> batteryDataList;

	private final static String NOMESSGE = "收集电量数据，需要拔掉USB连接，然后执行自己的测试用例，再次连接USB并分析数据";
	private CategoryPlot mPlot;
	private DefaultCategoryDataset mDataset = null;

	private JButton startBtn,parseBtn;
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
		mPlot.setNoDataMessage(NOMESSGE);
		mPlot.setNoDataMessageFont(new Font("粗体", Font.BOLD, 17));
		// x轴
		CategoryAxis mDomainAxis = mPlot.getDomainAxis();
		mDomainAxis.setCategoryLabelPositions(CategoryLabelPositions.UP_45);
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

		// 将freechart添加到面板中
		ChartPanel chartPanel = new ChartPanel(mBarchart);
		chartPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4),
				BorderFactory.createLineBorder(Color.black)));
		chartPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
		// 添加【开始测试】、【分析数据】按钮
		startBtn = SwingUiUtil.getInstance().createBtnWithColor("开始测试", Color.green);
		parseBtn = SwingUiUtil.getInstance().createBtnWithColor("分析数据", Color.RED);
		startBtn.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				startBtn.setEnabled(false);
				LaunchView.setComboxEnable(false);
				LaunchView.setBtnEnable(false);
				// 清理数据
				boolean isSuc = CollectDataImpl.clearBatteryData();
				if (isSuc) {
					// 记录测试包名
					ProPertiesUtil.getInstance().writeProperties(Constants.LAST_PACKAGENAME, ProPertiesUtil.getInstance().getProperties(Constants.CHOOSE_PACKAGE));
					// show dialog
					SwingUiUtil.getInstance().showTipsDialog(BatteryView.this, "提示", "测试电量，请拔掉usb，然后执行自己的测试用例即可", "好的",
							null);
				}
			}
		});

		parseBtn.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				startBtn.setEnabled(true);
				LaunchView.setComboxEnable(true);
				LaunchView.setBtnEnable(true);
				try {
					String lastPackageName = ProPertiesUtil.getInstance().getProperties(Constants.LAST_PACKAGENAME);
					String nowPackageName = ProPertiesUtil.getInstance().getProperties(Constants.CHOOSE_PACKAGE);
					if (CommonUtil.strIsNull(lastPackageName) || !lastPackageName.equals(nowPackageName)) {
						SwingUiUtil.getInstance().showTipsDialog(BatteryView.this, "提示", "请选择上次测试的应用包名之后再执行解析数据操作",
								"好的", null);
						return;
					}
					start(lastPackageName);
				} catch (InterruptedException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		});

		chartPanel.add(startBtn);
		chartPanel.add(parseBtn);
		addJpanel(chartPanel);
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
				isRunning = false;
				try {
					batteryDataList = CCRDFile.getpowerdata(packageName);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				if (batteryDataList != null || batteryDataList.size() > 0) {
					for(BatteryData batteryData : batteryDataList){
						mDataset.addValue(batteryData.getBatteryValue(), "uid", batteryData.getUid());
					}
					
					if (mPlot != null) {
						mPlot.setDataset(mDataset);
					}
				}
				
				handleBatteryData();
				isRunning = false;
			}

		});
		batteryThread.start();
	}
	
	/**
	 * 处理电量数据
	 */
	private void handleBatteryData(){
		if (batteryDataList == null || batteryDataList.size() < 0) {
			return;
		}
		
		List<BatteryHandleResult> batteryHandleResults = HandleDataManager.getInstance().handleBatteryData(batteryDataList);
		if (batteryHandleResults == null || batteryHandleResults.size() < 1) {
			return;
			
		}
		
		// 保存到数据库
		saveDataToDb(batteryHandleResults);
		
		// 显示异常信息
		for(BatteryHandleResult result : batteryHandleResults){
			if (!result.result) {
				mShowMessageView.append(formatErrorInfo(result, result.testValue, "电量消耗过多"));
			}
		}
	}
	
	/**
	 * 将数据保存到数据库中
	 */
	public void saveDataToDb(List<BatteryHandleResult> batteryHandleResults){
		if (batteryDataList == null) {
			return;
		}
		
		PerformanceDB.getInstance().insertBatteryData(batteryHandleResults);
		
		PerformanceDB.getInstance().closeDB();
	}
	
	public void destoryData(){
		if (batteryDataList != null) {
			batteryDataList.clear();
			batteryDataList = null;
		}
	}
	
	/**
	 *  设置当前界面的按钮是否是可以点击的。
	 * @param enable
	 */
	public void setBtnEnable(boolean enable){
		if (startBtn == null || parseBtn == null) {
			return;
		}
		if (enable) {
			startBtn.setEnabled(true);
			parseBtn.setEnabled(false);
		}else {
			startBtn.setEnabled(false);
			parseBtn.setEnabled(false);
		}
		
	}
}
