package com.xdja.util;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

/**
 * 读写Excel表格的工具类
 * 
 * @author zlw
 */
public class ExcelUtil {
	
	private Workbook mCurWorkBook;
	private String mCurFilePath;
	
	public ExcelUtil(String filePath) {
		super();
		// TODO Auto-generated constructor stub
		mCurWorkBook = createWorkBook(filePath);
		mCurFilePath = filePath;
	}

	/**
	 * 创建WorkBook
	 * 
	 * 
	 * @param filePath
	 *            workbook的名称
	 * @return
	 */
	public Workbook createWorkBook(String filePath) {
		if (filePath == null || "".equals(filePath)) {
			return null;
		}
		Workbook workbook = null;
		if (filePath.endsWith("xls")) {
			workbook = new HSSFWorkbook();
		} else if (filePath.endsWith("xlsx")) {
			workbook = new XSSFWorkbook();
		} else {
			workbook = new XSSFWorkbook();
			filePath = filePath + ".xlsx";
		}

		return workbook;
	}

	/**
	 * 创建一个表单
	 * 
	 * @return
	 */
	public  Sheet createSheet(Workbook workbook, String sheetName) {
		if (workbook == null) {
			return null;
		}

		if (sheetName == null || "".equals(sheetName)) {
			return null;
		}
		Sheet sheet = null;
		try{
			sheet = workbook.getSheet(sheetName);
			if (sheet == null) {
				sheet = workbook.createSheet(sheetName);
			}
		}catch (IllegalArgumentException e) {
			// TODO: handle exception
		}

		return sheet;
	}

	/**
	 * 将数据写入到Excel
	 * 
	 * @param titles
	 * @param values
	 * @param filePath
	 * @param sheetName
	 * 
	 */
	public void writeDataToExcel(String sheetName, String[] titles, List<String[]> values) {
		if (values.size() < 1) {
			return;
		}

		if (mCurWorkBook == null) {
			return;
		}
		
		if (mCurFilePath == null) {
			String defaultPath = System.getProperty("user.home");
			mCurFilePath = defaultPath + "\\performance\\" + "default.xlsx"; 
		}
		
		Sheet sheet = createSheet(mCurWorkBook, sheetName);
		if (sheet == null) {
			return;
		}

		CellStyle titleStyle;
		CellStyle contentStyle;
		if (mCurFilePath.endsWith("xls")) {
			titleStyle = (HSSFCellStyle) mCurWorkBook.createCellStyle();
			titleStyle.setAlignment(HorizontalAlignment.CENTER);
			titleStyle.setVerticalAlignment(VerticalAlignment.CENTER);

			contentStyle = (HSSFCellStyle) mCurWorkBook.createCellStyle();
			contentStyle.setAlignment(HorizontalAlignment.LEFT);
			contentStyle.setVerticalAlignment(VerticalAlignment.CENTER);
		} else {
			titleStyle = (XSSFCellStyle) mCurWorkBook.createCellStyle();
			titleStyle.setAlignment(HorizontalAlignment.CENTER);
			titleStyle.setVerticalAlignment(VerticalAlignment.CENTER);

			contentStyle = (XSSFCellStyle) mCurWorkBook.createCellStyle();
			contentStyle.setAlignment(HorizontalAlignment.LEFT);
			contentStyle.setVerticalAlignment(VerticalAlignment.CENTER);
		}

		// 插入标题
		if (titles.length > 0) {
			if (mCurFilePath.endsWith("xls")) {
				HSSFRow titleRow = (HSSFRow) sheet.createRow(0);
				for (int i = 0; i < titles.length; i++) {
					HSSFCell cell = titleRow.createCell(i);
					cell.setCellValue(titles[i]);
					cell.setCellStyle(titleStyle);
				}
			} else {
				XSSFRow xssfRow = (XSSFRow) sheet.createRow(0);
				for (int i = 0; i < titles.length; i++) {
					XSSFCell cell = xssfRow.createCell(i);
					cell.setCellValue(titles[i]);
					cell.setCellStyle(titleStyle);
				}
			}
		}

		// 插入数据
		int count = 1;
		for (String[] datas : values) {

			if (datas.length < 1) {
				return;
			}

			if (mCurFilePath.endsWith("xls")) {
				HSSFRow hssfRow = (HSSFRow) sheet.createRow(count);
				for (int i = 0; i < datas.length; i++) {
					HSSFCell cell = hssfRow.createCell(i);
					cell.setCellValue(datas[i]);
					cell.setCellStyle(contentStyle);
				}

			} else {
				XSSFRow xssfRow = (XSSFRow) sheet.createRow(count);
				for (int i = 0; i < datas.length; i++) {
					XSSFCell cell = xssfRow.createCell(i);
					cell.setCellValue(datas[i]);
					cell.setCellStyle(contentStyle);
				}
			}
			count += 1;
		}
		
		FileOutputStream foStream = null;
		try{
			foStream = new FileOutputStream(mCurFilePath);
			mCurWorkBook.write(foStream);
		}catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}finally {
			if (foStream != null) {
				try {
					foStream.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}
	
	/**
	 * 释放workbook
	 */
	public void releaseWorkBook(){
		if (mCurWorkBook != null) {
			try {
				mCurWorkBook.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}
