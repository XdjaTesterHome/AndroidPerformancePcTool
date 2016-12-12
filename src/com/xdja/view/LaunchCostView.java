package com.xdja.view;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import org.apache.log4j.Logger;

import com.xdja.constant.Constants;
import com.xdja.monitor.ControllerMonitor;

public class LaunchCostView extends JFrame{

	/**
	 * serial UID is auto generated 
	 */
	private static final long serialVersionUID = -5973398787394281648L;
	private Logger logger = Logger.getLogger(LaunchCostView.class);
	private String title;
	private JPanel frame;
	private JTextArea textArea;
	private JLabel labelPackage;
	private JLabel labelActivity;
	private JLabel labelLoops;
	private JTextField textPackage;
	private JTextField textActivity;
	private JTextField textLoops;
	private JButton btnStartTest;
	private boolean isStarted = false;
	
	public LaunchCostView(String title) {
		this.title = title;
		this.frame = new JPanel();
		setTitle(String.format("%s Version 1_0", this.title));
		setBounds(0, 150, 650, 700);
		add(frame);
		setVisible(true);
	}
	
	public void createParts() {
		frame.setLayout(null);

		textArea = new JTextArea();
		textArea.setLineWrap(true);
		textArea.setWrapStyleWord(true);
		textArea.setBounds(10, 10, 600, 452);
		frame.add(textArea);

		labelPackage = new JLabel(Constants.PACKAGE_NAME);
		frame.add(labelPackage);
		labelPackage.setBounds(15, 510, 95, 25);

		textPackage = new JTextField();
		frame.add(textPackage);
		textPackage.setBounds(130, 510, 180, 30);

		labelActivity = new JLabel(Constants.ACTIVITY_NAME);
		frame.add(labelActivity);
		labelActivity.setBounds(15, 550, 95, 25);

		textActivity = new JTextField();
		frame.add(textActivity);
		textActivity.setBounds(130, 550, 180, 30);
		
		labelLoops = new JLabel(Constants.LOOPS);
		frame.add(labelLoops);
		labelLoops.setBounds(15, 590, 45, 25);
		
		textLoops = new JTextField();
		frame.add(textLoops);
		textLoops.setBounds(130, 590, 180, 30);

		btnStartTest = new JButton(Constants.START_TEST);
		frame.add(btnStartTest);
		btnStartTest.setBounds(390, 550, 200, 35);
		
		addListeners();
	}
	
	private void addListeners() {
		btnStartTest.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				
				if (btnStartTest.getText().equals(Constants.START_TEST)) {
					
					Thread thread = new Thread(new Runnable() {
						@Override
						public void run() {
							logger.info("loop times is: " + textLoops.getText());
							logger.info("package name is: " + textPackage.getText());
							logger.info("activity name is: " + textActivity.getText());
							int loops = Integer.valueOf(textLoops.getText());
							String packageName = textPackage.getText();
							String activityName = textActivity.getText();
							String launchName = packageName + Constants.SEPERATOR + activityName;
							int index = 1;
							isStarted = true;
							while (index <= loops && isStarted) {
								// TODO Auto-generated method stub
								btnStartTest.setText(Constants.STOP_TEST);
								double info = ControllerMonitor.getInstance().getLaunchController().getInfo(launchName);
								textArea.append("第"+index+"次启动 " + launchName + "：" + info +"ms\n");
								logger.info("第"+index+"次启动 " + launchName + "：" + info +"ms\n");
								index ++;
								try {
									Thread.sleep(500);
								} catch (InterruptedException e) {
									e.printStackTrace();
								}
							}
							isStarted = false;
							btnStartTest.setText(Constants.START_TEST);
						}
					});
					
					thread.start();
					
				} else {
					isStarted = false;
					btnStartTest.setText(Constants.START_TEST);
				}
			}
		});
	}

}
