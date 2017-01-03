package com.xdja.util;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Insets;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.plaf.basic.BasicButtonUI;

import com.xdja.constant.Constants;

/**
 * 用于生成各种对话框的工具类
 * @author zlw
 *
 */
public class SwingUiUtil {
	private volatile static SwingUiUtil mInstance;
	
	private SwingUiUtil(){
		
	}
	
	public static SwingUiUtil getInstance() {
		if (mInstance == null) {
			synchronized (SwingUiUtil.class) {
				if (mInstance == null) {
					mInstance = new SwingUiUtil();
				}
			}
		}
		
		return mInstance;
	}
	
	/**
	 * 展示两个按钮的对话框
	 * @param parentComponent 父控件
	 * @param title 对话框的标题
	 * @param content 对话框的内容
	 * @param ok_text 对话框的确定按钮的文案
	 * @param cancel_text 对话框的取消按钮的文案
	 */
	public void showOkAndCancelDialog(Component parentComponent, String title, String content, String ok_text, String cancel_text, ClickDialogBtnListener listener){
		if (parentComponent == null) {
			return;
		}
		
		if (title == null || "".equals(title)) {
			title = "提醒";
		}
		
		if (content == null || "".equals(content)) {
			content = "你还没有设置文案";
		}
		
		if (ok_text == null || "".equals(ok_text)) {
			ok_text = "确定";
		}
		
		if (cancel_text == null || "".equals(cancel_text)) {
			cancel_text = "取消";
		}
		
		Object[] options = { ok_text, cancel_text };
		JOptionPane warnPane = new JOptionPane(content,
				JOptionPane.QUESTION_MESSAGE, JOptionPane.YES_NO_OPTION, null,
				options, options[1]);
		JDialog dialog = warnPane.createDialog(parentComponent, title);
		dialog.setVisible(true);
		Object selectedValue = warnPane.getValue();
		if (selectedValue == null || selectedValue == options[1]) {
			listener.clickCancelBtn();
		} else if (selectedValue == options[0]) {
			listener.clickOkBtn();
		}
	}
	
	/**
	 *  展示提示性文案的对话框，只有一个确定按钮
	 * @param parentComponent
	 * @param title
	 * @param content
	 * @param ok_text
	 */
	public void showTipsDialog(Component parentComponent, String title, String content, String ok_text, ClickDialogBtnListener listener) {
		if (parentComponent == null) {
			return;
		}
		
		if (title == null || "".equals(title)) {
			title = "提醒";
		}
		
		if (content == null || "".equals(content)) {
			content = "你还没有设置文案";
		}
		
		if (ok_text == null || "".equals(ok_text)) {
			ok_text = "确定";
		}
		
		Object[] options = { ok_text};
		JOptionPane warnPane = new JOptionPane(content,
				JOptionPane.YES_OPTION, JOptionPane.INFORMATION_MESSAGE, null,
				options, options[0]);
		
		JDialog dialog = warnPane.createDialog(parentComponent, title);
		dialog.setVisible(true);
		Object selectedValue = warnPane.getValue();
		if (selectedValue == null || selectedValue == options[0]) {
			if (listener != null) {
				listener.clickOkBtn();
			}
			dialog.setVisible(false);
			dialog.dispose();
		}
		
	}
	
	/**
	 * 用于创建带图标的icon
	 * @param text
	 * @param icon
	 * @return
	 */
	public JButton createBtnWithIcon(String text, String iconName){
		JButton button =  new JButton(text, new ImageIcon(Constants.IMG_PATH + iconName));
		button.setUI(new BasicButtonUI());
		button.setPreferredSize(new Dimension(80, 27));
		button.setContentAreaFilled(false);
		button.setFont(new Font("粗体", Font.PLAIN, 15));
		button.setMargin(new Insets(2, 2, 2, 2));
		return button;
	}
	
	/**
	 * 用于创建带图标的icon
	 * @param text
	 * @param color  背景色
	 * @return
	 */
	public JButton createBtnWithColor(String text, Color color){
		JButton button =  new JButton(text);
		button.setUI(new BasicButtonUI());
		button.setPreferredSize(new Dimension(80, 27));
		button.setContentAreaFilled(true);
		button.setFont(new Font("粗体", Font.PLAIN, 15));
		button.setMargin(new Insets(2, 2, 2, 2));
		button.setBackground(color);
		return button;
	}
	
	/**
	 *  点击对话框按钮的监听事件
	 * @author zlw
	 *
	 */
	public interface ClickDialogBtnListener{
		public void clickOkBtn();
		public void clickCancelBtn();
	}
}
