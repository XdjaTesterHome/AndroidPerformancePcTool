package com.xdja.view;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.concurrent.ExecutionException;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.SwingWorker;
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
import com.xdja.collectdata.FpsData;
import com.xdja.constant.GlobalConfig;

public class FpsView extends BaseChartView{
	
	/**
	 * serial UID auto generated
	 */
	private static final long serialVersionUID = -9002331611054515951L;
	private TimeSeries totalFps;
	private Timer mTaskTimer;
	
	public FpsView(String chartContent,String title,String yaxisName)  
    {  
        super();  
        this.totalFps = new TimeSeries("当前应用帧率");
        TimeSeriesCollection dataset = new TimeSeriesCollection();
        dataset.addSeries(this.totalFps);
        
        DateAxis domain = new DateAxis("Time");
        NumberAxis range = new NumberAxis("Fps");
        domain.setTickLabelFont(new Font("SansSerif", Font.PLAIN, 12));
        range.setTickLabelFont(new Font("SansSerif", Font.PLAIN, 12));
        domain.setLabelFont(new Font("SansSerif", Font.PLAIN, 14));
        range.setLabelFont(new Font("SansSerif", Font.PLAIN, 14));
        
        XYItemRenderer renderer = new XYLineAndShapeRenderer(true, false);
        renderer.setSeriesPaint(0, Color.red);
//        renderer.setSeriesPaint(1, Color.green);
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

        JFreeChart chart = new JFreeChart(
            "应用Fps情况", 
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
        setActionListener(actionListener);
    }
	
	ActionListener actionListener = new ActionListener() {
		
		@Override
		public void actionPerformed(ActionEvent e) {
			// TODO Auto-generated method stub
			
			
			final SwingWorker<FpsData, Void> worker = new SwingWorker<FpsData, Void>() {

				@Override
				protected FpsData doInBackground() throws Exception {
					// TODO Auto-generated method stub
					FpsData fpsdata = CollectDataImpl.getFpsData(GlobalConfig.PackageName);
					return fpsdata;
				}
				
				protected void done() {
					FpsData Fpsdata =null;
					try {
						Fpsdata = get();
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (ExecutionException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				
					addTotalObservation(Fpsdata);
				};
		    };
		    worker.run(); 
		}
	
	
	};
	
    /**
     * Adds an observation to the 'total memory' time series.
     *
     * @param fpsdata  the total memory used.
     */
    private void addTotalObservation(double fpsdata)  {
        this.totalFps.add(new Millisecond(), fpsdata);
    }
}


//import java.util.Arrays;
//	class tFpsView
//	{
//		public static void main(String[] args)
//		{
//		FpsData[] fpsdata ={};
//		for( int i=0;i<3 ; i++){
//			FpsData getfpsdata = CollectDataImpl.getFpsData("com.xdja.safekeyservice");
//			if (getfpsdata!=null  ){
//			  fpsdata =insertfps(fpsdata,getfpsdata);
//        }
//		}
//		System.out.println(Arrays.deepToString(fpsdata));
//		System.out.println(fpsdata[2]);
//		
//		}
//
//     private static FpsData[] insertfps(FpsData[] arr, FpsData str)
//     {
//		int size = arr.length;
//		FpsData[] tmp = new FpsData[size + 1];
//		System.arraycopy(arr, 0, tmp, 0, size);
//		tmp[size] = str;
//		return tmp;
//		}
//		}