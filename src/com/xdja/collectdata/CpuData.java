package com.xdja.collectdata;

/**
 *  封装CPU相关的数据信息
 * @author zlw
 *
 */
public class CpuData {
	public int lastProcTotal;
	public int lastProcPid;
	public float cpuUsage;
	public CpuData(int lastProcTotal, int lastProcPid, float cpuUsage) {
		super();
		this.lastProcTotal = lastProcTotal;
		this.lastProcPid = lastProcPid;
		this.cpuUsage = cpuUsage;
	}
}
