package com.xdja.view;

import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import org.apache.log4j.Logger;

import com.xdja.constant.Constants;

public class FpsView extends JFrame {
	
	/**
	 * serial UID is auto generated 
	 */
	private static final long serialVersionUID = -5973398787394281648L;
	private Logger logger = Logger.getLogger(FpsView.class);
	private String title;
	private JPanel frame;
	private FpsChartPanel chartFps;
	private JButton btnStart;
	
	public FpsView(String title) {
		this.title = title;
		this.frame = new JPanel();
		setTitle(String.format("%s Version 1_0", this.title));
		setBounds(0, 150, 750, 600);
		add(frame);
		setVisible(true);
	}
	
	public void createParts() {
		frame.setLayout(null);
		
		chartFps = new FpsChartPanel(Constants.FPS, Constants.FPS, Constants.FPS_UNIT);
	    frame.add(chartFps);
	    Rectangle rect = new Rectangle(50, 20, 600, 400);
	    chartFps.setBounds(rect);
	    
	    btnStart = new JButton(Constants.START_TEST);
		frame.add(btnStart);
		btnStart.setBounds(250, 460, 200, 35);
	    
		addListeners();
	}
	
	private void addListeners() {
		btnStart.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (btnStart.getText().equals(Constants.START_TEST)) {
					logger.info("click start button~");
					chartFps.start();
					btnStart.setText(Constants.STOP_TEST);
				} else {
					logger.info("click stop button~");
					chartFps.stop();
					btnStart.setText(Constants.START_TEST);
				}
				}
			});
	}
	
}
