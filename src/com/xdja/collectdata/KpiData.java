package com.xdja.collectdata;

/**
 *  页面加载时间的封装类
 * @author zlw
 *
 */
public class KpiData {
	public String currentPage;
	public float loadTime;
	public KpiData(String currentPage, float loadTime) {
		super();
		this.currentPage = currentPage;
		this.loadTime = loadTime;
	}
	
	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return "currentPage = " + currentPage + ", loadTime = " + this.loadTime;
	}
	
}
