package com.xdja.collectdata;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Timer;
import java.util.TimerTask;

public class LogRunnable implements Runnable {
	private int curPid;
	private String cmds;
	private File mCurLogFile;
	private Process mProcess;
	/**
	 * 
	 * @param pid
	 * @param logPath
	 * @param testType
	 *            测试的类型
	 */
	public LogRunnable(int pid, String logPath, String testType) {
		// TODO Auto-generated constructor stub
		curPid = pid;
		String fileName = SaveEnvironmentManager.getInstance().getSuggestedName(testType);
		mCurLogFile = new File(logPath, fileName + ".log");
		if (!mCurLogFile.exists()) {
			try {
				mCurLogFile.createNewFile();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		cmds = "adb logcat *:v -v time";
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		FileOutputStream fos = null;
		BufferedReader mReader = null;
		try {
			mProcess = Runtime.getRuntime().exec(cmds);
			mReader = new BufferedReader(new InputStreamReader(mProcess.getInputStream()), 1024);
			String line;
			while ((line = mReader.readLine()) != null) {
				System.out.println("line = " + line);
				if (line.length() == 0) {
					continue;
				}
				fos = new FileOutputStream(mCurLogFile, true);
				if (curPid != 0) {
					if (fos != null && line.contains(String.valueOf(curPid))) {
						fos.write((line + "\n").getBytes());
					}
				}else {
					if (fos != null) {
						fos.write((line + "\n").getBytes());
					}
				}
			}
			System.out.println("I am over");
		} catch (Exception e) {
			// TODO: handle exception
		}finally {
			if(mProcess != null){  
                mProcess.destroy();  
                mProcess = null;  
            }  
            try {  
                if(mReader != null){  
                    mReader.close();  
                    mReader = null;  
                }  
                if(fos != null){  
                    fos.close();  
                    fos = null;  
                }  
            } catch (Exception e2) {  
                e2.printStackTrace();  
            }  
		}
		
		System.out.println("I am over111");
	}
	
	/**
	 *  销毁进程
	 */
	public void destoryProcess(){
		if (mProcess != null) {
			mProcess.destroy();
		}
	}
}
