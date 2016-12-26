package com.xdja.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionListener;

import javax.swing.JPanel;
import javax.swing.Timer;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.StandardChartTheme;

import com.xdja.constant.GlobalConfig;

public abstract class BaseChartView extends JPanel{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static Font FONT = new Font("宋体", Font.PLAIN, 12);
	protected Timer mTaskTimer;
	protected ActionListener mActionListener;
	public BaseChartView() {
		super(new BorderLayout());
		// TODO Auto-generated constructor stub
		initCommonChart();
	}
	
	/**
	 * 初始化chart公共的部分
	 */
	protected void initCommonChart(){
		
		// 设置中文主题样式 解决乱码
		StandardChartTheme chartTheme = new StandardChartTheme("CN");
		// 设置标题字体
		chartTheme.setExtraLargeFont(FONT);
		// 设置图例的字体
		chartTheme.setRegularFont(FONT);
		// 设置轴向文字
		chartTheme.setLargeFont(new Font("宋体", Font.PLAIN, 15));
		//Paint 可以理解为绘制颜色；标题字体颜色
		chartTheme.setTitlePaint(new Color(51, 51, 51));
		// 设置标注背景色
		chartTheme.setLegendBackgroundPaint(Color.WHITE);
		//设置字体颜色
		chartTheme.setLegendItemPaint(Color.BLACK);
		//图表背景色
		chartTheme.setChartBackgroundPaint(Color.WHITE);
		// 绘制区域背景色
		chartTheme.setPlotBackgroundPaint(Color.gray);
		// 绘制区域外边框
		chartTheme.setPlotOutlinePaint(Color.WHITE);
		
		ChartFactory.setChartTheme(chartTheme);
	}
}
