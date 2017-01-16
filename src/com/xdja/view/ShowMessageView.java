package com.xdja.view;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.HeadlessException;
import java.awt.TextArea;

public class ShowMessageView extends TextArea{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public ShowMessageView() throws HeadlessException {
		super();
		// TODO Auto-generated constructor stub
		setPreferredSize(new Dimension(50, 100));
		setEditable(false);
		setEnabled(false);
		setText("暂时无异常信息");
		setForeground(Color.RED);
	}

	public ShowMessageView(int rows, int columns) throws HeadlessException {
		super(rows, columns);
		// TODO Auto-generated constructor stub
	}

	public ShowMessageView(String text, int rows, int columns, int scrollbars) throws HeadlessException {
		super(text, rows, columns, scrollbars);
		// TODO Auto-generated constructor stub
	}

	public ShowMessageView(String text, int rows, int columns) throws HeadlessException {
		super(text, rows, columns);
		// TODO Auto-generated constructor stub
	}

	public ShowMessageView(String text) throws HeadlessException {
		super(text);
		// TODO Auto-generated constructor stub
	}

}
