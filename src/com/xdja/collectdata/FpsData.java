package com.xdja.collectdata;

/**
 *  用于封装流量数据
 * @author zlw
 *
 */
public class FpsData {
	public int fps;
	public int fropcount;
	public int framecount;
	public FpsData(int fps, int fropcount, int framecount) {
		super();
		this.fps = fps;
		this.fropcount = fropcount;
		this.framecount = framecount;
	} 
	
	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return "fps = " + this.fps + ", fropcount = " + this.fropcount + ", framecount = " + framecount;
		
	}
}
