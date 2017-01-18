package com.xdja.view.chart;

import java.awt.Color;
import java.awt.Font;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;

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
import com.xdja.collectdata.entity.KpiData;
import com.xdja.collectdata.handleData.HandleDataManager;
import com.xdja.collectdata.handleData.entity.KpiHandleResult;
import com.xdja.constant.GlobalConfig;

public class KpiTestView extends BaseChartView {

	/**
	 * serial UID auto generated
	 */
	private static final long serialVersionUID = -9002331611054515951L;
	private static final String NOMESSGE = "无页面加载数据，请在测试App上切换页面收集数据！！";
	private boolean stopFlag = false;
	private Thread kpiThread, kdatathread;
	private List<KpiData> KpiData = null;
	private List<KpiHandleResult> kpiHandleList = new ArrayList<>(12);
	private List<KpiHandleResult> errorList = new ArrayList<>(12);

	private CategoryPlot mPlot;
	private DefaultCategoryDataset mDataset = null;

	public KpiTestView(String chartContent, String title, String yaxisName) {
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
		addJpanel(chartPanel);
	}

	/**
	 * 开始测试
	 * 
	 * @param packageName
	 * @throws InterruptedException
	 */
	public void start(String packageName) throws InterruptedException {

		kdatathread = new Thread(new Runnable() {

			public void run() {
				// TODO Auto-generated method stub

				CollectDataImpl.startCollectKpiData(packageName);
			}
		});
		kdatathread.start();
		kpiThread = new Thread(new Runnable() {
			@Override
			public void run() {
				// TODO Auto-generated method stub
				stopFlag = false;
				while (true) {
					if (stopFlag) {
						break;
					}

					KpiData = CollectDataImpl.getKpiData();
					if (KpiData != null) {
						int listSize = KpiData.size();
						// mDataset = new DefaultCategoryDataset();
						for (int i = 0; i < listSize; i++) {
							mDataset.addValue(KpiData.get(i).loadTime, "kpi", KpiData.get(i).currentPage);
						}
						if (mPlot != null) {
							mPlot.setDataset(mDataset);
						}
					}
					
					handleKpiData(KpiData);
					try {
						Thread.sleep(GlobalConfig.collectMIDDLEInterval);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		});
		kdatathread.join(1000);
		kpiThread.start();
	}

	/**
	 *  处理kpi相关的数据
	 */
	private void handleKpiData(List<KpiData> kpiList){
		kpiHandleList = HandleDataManager.getInstance().handleKpiData(kpiList);
		if (kpiHandleList == null || kpiHandleList.size() < 1) {
			return;
		}
		//将错误信息展示
		for(KpiHandleResult kpiHandle : kpiHandleList){
			if (!kpiHandle.result) {
				if (errorList.contains(kpiHandle)) {
					continue;
				}
				errorList.add(kpiHandle);
				mShowMessageView.append(formatErrorInfo(kpiHandle, kpiHandle.testValue, "页面加载时间过长"));
			}
		}
	}
	
	/**
	 *  返回kpi相关的处理数据
	 * @return
	 */
	public List<KpiHandleResult> getHandleKpiList() {
		return kpiHandleList;
	}
	
	public void stop() {
		stopFlag = true;
		CollectDataImpl.stopCollectKpiData();
	}

	public void clear() {
		if (KpiData != null) {
			KpiData.clear();
		}
	}

}