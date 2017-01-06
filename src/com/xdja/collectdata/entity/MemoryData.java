package com.xdja.collectdata.entity;

/**
 *  封装了内存的数据
 * @author zlw
 *
 */
public class MemoryData {
	public long memAlloc;
	public long memFree;
	
	public MemoryData() {
		// TODO Auto-generated constructor stub
	}

	public MemoryData(long memAlloc, long memFree) {
		super();
		this.memAlloc = memAlloc;
		this.memFree = memFree;
	}
}
