package com.xdja.util;

import java.awt.Component;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

import com.xdja.constant.Constants;

/**
 * 用于生成各种对话框的工具类
 * @author zlw
 *
 */
public class DialogUtil {
	private volatile static DialogUtil mInstance;
	
	private DialogUtil(){
		
	}
	
	public static DialogUtil getInstance() {
		if (mInstance == null) {
			synchronized (DialogUtil.class) {
				if (mInstance == null) {
					mInstance = new DialogUtil();
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
		
		Object[] options = { Constants.CONFIRM, Constants.CANCEL };
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
	 *  点击对话框按钮的监听事件
	 * @author zlw
	 *
	 */
	public interface ClickDialogBtnListener{
		public void clickOkBtn();
		public void clickCancelBtn();
	}
}
