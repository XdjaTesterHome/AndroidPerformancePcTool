package com.xdja.collectdata;

/**
 *  页面加载时间的封装类
 * @author zlw
 *
 */
public class KpiData {
	public String currentPage;
	public int loadTime;
	public KpiData(String currentPage, int loadTime) {
		super();
		this.currentPage = currentPage;
		this.loadTime = loadTime;
	}
	
	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return "currentPage = " + currentPage + ", loadTime = " + this.loadTime;
	}
	
	
	@Override
	public boolean equals(Object obj) {
		// TODO Auto-generated method stub
		return this.currentPage.equals(((KpiData)obj).currentPage);
	}
	
	@Override
	public int hashCode() {
		// TODO Auto-generated method stub
		return this.currentPage.hashCode();
	}

	public int getLoadTime() {
		return loadTime;
	}

	public void setLoadTime(int loadTime) {
		this.loadTime = loadTime;
	}
}
