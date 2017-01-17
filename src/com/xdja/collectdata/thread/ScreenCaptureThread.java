package com.xdja.collectdata.thread;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import com.android.ddmlib.AdbCommandRejectedException;
import com.android.ddmlib.IDevice;
import com.android.ddmlib.RawImage;
import com.android.ddmlib.TimeoutException;
import com.xdja.adb.AdbManager;

public class ScreenCaptureThread extends Thread{
	private BufferedImage mBufferedImage;
	private String mDeviceName;
	private boolean mNeed;
	private String mFilePath;
	
	public ScreenCaptureThread(String deviceName, boolean needSave, String filePath) {
		// TODO Auto-generated constructor stub
		mDeviceName = deviceName;
		mNeed = needSave;
		mFilePath = filePath;
	}
	
	@Override
	public void run() {
		// TODO Auto-generated method stub
		IDevice device = AdbManager.getInstance().getIDevice(mDeviceName);
		BufferedImage myImage = null;
		if (device != null) {
			try {
				RawImage rawImage = device.getScreenshot();
				if (rawImage != null) {
					myImage = new BufferedImage(rawImage.width, rawImage.height, BufferedImage.TYPE_INT_ARGB);
					for (int y = 0; y < rawImage.height; y++) {
						for (int x = 0; x < rawImage.width; x++) {
							int argb = rawImage.getARGB((x + y * rawImage.width) * (rawImage.bpp / 8));
							myImage.setRGB(x, y, argb);
						}
					}

					if (mNeed) {
						mBufferedImage = myImage;
						return;
					}

					ImageIO.write(myImage, "PNG", new File(mFilePath));
				}
			} catch (TimeoutException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (AdbCommandRejectedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	
	public BufferedImage getBufferedImage(){
		return mBufferedImage;
	}
}
