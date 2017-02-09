package com.xdja.view.custom;

import java.awt.Color;
import java.awt.Font;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class MyJSlider extends JPanel implements ChangeListener{

	/**
	 * 
	 */
	private static final long serialVersionUID = -2137850386165152756L;
	
	private JLabel mJSliderTitle;
	private JSlider mJSlider;
	private JLabel mJSliderValue; // 显示选择的value
	
	/**
	 * 
	 * @param sliderTitle 标题
	 * @param direction 构造的滑动选择条的方向
	 */
	public MyJSlider(int direction, String sliderTitle) {
		super();
		// TODO Auto-generated constructor stub
		if(sliderTitle == null || "".equals(sliderTitle)){
			sliderTitle = "default";
		}
		mJSliderTitle = new JLabel(sliderTitle);
		add(mJSliderTitle);
		
		mJSlider = new JSlider(JSlider.HORIZONTAL);
		mJSlider.setPaintTicks(true);
		mJSlider.setPaintLabels(true);
		mJSlider.setMajorTickSpacing(20);
		mJSlider.setMinorTickSpacing(5);
		mJSlider.addChangeListener(this);
		add(mJSlider);
		
		mJSliderValue = new JLabel();
		add(mJSliderValue);
		
		setBounds(0,  0, 200, 50);
	}
	
	/**
	 *  设置Slider的标题
	 * @param title
	 */
	public void setSliderTitle(String title){
		if(title == null || "".equals(title)){
			title = "default";
		}
	}
	
	/**
	 * 设置slider的最大值
	 * @param max
	 */
	public void setSliderMaxValue(int max){
		if (mJSlider != null) {
			mJSlider.setMaximum(max);
		}
	}
	
	/**
	 * 设置slider的最小值
	 * @param min
	 */
	public void setSliderMinValue(int min){
		if(mJSlider != null){
			mJSlider.setMinimum(min);
		}
	}
	
	/**
	 * 设置slider的当前的值
	 * @param curValue
	 */
	public void setSliderCurValue(int curValue){
		if (mJSlider != null) {
			mJSlider.setValue(curValue);
		}
	}
	
	/**
	 * 获取当前Slider的值
	 */
	public int getSliderValue(){
		if (mJSlider != null) {
			return mJSlider.getValue();
		}
		
		return 0;
	}

	@Override
	public void stateChanged(ChangeEvent e) {
		// TODO Auto-generated method stub
		if (e.getSource() instanceof JSlider) {
			JSlider source = (JSlider) e.getSource();
			if (mJSliderValue != null) {
				mJSliderValue.setText("选择：" + String.valueOf(source.getValue()));
				mJSliderValue.setForeground(Color.red);
				mJSliderValue.setFont(new Font("宋体",  Font.PLAIN, 20));
			}
		}
	}
	
	/**
	 * 设置一个大格子的间隔
	 * @param spac
	 */
	public void setMajorTickSpacing(int spac){
		if (spac <= 0) {
			spac = 1;
		}
		if (mJSlider != null) {
			mJSlider.setMajorTickSpacing(spac);
		}
	}
	
	/**
	 * 设置一个小格子的间隔
	 * @param spac
	 */
	public void setMinTickSpacing(int spac){
		if (spac < 0) {
			spac = 0;
		}
		if (mJSlider != null) {
			mJSlider.setMinorTickSpacing(spac);
		}
	}
}
