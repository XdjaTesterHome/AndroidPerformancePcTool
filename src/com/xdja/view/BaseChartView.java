package com.xdja.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Box;
import javax.swing.JPanel;
import javax.swing.Timer;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.StandardChartTheme;

import com.xdja.collectdata.handleData.HandleDataResult;
import com.xdja.util.CommonUtil;

public abstract class BaseChartView extends JPanel{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static Font FONT = new Font("宋体", Font.PLAIN, 12);
	protected Timer mTaskTimer;
	protected ActionListener mActionListener;
	protected List<HandleDataResult> mHandleDataList = new ArrayList<>();
	protected ShowMessageView mShowMessageView;
	private boolean isFirstShowError = true;  // 是否第一次展示错误信息
	
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
	
	/**
	 * 设置JPanel的布局
	 * 
	 */
	protected void addJpanel(JPanel jpanel) {
		Box horizontalBox = Box.createHorizontalBox();
		horizontalBox.add(jpanel);
		horizontalBox.add(Box.createHorizontalStrut(50));
		mShowMessageView = new ShowMessageView();
		horizontalBox.add(mShowMessageView);
		add(horizontalBox);
	}
	
    /**
     *  获取处理过的数据
     * @return
     */
    public List<HandleDataResult> getHandleResult(){
    	return mHandleDataList;
    }
    
    
    /**
     *  格式化错误信息
     * @param result
     * @return
     */
    protected String formatErrorInfo(HandleDataResult result, String value, String errorInfo){
    	StringBuilder sbBuilder = new StringBuilder("===================== \n");
    	if (!CommonUtil.strIsNull(errorInfo)) {
			sbBuilder.append(errorInfo).append("\n");
		}
    	sbBuilder.append("ActivityName = ").append(result.activityName).append("\n");
    	sbBuilder.append("当前测试值= ").append(value).append("\n");
    	sbBuilder.append("Logfile= ").append(result.logPath).append("\n");
    	sbBuilder.append("截屏路径= ").append(result.screenshotsPath).append("\n");
    	sbBuilder.append("===================== \n\n\n\n");
    	return sbBuilder.toString();
    }
    
    protected void appendErrorInfo(String msg) {
		if (CommonUtil.strIsNull(msg)) {
			return;
		}
		
		if (mShowMessageView != null) {
			if (isFirstShowError) {
				mShowMessageView.setText("");
				isFirstShowError = false;
			}
			
			mShowMessageView.append(msg);
		}
	}
}
