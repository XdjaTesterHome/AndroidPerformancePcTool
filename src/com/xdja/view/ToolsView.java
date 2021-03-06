package com.xdja.view;

import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JPanel;

import com.xdja.adb.AdbManager;
import com.xdja.adb.AndroidSdk;
import com.xdja.collectdata.SaveEnvironmentManager;
import com.xdja.collectdata.thread.ScreenCaptureThread;
import com.xdja.constant.Constants;
import com.xdja.constant.GlobalConfig;
import com.xdja.util.CommonUtil;
import com.xdja.util.ExecShellUtil;
import com.xdja.util.SwingUiUtil;
import com.xdja.util.SwingUiUtil.chooseFileListener;

/**
 * 实用工具的面板
 * 
 * @author zlw
 *
 */
public class ToolsView extends JPanel implements ActionListener {

	private final static String TRACEBUTTON = "打开Trace文件";
	private final static String SCREENSHOT = "截屏";
	private final static String MEMORYTRACE = "抓取内存快照";
	
	private String mCurTestPackageName = "";
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public ToolsView() {
		setBackground(Color.gray);
		setLayout(new FlowLayout(FlowLayout.LEFT));
		layoutButton();
	}

	/**
	 * 用来摆放Button
	 */
	private void layoutButton() {
		// trace method
		JButton traceBtn = new JButton(TRACEBUTTON);
		traceBtn.setBounds(new Rectangle(10, 10, 100, 50));
		traceBtn.addActionListener(this);
		add(traceBtn);

		// 截屏
		JButton screenShotBtn = new JButton(SCREENSHOT);
		screenShotBtn.setBounds(new Rectangle(70, 10, 100, 50));
		screenShotBtn.addActionListener(this);
		add(screenShotBtn);
		
		// memory trace
		JButton memoryBtn = new JButton(MEMORYTRACE);
		memoryBtn.setBounds(new Rectangle(70, 10, 100, 50));
		memoryBtn.addActionListener(this);
		add(memoryBtn);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub
		String cmd = e.getActionCommand();
		switch (cmd) {
		case TRACEBUTTON: // 打开Trace 文件
			File file = new File(Constants.METHOD_TRACE);
			if (!file.exists()) {
				file.mkdirs();
			}
			SwingUiUtil.getInstance().showChooseFileDialog(this, file.getAbsolutePath(), new chooseFileListener() {

				@Override
				public void chooseFile(File chooseFile) {
					// TODO Auto-generated method stub
					if (chooseFile != null) {
						if (chooseFile.isDirectory()) {
							return;
						}

						if (chooseFile.isFile()) {
							// 用TraceView 命令打开
							String cmd = AndroidSdk.traceview().getAbsolutePath() + " " + chooseFile.getAbsolutePath();
							System.out.println("cmd = " + cmd);
							ExecShellUtil.getInstance().execCmdCommand(cmd, false, false);
						}
					}
				}
			});
			break;
		case SCREENSHOT:
			ScreenCaptureThread screenCaptureThread = AdbManager.getInstance().screenCapture(mCurTestPackageName, "", true);
			SwingUiUtil.getInstance().showSaveFileDialog(this, new chooseFileListener() {

				@Override
				public void chooseFile(File chooseFile) {
					// TODO Auto-generated method stub
					if (screenCaptureThread.getBufferedImage() == null) {
						return;
					}
					if (chooseFile != null) {
						System.out.println("chooseFile = " + chooseFile.getAbsolutePath());
						try {
							if (chooseFile.isDirectory()) {

								ImageIO.write(screenCaptureThread.getBufferedImage(), "PNG", new File(chooseFile,
										SaveEnvironmentManager.getInstance().getSuggestedName("") + ".png"));
							} else{
								ImageIO.write(screenCaptureThread.getBufferedImage(), "PNG", chooseFile);
							}
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}
			});
			break;
		case MEMORYTRACE:
			if (CommonUtil.strIsNull(GlobalConfig.DeviceName) || CommonUtil.strIsNull(mCurTestPackageName)) {
				break;
			}
			File memoryFile = new File(Constants.MEMORY_DUMP);
			if (!memoryFile.exists()) {
				memoryFile.mkdirs();
			}
			
			SwingUiUtil.getInstance().showChooseFileDialog(this, memoryFile.getAbsolutePath(), new chooseFileListener(){

				@Override
				public void chooseFile(File chooseFile) {
					// TODO Auto-generated method stub
					if (chooseFile != null) {
						if (chooseFile.isDirectory()) {
							AdbManager.getInstance().dumpMemory(GlobalConfig.DeviceName, mCurTestPackageName, chooseFile.getAbsolutePath() + File.separator + "unkonwName.hprof");
						}

						if (chooseFile.isFile()) {
							AdbManager.getInstance().dumpMemory(GlobalConfig.DeviceName, mCurTestPackageName, chooseFile.getAbsolutePath());
						}
					}
				}
				
			});
			break;
		default:
			break;
		}
	}
	
	/**
	 * 设置当前的测试
	 * @param curTestPkg
	 */
	public void setCurTestPackageName(String curTestPkg){
		mCurTestPackageName = curTestPkg;
	}
}
