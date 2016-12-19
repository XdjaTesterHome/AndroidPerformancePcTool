package com.xdja.util;

import java.text.DecimalFormat;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/***
 *  公共的工具
 * @author zlw
 *
 */
public class CommonUtil {
	
	/**
	 *  保留两位小数
	 * @param number
	 * @return
	 */
	public static float getTwoDots(float number){
		DecimalFormat df = new DecimalFormat("######0.00");
		return Float.parseFloat(df.format(number));
	}
	
	/**
	 *  将字符串中的多个空格，替换成一个空格
	 * @param contents
	 * @return
	 */
	public static String formatBlanksToBlank(String contents) {
		Pattern p = Pattern.compile("\\s+");
		Matcher m = p.matcher(contents);
		contents = m.replaceAll(" ");
		return contents;
	}
}
