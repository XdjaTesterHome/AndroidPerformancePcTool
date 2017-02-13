package com.xdja.view;

import java.awt.Checkbox;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import com.xdja.constant.Constants;
import com.xdja.util.ProPertiesUtil;
import com.xdja.util.SwingUiUtil;

/**
 * 用于保存测试数据的配置页面
 * 
 * @author zlw
 *
 */
public class SaveTestDataSettingDialog extends JDialog implements ItemListener, ActionListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1998104508054845824L;
	private Checkbox dbCheckbox, localCheckbox;
	private JTextField ipTextField, portTextField, dbNameTextField, localTextField, dbUserTextField, dbPwdTextField;
	private JPanel dbSettingPanel, localSettingPanel;
	private JButton mSaveButton;
	
	public SaveTestDataSettingDialog(Frame owner, String title) {
		super(owner, title);
		// TODO Auto-generated constructor stub
		// 是否使用数据库
		dbCheckbox = new Checkbox("使用数据库", false);
		dbCheckbox.setPreferredSize(new Dimension(500, 70));
		dbCheckbox.addItemListener(this);
		dbSettingPanel = initDBSettingPanel();

		// 是否保存到本地
		localCheckbox = new Checkbox("保存到本地", true);
		localCheckbox.setPreferredSize(new Dimension(500, 70));
		localCheckbox.addItemListener(this);
		localSettingPanel = initLocalSettingPanel();
		setdbSettingEnable(false);
		
		// 保存配置按钮
		mSaveButton = new JButton("保存配置");
		mSaveButton.setPreferredSize(new Dimension(300, 50));
		mSaveButton.addActionListener(this);
		
		
		add(dbCheckbox);
		add(dbSettingPanel);
		add(localCheckbox);
		add(localSettingPanel);
		add(mSaveButton);
		setLayout(new FlowLayout(FlowLayout.LEFT));
		setBounds(600, 260, 700, 600);
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
	}

	/**
	 * 初始化数据库配置的面板
	 * 
	 * @return
	 */
	private JPanel initDBSettingPanel() {
		JPanel jPanel = new JPanel();

		// 数据库ip地址
		JLabel iptitle = new JLabel("ip地址：          ");
		ipTextField = new JTextField(30);
		ipTextField.setText("localhost");
		jPanel.add(iptitle);
		jPanel.add(ipTextField);

		// 数据库port地址
		JLabel porttitle = new JLabel("port：             ");
		portTextField = new JTextField(30);
		portTextField.setText("3306");
		jPanel.add(porttitle);
		jPanel.add(portTextField);

		// 数据库名称
		JLabel dbNametitle = new JLabel("数据库名称：");
		dbNameTextField = new JTextField(30);
		dbNameTextField.setText("Performancedata");
		jPanel.add(dbNametitle);
		jPanel.add(dbNameTextField);
		// 数据库名称
		JLabel dbUsertitle = new JLabel("数据库用户名：");
		dbUserTextField = new JTextField(30);
		dbUserTextField.setText("root");
		jPanel.add(dbUsertitle);
		jPanel.add(dbUserTextField);
		// 数据库名称
		JLabel dbPwdtitle = new JLabel("数据库密码：");
		dbPwdTextField = new JTextField(30);
		dbPwdTextField.setText("123456");
		jPanel.add(dbPwdtitle);
		jPanel.add(dbPwdTextField);
		

		jPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
		jPanel.setPreferredSize(new Dimension(500, 150));
		return jPanel;
	}

	/**
	 * 初始化保存数据到本地的面板
	 * 
	 * @return
	 */
	private JPanel initLocalSettingPanel() {
		JPanel jPanel = new JPanel();

		// 本地路径
		JLabel localNametitle = new JLabel("保存路径：");
		localTextField = new JTextField(40);
		String defaultPath = System.getProperty("user.home");
		localTextField.setText(defaultPath+"\\performance");
		jPanel.add(localNametitle);
		jPanel.add(localTextField);

		jPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
		jPanel.setPreferredSize(new Dimension(500, 100));
		return jPanel;
	}

	@Override
	public void itemStateChanged(ItemEvent e) {
		// TODO Auto-generated method stub
		Checkbox checkbox = (Checkbox) e.getSource();
		String label = checkbox.getLabel();
		switch (label) {
		case "使用数据库":
			if (dbCheckbox != null && !dbCheckbox.getState()) {
				dbCheckbox.setState(true);
				setdbSettingEnable(true);
				
			} else {
				localCheckbox.setState(false);
				setdbSettingEnable(true);
			}
			break;
		case "保存到本地":
			if (localCheckbox != null && !localCheckbox.getState()) {
				localCheckbox.setState(true);
				setdbSettingEnable(false);
			} else {
				dbCheckbox.setState(false);
				setdbSettingEnable(false);
			}
			break;
		default:
			break;
		}
	}
	
	/**
	 *  设置db设置页面是否可以使用
	 * @param enable true 可用
	 *               false 不可用
	 * 
	 */
	private void setdbSettingEnable(boolean enable){
		if (dbSettingPanel != null) {
			ipTextField.setEditable(enable);
			portTextField.setEditable(enable);
			dbNameTextField.setEditable(enable);
			dbUserTextField.setEditable(enable);
			dbPwdTextField.setEditable(enable);
		}
		if (localSettingPanel != null) {
			localTextField.setEditable(!enable);
		}
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub
		String command = e.getActionCommand();
		switch (command) {
		case "保存配置":
			saveDataToProprtites();
			break;

		default:
			break;
		}
	}
	
	/**
	 *  将数据保存到配置文件中
	 */
	private void saveDataToProprtites(){
		if (dbCheckbox != null) {
			boolean choose = dbCheckbox.getState();
			String chooseStr = choose ? "true" : "false";
			ProPertiesUtil.getInstance().writeProperties(Constants.DBSAVE_CHOOSE, chooseStr);
			if (choose) {
				if (ipTextField != null) {
					String ip = ipTextField.getText();
					ProPertiesUtil.getInstance().writeProperties(Constants.DBIP_SETTING, ip);
				}
				
				if (portTextField != null) {
					String port = portTextField.getText();
					ProPertiesUtil.getInstance().writeProperties(Constants.DBPORT_SETTING, port);
				}
				
				if (dbNameTextField !=null) {
					String dbName = dbNameTextField.getText();
					ProPertiesUtil.getInstance().writeProperties(Constants.DBNAME_SETTING, dbName);
				}
				
				if (dbUserTextField != null) {
					String user = dbUserTextField.getText();
					ProPertiesUtil.getInstance().writeProperties(Constants.DBUSER_SETTING, user);
				}
				
				if (dbPwdTextField != null) {
					String pwd = dbPwdTextField.getText();
					ProPertiesUtil.getInstance().writeProperties(Constants.DBPWD_SETTING, pwd);
				}
			}else {
				if (localTextField != null) {
					String localPath = localTextField.getText();
					ProPertiesUtil.getInstance().writeProperties(Constants.LOCALSAVE_SETTING, localPath);
				}
			}
		}
		SwingUiUtil.getInstance().showTipsDialog(this, "提示", "配置已经保存成功", "确定", null);
	}
}
