package com.xdja.collectdata.entity;

/**
 *  用于封装流量数据
 * @author zlw
 *
 */
public class FpsData {
	public int fps;
	public int dropcount;
	public int framecount;
	public String activityName;
	public FpsData(int fps, int dropcount, int framecount, String activityName) {
		super();
		this.fps = fps;
		this.dropcount = dropcount;
		this.framecount = framecount;
		this.activityName = activityName;
	} 
	
	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return "fps = " + this.fps + ", dropcount = " + this.dropcount + ", framecount = " + framecount + ", activityName =" + activityName;
		
	}
	
	@Override
	public boolean equals(Object obj) {
		// TODO Auto-generated method stub
		return this.activityName.equals(((FpsData)obj).activityName);
	}
	
	@Override
	public int hashCode() {
		// TODO Auto-generated method stub
		return this.activityName.hashCode();
	}
}
