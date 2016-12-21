package com.xdja.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.Timer;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.StandardChartTheme;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;

import com.xdja.collectdata.CollectDataImpl;
import com.xdja.collectdata.KpiData;
import com.xdja.constant.Constants;
import com.xdja.constant.GlobalConfig;

public class KpiTestView extends BaseChartView {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Timer mTaskTimer;
	private List<KpiData> mKpiDataList;
	private CategoryPlot mPlot;

	public KpiTestView(String title, String value) {
		super();
		JFreeChart mChart = ChartFactory.createBarChart(title, "当前页面","时间(ms)", null, PlotOrientation.VERTICAL, true,
				true, true);
		// 设置内部属性
		mPlot = (CategoryPlot) mChart.getPlot();
		// 设置纵轴 和横轴
		CategoryAxis mDomainAxis = mPlot.getDomainAxis();
		// 设置柱状图距离x轴最左端的距离百分比为10%
		mDomainAxis.setUpperMargin(0.1);

		ChartPanel chartPanel = new ChartPanel(mChart);
		chartPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4),
				BorderFactory.createLineBorder(Color.black)));
		add(chartPanel);

	}

	ActionListener listener = new ActionListener() {

		@Override
		public void actionPerformed(ActionEvent e) {
			// TODO Auto-generated method stub
			mKpiDataList = CollectDataImpl.getKpiData();
			if (mKpiDataList == null || mKpiDataList.size() < 1) {
				return;
			}

			for (KpiData kpiData : mKpiDataList) {
				if (kpiData == null) {
					continue;
				}
				
				addCostTimeObservation(kpiData.currentPage, kpiData.loadTime);
			}

		}
	};

	/**
	 * 开始测试
	 */
	public void startTest() {
		CollectDataImpl.startCollectKpiData(GlobalConfig.PackageName);
		if (mTaskTimer == null) {
			mTaskTimer = new Timer(GlobalConfig.collectInterval, listener);
		}

		if (!mTaskTimer.isRunning()) {
			mTaskTimer.start();
		}

	}

	/**
	 * 结束测试
	 */
	public void stopTest() {
		if (mTaskTimer != null && mTaskTimer.isRunning()) {
			mTaskTimer.stop();
		}
	}

	/**
	 * Adds an observation to the 'total memory' time series.
	 *
	 * @param y
	 *            the total memory used.
	 */
	private void addCostTimeObservation(String pageName, float costTime) {
		if (mPlot != null) {
			DefaultCategoryDataset mDataset = new DefaultCategoryDataset();
			mDataset.addValue(costTime, Constants.KPI, pageName);
			mPlot.setDataset(mDataset);
		}
	}
}
