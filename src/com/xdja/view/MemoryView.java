package com.xdja.view;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.Timer;

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
import com.xdja.constant.GlobalConfig;

public class MemoryView extends JPanel{
	
	/**
	 * serial UID auto generated
	 */
	private static final long serialVersionUID = -9002331611054515951L;
	private TimeSeries totalAlloc;
	private Timer mTaskTimer;
	
	public MemoryView(String chartContent,String title,String yaxisName)  
    {  
        super(new BorderLayout());  
        this.totalAlloc = new TimeSeries("已经分配的内存", Millisecond.class);
        TimeSeriesCollection dataset = new TimeSeriesCollection();
        dataset.addSeries(this.totalAlloc);
        
        DateAxis domain = new DateAxis("Time");
        NumberAxis range = new NumberAxis("Memory(KB)");
        domain.setTickLabelFont(new Font("SansSerif", Font.PLAIN, 12));
        range.setTickLabelFont(new Font("SansSerif", Font.PLAIN, 12));
        domain.setLabelFont(new Font("SansSerif", Font.PLAIN, 14));
        range.setLabelFont(new Font("SansSerif", Font.PLAIN, 14));
        
        XYItemRenderer renderer = new XYLineAndShapeRenderer(true, false);
        renderer.setSeriesPaint(0, Color.red);
//        renderer.setSeriesPaint(1, Color.green);
        renderer.setStroke(
            new BasicStroke(3f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL)
        );
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

        JFreeChart chart = new JFreeChart(
            "应用内存分配情况", 
            new Font("SansSerif", Font.BOLD, 24),
            plot, 
            true
        );
        chart.setBackgroundPaint(Color.white);
        ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createEmptyBorder(4, 4, 4, 4),
            BorderFactory.createLineBorder(Color.black))
        );
        add(chartPanel);
    }
	
	ActionListener actionListener = new ActionListener() {
		
		@Override
		public void actionPerformed(ActionEvent e) {
			// TODO Auto-generated method stub
			float memory = CollectDataImpl.getMemoryData(GlobalConfig.PACKAGENAME);
			addTotalObservation(memory);
		}
	};
	
	public void refreshData(){
		mTaskTimer = new Timer(GlobalConfig.collectInterval, actionListener);
		mTaskTimer.start();
	}
	
	public void stopRefresh(){
		if (mTaskTimer != null) {
			mTaskTimer.stop();
		}
	}
	
    /**
     * Adds an observation to the 'total memory' time series.
     *
     * @param y  the total memory used.
     */
    private void addTotalObservation(double y) {
        this.totalAlloc.add(new Millisecond(), y);
    }
}
