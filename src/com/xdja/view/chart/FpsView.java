package com.xdja.view.chart;

import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
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

import com.xdja.collectdata.CollectDataImpl;
import com.xdja.collectdata.entity.FpsData;
import com.xdja.collectdata.handleData.HandleDataManager;
import com.xdja.collectdata.handleData.entity.FpsHandleResult;
import com.xdja.collectdata.handleData.entity.HandleDataResultBase;
import com.xdja.constant.GlobalConfig;
import com.xdja.database.PerformanceDB;
import com.xdja.util.CommonUtil;
import com.xdja.util.SwingUiUtil;

public class FpsView extends BaseChartView {

	/**
	 * serial UID auto generated
	 */
	private static final long serialVersionUID = -9002331611054515951L;
	private JButton startBtn, pauseBtn;
	private Thread fpsThread;
	private List<FpsData> fpsdataList = null;
	private List<FpsHandleResult> fpsHandleList = new ArrayList<>(12);
	private List<FpsHandleResult> errorList = new ArrayList<>(12);
	private CategoryPlot mPlot;
	private DefaultCategoryDataset mDataset  = null;
	private final static String  NOMESSGE = "测试帧率，请在开发者选项中找到【GPU呈现模式分析】，打开【在adb shell dumpsys gfxinfo中】选项";
	
	
	public FpsView(String chartContent, String title, String yaxisName) {
		super();
//		setLayout(new FlowLayout(FlowLayout.LEADING));
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
		mRenderer.setBaseItemLabelGenerator(new StandardCategoryItemLabelGenerator());
		mRenderer.setBaseItemLabelsVisible(true);
		
		mPlot.setRenderer(mRenderer);
		
		//将freechart添加到面板中
		ChartPanel chartPanel = new ChartPanel(mBarchart);
		chartPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4),
				BorderFactory.createLineBorder(Color.black)));
//		addSigleSwitch();
		chartPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
		//添加单独的图标
		startBtn = SwingUiUtil.getInstance().createBtnWithColor("开始", Color.green);
		pauseBtn = SwingUiUtil.getInstance().createBtnWithColor("结束", Color.RED);
		pauseBtn.setEnabled(false);
		startBtn.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				startBtn.setEnabled(false);
				pauseBtn.setEnabled(true);
				start(GlobalConfig.getTestPackageName());
			}
		});
		
		pauseBtn.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				startBtn.setEnabled(true);
				pauseBtn.setEnabled(false);
				stop();
				
				//保存数据
				saveDataToDb();
			}
		});
		
		startBtn.setLocation(0, 30);
		pauseBtn.setLocation(30, 30);
		
		chartPanel.add(startBtn);
		chartPanel.add(pauseBtn);
		addJpanel(chartPanel);
	}
	
	/**
	 * 开始测试
	 * 
	 * @param packageName
	 */
	public void start(String packageName) {
		fpsThread = new Thread(new Runnable() {
			@Override
			public void run() {
				// TODO Auto-generated method stub
				stopFlag = false;
				isRunning = true;
				while (true) {
					if (stopFlag) {
						isRunning = false;
						break;
					}
					fpsdataList = CollectDataImpl.getFpsData(packageName);
					if (fpsdataList != null ) {
						for(FpsData fpsdata : fpsdataList){
//							mDataset = new DefaultCategoryDataset();
							mDataset.addValue(fpsdata.fps, "帧率", fpsdata.activityName);
							mDataset.addValue(fpsdata.dropcount, "丢帧数", fpsdata.activityName);
							mDataset.addValue(fpsdata.framecount, "总帧数", fpsdata.activityName);
							if (mPlot != null) {
								mPlot.setDataset(mDataset);
							}
						}
					}
					
					handleFpsData(fpsdataList);
					try {
						Thread.sleep(GlobalConfig.collectMIDDLEInterval);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		});

		fpsThread.start();
	}
	
	public void stop() {
		stopFlag = true;
	}
	
	private void handleFpsData(List<FpsData> fpsDatas){
		fpsHandleList = HandleDataManager.getInstance().handleFpsData(fpsDatas);
		
		if (fpsHandleList != null && fpsHandleList.size() > 0) {
			// 展示错误信息
			for(FpsHandleResult fpsData : fpsHandleList){
				if (!fpsData.result) {
					if (errorList.contains(fpsData)) {
						continue;
					}
					
					errorList.add(fpsData);
					mShowMessageView.append(formatErrorInfo(fpsData, String.valueOf(fpsData.testValue), "页面出现卡顿"));
				}
			}
		}
	}
	
	@Override
	protected String formatErrorInfo(HandleDataResultBase result, String value, String errorInfo) {
		FpsHandleResult fpsHandleResult = null;
		if (result instanceof FpsHandleResult) {
			fpsHandleResult = (FpsHandleResult) result;
		}
		// TODO Auto-generated method stub
		StringBuilder sbBuilder = new StringBuilder("===================== \n");
    	if (!CommonUtil.strIsNull(errorInfo)) {
			sbBuilder.append(errorInfo).append("\n");
		}
    	sbBuilder.append("ActivityName = ").append(fpsHandleResult.activityName).append("\n");
    	sbBuilder.append("当前帧率值= ").append(value).append("\n");
    	sbBuilder.append("当前丢帧数= ").append(fpsHandleResult.dropcount).append("\n");
    	sbBuilder.append("Logfile= ").append(result.logPath).append("\n");
    	sbBuilder.append("===================== \n\n\n\n");
    	return sbBuilder.toString();
	}
	/**
	 * 返回fps处理之后的结果
	 * @return
	 */
	public List<FpsHandleResult> getFpsHandleResult(){
		return fpsHandleList;
	}
	
	public void saveDataToDb(){
		// 保存fps数据
		if (fpsHandleList == null || fpsHandleList.size() < 1) {
			return;
		}
		
		PerformanceDB.getInstance().insertFpsData(fpsHandleList);
		
		// 关闭数据库
		PerformanceDB.getInstance().closeDB();
	}
	
	public void destoryData(){
		if (fpsHandleList != null) {
			fpsHandleList.clear();
			fpsHandleList = null;
		}
		
		if (errorList != null) {
			errorList.clear();
			errorList = null;
		}
	}
	
	/**
	 *  设置当前界面的按钮是否是可以点击的。
	 * @param enable
	 */
	public void setBtnEnable(boolean enable){
		if (startBtn == null || pauseBtn == null) {
			return;
		}
		if (enable) {
			startBtn.setEnabled(true);
			pauseBtn.setEnabled(false);
		}else {
			startBtn.setEnabled(false);
			pauseBtn.setEnabled(false);
		}
		
	}
}


