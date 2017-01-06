package com.xdja.collectdata.handleData;
import java.util.Arrays;
import com.xdja.collectdata.CpuData;
import com.xdja.collectdata.FlowData;
import com.xdja.collectdata.FpsData;
import com.xdja.collectdata.KpiData;
import com.xdja.collectdata.MemoryData;
import com.xdja.collectdata.handleData.HandleDataResult;

/**
 *  问题模型：用于对上报的数据进行处理，判断是否存在问题。
 * @author zlw
 *
 */
public class HandleDataManager {
	public static HandleDataManager mInstance = null;
	
    private HandleDataManager(){
		
	}
	
	public static HandleDataManager getInstance() {
		if (mInstance == null) {
			synchronized (HandleDataManager.class) {
				if (mInstance == null) {
					mInstance = new HandleDataManager();
				}
			}
		}
		return mInstance;
	}
	
	/**
	 * 
	 * @param cpuData
	 * @return
	 */
	//处理静默CPU数据，异常捕获模型，返回处理后认为有问题的数据//
	public HandleDataResult[] handleCpuData(float[] cpuData){
		//静默测试数据的判断//
		boolean result;
		String activityName;
		String screenshotsPath;
		String logPath;
		HandleDataResult[] uploadcpu = {}; //保存异常CPU数据的数组，以及场景变量，并作为返回值
		HandleDataResult upcpu ;
		float[] totalcpu = {};   //记录异常CPU数据
		float[] silenceCPU = cpuData ;  
		for(int i=0;i<silenceCPU.length;i++){
			if (silenceCPU[i]>0.01){
				//手机截屏，当前activity，当前CPU,traceview文件的值等信息并返回其值/
				totalcpu = insert(totalcpu,silenceCPU[i]);
				//添加对异常场景的捕获
				result =true;
				activityName = "";
				screenshotsPath= "";
				logPath= "";
				upcpu = new HandleDataResult(result,activityName,screenshotsPath,logPath);
				uploadcpu = inserthandle(uploadcpu, upcpu);
			}
		}
		if (uploadcpu.length==0){
			result = false;
			upcpu =new HandleDataResult(result);
			uploadcpu = inserthandle(uploadcpu, upcpu);
		}
		return uploadcpu;
	}
	
  //insert动态向一维HandleDataResult数组中插入元素//	
	private static HandleDataResult[] inserthandle(HandleDataResult[] arr, HandleDataResult str)
    {
		int size = arr.length;
		HandleDataResult[] tmp = new HandleDataResult[size + 1];
		System.arraycopy(arr, 0, tmp, 0, size);
		tmp[size] = str;
		arr = null;
		return tmp;
	}
	
	//insert动态向一维字符串数组中插入元素//	
		private static float[] insert(float[] arr, float str)
	    {
			int size = arr.length;
			float[] tmp = new float[size + 1];
			System.arraycopy(arr, 0, tmp, 0, size);
			tmp[size] = str;
			arr = null;
			return tmp;
		}
		
	
	/**
	 * 
	 * @param handleCpuAll;针对通用测试的CPU的判断(非静默测试CPU数据)
	 * @return
	 */
	public HandleDataResult[] handleCpuAll(float[] cpuData){
		boolean result;
		String activityName;
		String screenshotsPath;
		String logPath;
		float[] allcpu;
		allcpu = cpuData;
		HandleDataResult[] uploadcpu = {}; //保存异常CPU数据的数组，以及场景变量，并作为返回值
		HandleDataResult upcpu ;
		float[] cpuerror ={};//收集异常CPU数据
		for(int i=0;i<allcpu.length;i++){
			if (allcpu[i]>0.5){
				cpuerror = 	insert(cpuerror,allcpu[i]);
				result =true;
				activityName= "";
				screenshotsPath="";
				logPath="";
				upcpu = new HandleDataResult(result,activityName,screenshotsPath,logPath);
				uploadcpu = inserthandle(uploadcpu, upcpu);
			}else if (allcpu.length-i>4){
				if(allcpu[i]>0.2&&allcpu[i+1]>0.2&&allcpu[i+2]>0.2&&allcpu[i+3]>0.2&&allcpu[i+4]>0.2){
					cpuerror = 	insert(cpuerror,allcpu[i]);
					result =true;
					activityName= "";
					screenshotsPath="";
					logPath="";
					upcpu = new HandleDataResult(result,activityName,screenshotsPath,logPath);
					uploadcpu = inserthandle(uploadcpu, upcpu);
				}
			}else if (allcpu.length-i==4&&i>=1){
				int j =allcpu.length;
				if(allcpu[j-1]>0.2&&allcpu[j-2]>0.2&&allcpu[j-3]>0.2&&allcpu[j-4]>0.2&&allcpu[j-5]>0.2){
					cpuerror = 	insert(cpuerror,allcpu[i]);
					result =true;
					activityName= "";
					screenshotsPath="";
					logPath="";
					upcpu = new HandleDataResult(result,activityName,screenshotsPath,logPath);
					uploadcpu = inserthandle(uploadcpu, upcpu);
				}
			}else if (allcpu.length-i==3&&i>=2){
				int j =allcpu.length;
				if(allcpu[j-1]>0.2&&allcpu[j-2]>0.2&&allcpu[j-3]>0.2&&allcpu[j-4]>0.2&&allcpu[j-5]>0.2){
					cpuerror = 	insert(cpuerror,allcpu[i]);
					result =true;
					activityName= "";
					screenshotsPath="";
					logPath="";
					upcpu = new HandleDataResult(result,activityName,screenshotsPath,logPath);
					uploadcpu = inserthandle(uploadcpu, upcpu);
				}
			}else if (allcpu.length-i==2&&i>=3){
				int j =allcpu.length;
				if(allcpu[j-1]>0.2&&allcpu[j-2]>0.2&&allcpu[j-3]>0.2&&allcpu[j-4]>0.2&&allcpu[j-5]>0.2){
					cpuerror = 	insert(cpuerror,allcpu[i]);
					result =true;
					activityName= "";
					screenshotsPath="";
					logPath="";
					upcpu = new HandleDataResult(result,activityName,screenshotsPath,logPath);
					uploadcpu = inserthandle(uploadcpu, upcpu);
				}
			}else if (allcpu.length-i==1&&i>=4){
				int j =allcpu.length;
				if(allcpu[j-1]>0.2&&allcpu[j-2]>0.2&&allcpu[j-3]>0.2&&allcpu[j-4]>0.2&&allcpu[j-5]>0.2){
					cpuerror = 	insert(cpuerror,allcpu[i]);
					result =true;
					activityName= "";
					screenshotsPath="";
					logPath="";
					upcpu = new HandleDataResult(result,activityName,screenshotsPath,logPath);
					uploadcpu = inserthandle(uploadcpu, upcpu);
			    }
		    }  
		}
		if (uploadcpu.length==0){
			result = false;
			upcpu =new HandleDataResult(result);
			uploadcpu = inserthandle(uploadcpu, upcpu);
		}
		return uploadcpu;
	}			

	
	
	public HandleDataResult handleFlowData(FlowData flowData){
		return null;
	}
	/**
	 * 
	 * @param cpuData
	 * @return
	 */
	public HandleDataResult handleFpsData(FpsData fpsData){
		return null;
	}
	/**
	 * 
	 * @param cpuData
	 * @return
	 */
	public HandleDataResult handleKpiData(KpiData kpiData){
		return null;
	}
	/**
	 * 
	 * @param cpuData
	 * @return
	 */
	public HandleDataResult handleMemoryData(MemoryData memoryData){
		return null;
	}

	
}
