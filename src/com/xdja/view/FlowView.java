package com.xdja.view;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;

import org.apache.log4j.Logger;
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
import com.xdja.collectdata.FlowData;
import com.xdja.constant.GlobalConfig;

public class FlowView extends BaseChartView {

	/**
	 * serial UID is auto generated
	 */
	private static final long serialVersionUID = 1719925024734975743L;
	private TimeSeries flowCost;
	private FlowData mFlowData;
	private float mLastFlow = -1 ;
	private Thread flowThread ;
	private boolean stopFlag = false;
	
	public FlowView(String chartContent, String title, String yaxisName) {
		super();
		this.flowCost = new TimeSeries(chartContent);
		TimeSeriesCollection dataset = new TimeSeriesCollection();
		dataset.addSeries(this.flowCost);

		DateAxis domain = new DateAxis("Time");
		NumberAxis range = new NumberAxis("流量消耗(KB)");
		domain.setTickLabelFont(new Font("SansSerif", Font.PLAIN, 12));
		range.setTickLabelFont(new Font("SansSerif", Font.PLAIN, 12));
		domain.setLabelFont(new Font("SansSerif", Font.PLAIN, 14));
		range.setLabelFont(new Font("SansSerif", Font.PLAIN, 14));

		XYItemRenderer renderer = new XYLineAndShapeRenderer(true, false);
		renderer.setSeriesPaint(0, Color.red);
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
		JFreeChart chart = new JFreeChart(title, new Font("SansSerif", Font.BOLD, 24), plot, true);
		chart.setBackgroundPaint(Color.white);
		ChartPanel chartPanel = new ChartPanel(chart);
		chartPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4),
				BorderFactory.createLineBorder(Color.black)));
		add(chartPanel);
	}

	/**
	 * Adds an observation to the 'total memory' time series.
	 *
	 * @param y
	 *            the total memory used.
	 */
	private void addFlowObservation(double y) {
		this.flowCost.add(new Millisecond(), y);
	}
	
	/**
	 * 开始测试
	 * @param packageName
	 */
	public void start(String packageName) {
		flowThread = new Thread(new Runnable() {
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
				stopFlag = false;
				while (true) {
					if (stopFlag) {
						break;
					}
					
					mFlowData = CollectDataImpl.getFlowData(packageName);
					if (mFlowData != null) {
						if (mLastFlow == -1) {
							addFlowObservation(0);
						}
						addFlowObservation(mFlowData.flowTotal - mLastFlow);
						mLastFlow = mFlowData.flowTotal;
					}
				}
			}
		});
		
		flowThread.start();
	}
	
	public void stop() {
		stopFlag = true;
	}
}
