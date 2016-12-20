package com.xdja.collectdata;

/**
 *  用于封装流量数据
 * @author zlw
 *
 */
public class FpsData {
	public int fps;
	public int dropcount;
	public int framecount;
	public FpsData(int fps, int dropcount, int framecount) {
		super();
		this.fps = fps;
		this.dropcount = dropcount;
		this.framecount = framecount;
	} 
	
	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return "fps = " + this.fps + ", dropcount = " + this.dropcount + ", framecount = " + framecount;
		
	}
}
