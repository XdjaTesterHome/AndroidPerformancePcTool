package com.xdja.monitor;

import com.android.ddmlib.IDevice;
import com.xdja.controllers.BatteryController;
import com.xdja.controllers.CPUController;
import com.xdja.controllers.FlowController;
import com.xdja.controllers.FpsController;
import com.xdja.controllers.LaunchController;
import com.xdja.controllers.MemoryController;
import com.xdja.controllers.PackageController;

public class ControllerMonitor {

	private static ControllerMonitor monitor;
	private BatteryController battery;
	private CPUController cpu;
	private FlowController flow;
	private LaunchController launch;
	private MemoryController memory;
	private PackageController packageManager;
	private FpsController fpsController;

	/**
	 * single instance mode
	 * 
	 * @return ControllerMonitor instance
	 **/
	public static ControllerMonitor getInstance() {
		if (null == monitor) {
			monitor = new ControllerMonitor();
		}
		return monitor;
	}

	/**
	 * get the Battery instance controller
	 * 
	 * @return BatteryController instance
	 **/
	public BatteryController getBatteryController() {
		if (null == battery) {
			battery = new BatteryController();
		}
		return battery;
	}

	/**
	 * get the CPU instance controller
	 * 
	 * @return CPUController instance
	 **/
	public CPUController getCPUController() {
		if (null == cpu) {
			cpu = new CPUController();
		}
		return cpu;
	}

	/**
	 * get the Flow instance controller
	 * 
	 * @return FlowController instance
	 **/
	public FlowController getFlowController() {
		if (null == flow) {
			flow = new FlowController();
		}
		return flow;
	}

	/**
	 * get the Launch Cost instance controller
	 * 
	 * @return LaunchController
	 **/
	public LaunchController getLaunchController() {
		if (null == launch) {
			launch = new LaunchController();
		}
		return launch;
	}

	/**
	 * get the Memory instance controller
	 * 
	 * @return MemoryController instance
	 **/
	public MemoryController getMemoryController() {
		if (null == memory) {
			memory = new MemoryController();
		}
		return memory;
	}

	/**
	 * get the Package instance controller
	 * 
	 * @return PackageController instance
	 */
	public PackageController getPackageController() {
		if (null == packageManager) {
			packageManager = new PackageController();
		}
		return packageManager;
	}

	/**
	 * get the Fps instance controller
	 * 
	 * @return FpsController instance
	 */
	public FpsController getFpsController() {
		if (null == fpsController) {
			fpsController = new FpsController();
		}
		return fpsController;
	}

	public void setDevice(IDevice dev) {
		getBatteryController().setDevice(dev);
		getCPUController().setDevice(dev);
		getFlowController().setDevice(dev);
		getMemoryController().setDevice(dev);
		getLaunchController().setDevice(dev);
		getPackageController().setDevice(dev);
		getFpsController().setDevice(dev);
	}

}
