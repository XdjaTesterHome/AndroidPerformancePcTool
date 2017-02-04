package com.xdja.database;

import java.util.List;


/**
 *  封装数据库查询接口，用于返回相关查询结果
 * @author 
 *
 */
public class PerformanceDBSearch {
	public List<Float> cpuData;
	public List<String> pageData;
	public List<Integer> passData;
	public PerformanceDBSearch(List<Float> cpuData,List<String> pageData,List<Integer> passData){
		super();
		this.cpuData = cpuData;
		this.pageData = pageData;
		this.passData = passData;
	}
	
}