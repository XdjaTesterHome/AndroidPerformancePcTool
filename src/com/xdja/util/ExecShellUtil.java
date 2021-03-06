package com.xdja.util;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import com.android.ddmlib.IDevice;
import com.android.ddmlib.MultiLineReceiver;
import com.xdja.adb.AdbManager;
import com.xdja.collectdata.entity.CommandResult;
import com.xdja.constant.GlobalConfig;

/**
 * 和Adb命令进行交互的工具类
 * 
 * @author zlw
 *
 */
public class ExecShellUtil {

	private static final String COMMAND_SU = "su";
	private static final String COMMAND_SH = "sh";
	private static final String COMMAND_EXIT = "exit\n";
	private static final String COMMAND_LINE_END = "\n";
	private IDevice device;
 //	private static DefaultHardwareDevice mDefaultHardwareDevice;
	
	//针对连续读取内容的Process，比如执行adb logcat等
	private Process mReadInfoProcess;
	private static ExecShellUtil mInstance = null;
	
	private ExecShellUtil() {
	}
	
	
	public static ExecShellUtil getInstance(){
		if (mInstance == null) {
			synchronized (ExecShellUtil.class) {
				if (mInstance == null) {
					mInstance = new ExecShellUtil();
				}
			}
		}
		
		return mInstance;
	}
	/**
	 * check whether has root permission
	 *
	 * @return
	 */
	public boolean checkRootPermission() {
		return execCommand("echo root", true, false).result == 0;
	}

	/**
	 * execute shell command, default return result msg
	 *
	 * @param command
	 *            command
	 * @param isRoot
	 *            whether need to run with root
	 * @return
	 * @see ShellUtils#execCommand(Object[], boolean, boolean)
	 */
	public CommandResult execCommand(String command, boolean isRoot) {
		return execCommand(new String[] { command }, isRoot, true);
	}

	/**
	 * execute shell commands, default return result msg
	 *
	 * @param commands
	 *            command list
	 * @param isRoot
	 *            whether need to run with root
	 * @return
	 * @see ShellUtils#execCommand(Object[], boolean, boolean)
	 */
	public CommandResult execCommand(List<?> commands, boolean isRoot) {
		return execCommand(commands == null ? null : commands.toArray(new String[] {}), isRoot, true);
	}

	/**
	 * execute shell commands, default return result msg
	 *
	 * @param commands
	 *            command array
	 * @param isRoot
	 *            whether need to run with root
	 * @return
	 * @see ShellUtils#execCommand(Object[], boolean, boolean)
	 */
	public CommandResult execCommand(String[] commands, boolean isRoot) {
		return execCommand(commands, isRoot, true);
	}

	/**
	 * execute shell command
	 *
	 * @param command
	 *            command
	 * @param isRoot
	 *            whether need to run with root
	 * @param isNeedResultMsg
	 *            whether need result msg
	 * @return
	 * @see ShellUtils#execCommand(Object[], boolean, boolean)
	 */
	public CommandResult execCommand(String command, boolean isRoot, boolean isNeedResultMsg) {
		return execCommand(new String[] { command }, isRoot, isNeedResultMsg);
	}

	/**
	 * execute shell commands
	 *
	 * @param commands
	 *            command list
	 * @param isRoot
	 *            whether need to run with root
	 * @param isNeedResultMsg
	 *            whether need result msg
	 * @return
	 * @see ShellUtils#execCommand(Object[], boolean, boolean)
	 */
	public CommandResult execCommand(List<?> commands, boolean isRoot, boolean isNeedResultMsg) {
		return execCommand(commands == null ? null : commands.toArray(new String[] {}), isRoot, isNeedResultMsg);
	}

	/**
	 * execute shell commands
	 *
	 * @param commands
	 *            command array
	 * @param isRoot
	 *            whether need to run with root
	 * @param isNeedResultMsg
	 *            whether need result msg
	 * @return
	 *
	 * 		if isNeedResultMsg is false, {@link CommandResult#successMsg} is
	 *         null and {@link CommandResult#errorMsg} is null.
	 *
	 *         if {@link CommandResult#result} is -1, there maybe some
	 *         excepiton.
	 *
	 */
	public  CommandResult execCommand(Object[] commands, boolean isRoot, boolean isNeedResultMsg) {
		int result = -1;
		if (commands == null || commands.length == 0) {
			return new CommandResult(result, null, null);
		}

		Process process = null;
		BufferedReader successResult = null;
		BufferedReader errorResult = null;
		StringBuilder successMsg = null;
		StringBuilder errorMsg = null;

		DataOutputStream os = null;
		try {
			process = Runtime.getRuntime().exec(isRoot ? COMMAND_SU : COMMAND_SH);
			os = new DataOutputStream(process.getOutputStream());
			for (Object command : commands) {
				String strCommand = String.valueOf(command);

				if (strCommand == null) {
					continue;
				}
				// donnot use os.writeBytes(strCommand), avoid chinese charset
				// error
				os.write(strCommand.getBytes());
				os.writeBytes(COMMAND_LINE_END);
				os.flush();
			}
			os.writeBytes(COMMAND_EXIT);
			os.flush();

			result = process.waitFor();
			// get command result
			if (isNeedResultMsg) {
				successMsg = new StringBuilder();
				errorMsg = new StringBuilder();
				successResult = new BufferedReader(new InputStreamReader(process.getInputStream()));
				// errorResult = new BufferedReader(new
				// InputStreamReader(process.getErrorStream()));
				String s;
				while ((s = successResult.readLine()) != null) {
					successMsg.append(s).append("\n");
				}
				// while ((s = errorResult.readLine()) != null) {
				// errorMsg.append(s).append("\n");
				// }
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (os != null) {
					os.close();
				}
				if (successResult != null) {
					successResult.close();
				}
				if (errorResult != null) {
					errorResult.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}

			if (process != null) {
				process.destroy();
			}
		}
		return new CommandResult(result, successMsg == null ? null : successMsg.toString(),
				errorMsg == null ? null : errorMsg.toString());
	}

	/**
	 * execute shell commands
	 *
	 * @param commands
	 *            command array
	 * @param isRoot
	 *            whether need to run with root
	 * @param isNeedResultMsg
	 *            whether need result msg
	 * @return
	 *
	 * 		if isNeedResultMsg is false, {@link CommandResult#successMsg} is
	 *         null and {@link CommandResult#errorMsg} is null.
	 *
	 *         if {@link CommandResult#result} is -1, there maybe some
	 *         excepiton.
	 *
	 */
	public CommandResult execCmdCommand(String cmd, boolean isRoot, boolean isNeedResultMsg) {
		int result = -1;
		if (cmd == null || "" == cmd) {
			return new CommandResult(result, null, null);
		}

		Process process = null;
		BufferedReader successResult = null;
		BufferedReader errorResult = null;
		StringBuilder successMsg = null;
		StringBuilder errorMsg = null;

		DataOutputStream os = null;
		try {
			process = Runtime.getRuntime().exec("cmd /c " + cmd);
			// get command result
			if (isNeedResultMsg) {
				successMsg = new StringBuilder();
				errorMsg = new StringBuilder();
				successResult = new BufferedReader(new InputStreamReader(process.getInputStream()));
				errorResult = new BufferedReader(new InputStreamReader(process.getErrorStream()));
				String s;
				while ((s = successResult.readLine()) != null) {
					successMsg.append(s).append("\n");
				}
//				System.out.println(successMsg);//
//				 读取错误流可能会阻塞流程waitFor（）
				 while ((s = errorResult.readLine()) != null) {
				 errorMsg.append(s).append("\n");
				 }
			}
			WatchThread wt = new WatchThread(process);  
			wt.start();  
			result = process.waitFor();
//			ArrayList<String> commandStream = wt.getStream();
			wt.setOver(true); 
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (os != null) {
					os.close();
				}
				if (successResult != null) {
					successResult.close();
				}
				if (errorResult != null) {
					errorResult.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}

			if (process != null) {
				process.destroy();
			}
		}

		return new CommandResult(result, successMsg == null ? null : successMsg.toString(),
				errorMsg == null ? null : errorMsg.toString());
	}

	/**
	 * 能够实时的获取cmd的输出信息
	 * 
	 * @param cmd
	 * @param getDataListener
	 *            获取数据的getDataListener
	 */
	public void execCmdCommand(String cmd, GetDataInterface getDataListener) {

		BufferedReader br = null;
		try {

			mReadInfoProcess = Runtime.getRuntime().exec(cmd);

			InputStream in = mReadInfoProcess.getInputStream();

			br = new BufferedReader(new InputStreamReader(in, "GBK"));
			String line = null;

			while ((line = br.readLine()) != null) {
				if (getDataListener != null) {
					getDataListener.getString(line);
				}
			}
			
			System.out.println("I am over");

		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	/**
	 * 将device传进来
	 * @param dev
	 */
	public void setDevice(IDevice dev){
		device = dev;
	}
	
	/**
	 * 能执行shell command，通过ddmlib
	 * @param cmd
	 */
	public CommandResult execShellCommand(String cmd){
		return execShellCommand(cmd, true);
	}
	
	
	/**
	 * 能执行shell command，通过ddmlib
	 * @param cmd
	 * @param isResult 是否需要结果
	 */
	public CommandResult execShellCommand(String cmd, boolean isResult){
		
		if (device == null) {
			device = AdbManager.getInstance().getIDevice(GlobalConfig.DeviceName);
		}
		if (CommonUtil.strIsNull(cmd)) {
			return new CommandResult(-1, "", "");
		}
		StringBuilder successMsg = new StringBuilder();
		
		// 处理cmd
//		cmd = cmd.substring(cmd.indexOf(" "));
		if (isResult) {
			List<String> results = executeShellCommandWithOutput(device, cmd);
			if (results.size() > 1) {
				for(String str:results){
					successMsg.append(str).append("\n");
				}
				
				return new CommandResult(0, successMsg.toString(), "");
			}
		}else {
			executeShellCommandWithOutput(device, cmd);
		}
		
		
		return new CommandResult(0, "", "");
	}
	
	
	/**
	 *  通过ddmblib执行cmd命令
	 * @param device
	 * @param cmd
	 * @return
	 */
	public List<String> executeShellCommandWithOutput(IDevice device, String cmd) {
		final List<String> results = new ArrayList<String>();
		try {
			device.executeShellCommand(cmd, new MultiLineReceiver() {

				@Override
				public void processNewLines(String[] lines) {
					for (String line : lines) {
						results.add(line);
					}
				}

				@Override
				public boolean isCancelled() {
					return false;
				}
			});
		} catch (Exception e) {
			System.out.println("executeShellCommandWithOutput error = " + e.getMessage());
		}
		return results;
	}
	
	/**
	 *  销毁process
	 */
	public void stopProcess(){
		if (mReadInfoProcess != null) {
			mReadInfoProcess.destroy();
			mReadInfoProcess = null;
		}
	}
	
	
	class WatchThread extends Thread {
		Process p;
		boolean over;
		ArrayList<String> stream;

		public WatchThread(Process p) {
			this.p = p;
			over = false;
			stream = new ArrayList<String>();
		}

		public void run() {
			try {
				if (p == null)
					return;
				Scanner br = new Scanner(p.getInputStream());
				while (true) {
					if (p == null || over)
						break;
					while (br.hasNextLine()) {
						String tempStream = br.nextLine();
						if (tempStream.trim() == null || tempStream.trim().equals(""))
							continue;
						stream.add(tempStream);
					}
				}
				br.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		public void setOver(boolean over) {
			this.over = over;
		}

		public ArrayList<String> getStream() {
			return stream;
		}
	}
	
	
	/**
	 * 获取数据的监听
	 * 
	 * @author zlw
	 *
	 */
	public interface GetDataInterface {
		public void getString(String content);

		public void getErrorString(String error);
	}
}
