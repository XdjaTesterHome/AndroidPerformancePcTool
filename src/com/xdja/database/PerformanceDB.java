package com.xdja.database;

import java.sql.*;
import java.util.List;
import com.xdja.collectdata.handleData.entity.BatteryHandleResult;
import com.xdja.collectdata.handleData.entity.CpuHandleResult;
import com.xdja.collectdata.handleData.entity.FlowHandleResult;
import com.xdja.collectdata.handleData.entity.FpsHandleResult;
import com.xdja.collectdata.handleData.entity.KpiHandleResult;
import com.xdja.collectdata.handleData.entity.MemoryHandleResult;
import com.xdja.constant.Constants;
import com.xdja.log.LoggerManager;
import com.xdja.util.CommonUtil;
import com.xdja.util.ProPertiesUtil;

public class PerformanceDB {
	private static PerformanceDB mInstance = null;

	private Connection conn;
	private Statement stat;
	private ResultSet result;
	private String driverClass = "com.mysql.jdbc.Driver";

	private final static String DBNAME = "performancedata";
	private String mDBUser;
	private String mDBPwd;
	
	
	public static PerformanceDB getInstance() {
		if (mInstance == null) {
			synchronized (PerformanceDB.class) {
				if (mInstance == null) {
					mInstance = new PerformanceDB();
				}
			}
		}

		return mInstance;
	}

	private String cpuTableName, cpuSlientTableName, memoryTableName, kpiTableName, fpsTableName, batteryTableName,
			flowTableName, flowSlientTableName, commonTableName;

	private PerformanceDB() {
		try {
			Class.forName(driverClass);
			// 创建数据库
			createDb();
			// 创建表的连接
			String tableUrl = ProPertiesUtil.getInstance().getProperties(Constants.TABLEURL);
			mDBUser = ProPertiesUtil.getInstance().getProperties(Constants.DBUSERNAME);
			mDBPwd  = ProPertiesUtil.getInstance().getProperties(Constants.DBPASSWD);
			conn = DriverManager.getConnection(tableUrl, mDBUser, mDBPwd);
			stat = conn.createStatement();

			cpuTableName = Constants.CPU_TABLE;
			memoryTableName = Constants.MEMORY_TABLE;
			kpiTableName = Constants.KPI_TABLE;
			fpsTableName = Constants.FPS_TABLE;
			batteryTableName = Constants.BATTERY_TABLE;
			flowTableName = Constants.FLOW_TABLE;
			cpuSlientTableName = Constants.SLIENT_CPU_TABLE;
			flowSlientTableName = Constants.SLIENT_FLOW_TABLE;
			commonTableName = Constants.COMMON_TABLE;
			// 创建数据表
			createTables();
		} catch (ClassNotFoundException | SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * 关闭数据库的一些资源 在所有操作完成之后调用
	 */
	public void closeDB() {
		try {
			if (result != null) {
				result.close();
			}

			if (stat != null) {
				stat.close();
			}

			if (conn != null) {
				conn.close();
			}

			mInstance = null;
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * 创建数据库
	 */
	private void createDb() {
		Connection connection = null;
		Statement stat = null;
		try {
			String dbUrl = ProPertiesUtil.getInstance().getProperties(Constants.DBURL);
			connection = DriverManager.getConnection(dbUrl, mDBUser, mDBPwd);
			stat = connection.createStatement();
			String sql = "create database if not exists " + DBNAME;
			stat.executeUpdate(sql);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			LoggerManager.logError(PerformanceDB.class.getSimpleName(), "createDb", "数据库已经存在：" + DBNAME);
			return;
		} finally {
			if (stat != null) {
				try {
					stat.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

			if (connection != null) {
				try {
					connection.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * 创建数据表，适用需要自定义或者扩展字段的场景
	 * 
	 * @param tableName
	 * @param sql
	 * @throws SQLException
	 */
	private void createTable(String sql) throws SQLException {
		if (CommonUtil.strIsNull(sql)) {
			return;
		}
		stat.executeUpdate(sql);
	}

	/**
	 * 创建所有的数据表
	 * 
	 * @throws SQLException
	 */
	public void createTables() throws SQLException {
		String createMemorySql = "CREATE TABLE IF NOT EXISTS `" + memoryTableName + "`("
				+ "id int(11) not null AUTO_INCREMENT, page varchar(80),package varchar(160),version varchar(160),testvalue varchar(50), logPath varchar(50),methodTracePath varchar(50),hprofPath varchar(50), isPass int, PRIMARY KEY(`id`))";
		String createCpuSql = "CREATE TABLE IF NOT EXISTS `" + cpuTableName + "`("
				+ "id int(11) not null AUTO_INCREMENT, page varchar(80),package varchar(160),version varchar(160),testvalue varchar(50), logPath varchar(50),methodTracePath varchar(50), isPass int, PRIMARY KEY(`id`))";
		String createKpiSql = "CREATE TABLE IF NOT EXISTS `" + kpiTableName + "`("
				+ "id int(11) not null AUTO_INCREMENT, page varchar(80),package varchar(160),version varchar(160),testvalue varchar(50), logPath varchar(50),methodTracePath varchar(50), isPass int, PRIMARY KEY(`id`))";
		String createFlowSql = "CREATE TABLE IF NOT EXISTS `" + flowTableName + "`("
				+ "id int(11) not null AUTO_INCREMENT, page varchar(80),package varchar(160),version varchar(160),testvalue varchar(50), logPath varchar(50), isPass int, PRIMARY KEY(`id`))";
		String createFpsSql = "CREATE TABLE IF NOT EXISTS `" + fpsTableName + "`("
				+ "id int(11) not null AUTO_INCREMENT, page varchar(80),package varchar(160),version varchar(160),testvalue varchar(50), logPath varchar(50),methodTracePath varchar(50),hprofPath varchar(50), isPass int, PRIMARY KEY(`id`))";
		String createBatterySql = "CREATE TABLE IF NOT EXISTS `" + batteryTableName + "`("
				+ "id int(11) not null AUTO_INCREMENT, uid varchar(50),testvalue varchar(50), appPackageName varchar(50), detailInfo varchar(1024), package varchar(160), version varchar(160), PRIMARY KEY(`id`))";
		String createSlientCpuSql = "CREATE TABLE IF NOT EXISTS `" + cpuSlientTableName + "`("
				+ "id int(11) not null AUTO_INCREMENT, page varchar(80),package varchar(160),version varchar(160),testvalue varchar(50), logPath varchar(50),methodTracePath varchar(50), isPass int, PRIMARY KEY(`id`))";
		String createSlientFlowSql = "CREATE TABLE IF NOT EXISTS `" + flowSlientTableName + "`("
				+ "id int(11) not null AUTO_INCREMENT, page varchar(80),package varchar(160),version varchar(160),testvalue varchar(50), logPath varchar(50), isPass int, PRIMARY KEY(`id`))";
		String createCommonDataSql = "CREATE TABLE IF NOT EXISTS `" + commonTableName + "`("
				+ "id int(11) not null AUTO_INCREMENT, package varchar(160), version varchar(160), PRIMARY KEY(`id`))";

		// 创建公共数据的表
		createTable(createCommonDataSql);
		// 创建CPU数据表
		createTable(createMemorySql);
		// 创建Memory数据表
		createTable(createCpuSql);
		// 创建KPI数据表
		createTable(createKpiSql);
		// 创建Flow数据表
		createTable(createFlowSql);
		// 创建FPS数据表
		createTable(createFpsSql);
		// 创建电量数据表
		createTable(createBatterySql);
		// 创建静默cpu数据表
		createTable(createSlientCpuSql);
		// 创建静默流量数据表
		createTable(createSlientFlowSql);
	}

	/**
	 * 得到格式化后的数据表名字， 格式是： testType#packageName#version
	 * 
	 * @param packageName
	 * @param version
	 * @param testType
	 * @return
	 */
	@SuppressWarnings("unused")
	private String getFormatDbName(String packageName, String version, String testType) {
		String pkg1 = packageName.replace(".", "_");
		String version1 = version.replace(".", "_");
		StringBuilder sbBuilder = new StringBuilder(testType);
		sbBuilder.append("#");
		sbBuilder.append(pkg1).append("#");
		sbBuilder.append(version1);

		return sbBuilder.toString();
	}

	/**
	 * 将Cpu数据插入到数据表中
	 * 
	 * @param handleDataList
	 */
	public void insertCpuData(List<CpuHandleResult> handleDataList) {
		if (handleDataList == null || handleDataList.size() < 1) {
			return;
		}

		if (conn == null) {
			return;
		}
		String insertSql = "insert into `" + cpuTableName
				+ "`(page,package,version ,testvalue, logPath, methodTracePath, isPass) values (?,?,?,?,?,?,?)";
		PreparedStatement psts = null;
		try {
			conn.setAutoCommit(false);
			psts = conn.prepareStatement(insertSql);
			for (CpuHandleResult result : handleDataList) {
				psts.setString(1, result.activityName);
				psts.setString(2, result.packageName); // 增加package
				psts.setString(3, result.version); // 增加version
				psts.setFloat(4, Float.valueOf(result.testValue));
				psts.setString(5, result.logPath);
				psts.setString(6, result.methodTracePath);
				// 正常数据，值是1，异常数据，值是0
				psts.setInt(7, result.result ? 1 : 0);
				psts.addBatch();
			}

			psts.executeBatch(); // 执行批量处理
			conn.commit(); // 提交
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * 将Cpu数据插入到数据表中
	 * 
	 * @param handleDataList
	 */
	public void insertSlientCpuData(List<CpuHandleResult> handleDataList) {
		if (handleDataList == null || handleDataList.size() < 1) {
			return;
		}

		if (conn == null) {
			return;
		}

		String insertSql = "insert into `" + cpuSlientTableName
				+ "`(page,package,version, testvalue, logPath, methodTracePath, isPass) values (?,?,?,?,?,?,?)";
		PreparedStatement psts = null;
		try {
			conn.setAutoCommit(false);
			psts = conn.prepareStatement(insertSql);
			for (CpuHandleResult result : handleDataList) {
				psts.setString(1, result.activityName);
				psts.setString(2, result.packageName); // 增加package
				psts.setString(3, result.version); // 增加version
				psts.setFloat(4, Float.valueOf(result.testValue));
				psts.setString(5, result.logPath);
				psts.setString(6, result.methodTracePath);
				// 正常数据，值是1，异常数据，值是0
				psts.setInt(7, result.result ? 1 : 0);
				psts.addBatch();
			}

			psts.executeBatch(); // 执行批量处理
			conn.commit(); // 提交
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * 将kpi的数据结果存到数据库中
	 * 
	 * @param handleKpiList
	 */
	public void insertKpiData(List<KpiHandleResult> handleKpiList) {
		if (handleKpiList == null || handleKpiList.size() < 1) {
			return;
		}

		if (conn == null) {
			return;
		}

		String insertSql = "insert into `" + kpiTableName
				+ "`(page,package,version, testvalue, logPath, methodTracePath, isPass) values (?,?,?,?,?,?,?)";
		PreparedStatement psts = null;
		try {
			conn.setAutoCommit(false);
			psts = conn.prepareStatement(insertSql);
			for (KpiHandleResult result : handleKpiList) {
				psts.setString(1, result.activityName);
				psts.setString(2, result.packageName); // 增加package
				psts.setString(3, result.version); // 增加version
				psts.setFloat(4, Float.valueOf(result.testValue));
				psts.setString(5, result.logPath);
				psts.setString(6, result.methodTracePath);
				// 正常数据，值是1，异常数据，值是0
				psts.setInt(7, result.result ? 1 : 0);
				psts.addBatch();
			}

			psts.executeBatch(); // 执行批量处理
			conn.commit(); // 提交
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * 将Cpu数据插入到数据表中
	 * 
	 * @param handleDataList
	 */
	public void insertMemoryData(List<MemoryHandleResult> handleDataList) {
		if (handleDataList == null || handleDataList.size() < 1) {
			return;
		}
		if (conn == null) {
			return;
		}

		String insertSql = "insert into `" + memoryTableName
				+ "`(page,package,version, testvalue, logPath, methodTracePath, hprofPath, isPass) values (?,?,?,?,?,?,?,?)";
		PreparedStatement psts = null;
		try {
			conn.setAutoCommit(false);
			psts = conn.prepareStatement(insertSql);
			for (MemoryHandleResult result : handleDataList) {
				psts.setString(1, result.activityName);
				psts.setString(2, result.packageName); // 增加package
				psts.setString(3, result.version); // 增加version
				psts.setFloat(4, Float.valueOf(result.testValue));
				psts.setString(5, result.logPath);
				psts.setString(6, result.methodTracePath);
				psts.setString(7, result.memoryHprofPath);
				// 正常数据，值是1，异常数据，值是0
				psts.setInt(8, result.result ? 1 : 0);
				psts.addBatch();
			}

			psts.executeBatch(); // 执行批量处理
			conn.commit(); // 提交
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * 将Flow数据插入到数据库中
	 * 
	 * @param handleFlowList
	 */
	public void insertFlowData(List<FlowHandleResult> handleFlowList) {
		if (handleFlowList == null || handleFlowList.size() < 1) {
			return;
		}

		if (conn == null) {
			return;
		}

		String insertSql = "insert into `" + flowTableName
				+ "`(page,package,version, testvalue, logPath, isPass) values (?,?,?,?,?,?)";
		PreparedStatement psts = null;
		try {
			conn.setAutoCommit(false);
			psts = conn.prepareStatement(insertSql);
			for (FlowHandleResult result : handleFlowList) {
				psts.setString(1, result.activityName);
				psts.setString(2, result.packageName); // 增加package
				psts.setString(3, result.version); // 增加version
				psts.setFloat(4, Float.valueOf(result.testValue));
				psts.setString(5, result.logPath);
				// 正常数据，值是1，异常数据，值是0
				psts.setInt(6, result.result ? 1 : 0);
				psts.addBatch();
			}

			psts.executeBatch(); // 执行批量处理
			conn.commit(); // 提交
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * 将Flow数据插入到数据库中
	 * 
	 * @param handleFlowList
	 */
	public void insertSlientFlowData(List<FlowHandleResult> handleFlowList) {
		if (handleFlowList == null || handleFlowList.size() < 1) {
			return;
		}
		if (conn == null) {
			return;
		}

		String insertSql = "insert into `" + flowSlientTableName
				+ "`(page,package,version, testvalue, logPath, isPass) values (?,?,?,?,?,?)";
		PreparedStatement psts = null;
		try {
			conn.setAutoCommit(false);
			psts = conn.prepareStatement(insertSql);
			for (FlowHandleResult result : handleFlowList) {
				psts.setString(1, result.activityName);
				psts.setString(2, result.packageName); // 增加package
				psts.setString(3, result.version); // 增加version
				psts.setFloat(4, Float.valueOf(result.testValue));
				psts.setString(5, result.logPath);
				// 正常数据，值是1，异常数据，值是0
				psts.setInt(6, result.result ? 1 : 0);
				psts.addBatch();
			}

			psts.executeBatch(); // 执行批量处理
			conn.commit(); // 提交
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * 将Fps的数据插入到数据库中
	 * 
	 * @param handleFpsList
	 */
	public void insertFpsData(List<FpsHandleResult> handleFpsList) {
		if (handleFpsList == null || handleFpsList.size() < 1) {
			return;
		}
		if (conn == null) {
			return;
		}

		String insertSql = "insert into `" + fpsTableName
				+ "`(page,package,version, testvalue, logPath, methodTracePath, hprofPath, isPass) values (?,?,?,?,?,?,?,?)";
		PreparedStatement psts = null;
		try {
			conn.setAutoCommit(false);
			psts = conn.prepareStatement(insertSql);
			for (FpsHandleResult result : handleFpsList) {
				psts.setString(1, result.activityName);
				psts.setString(2, result.packageName); // 增加package
				psts.setString(3, result.version); // 增加version
				psts.setFloat(4, Float.valueOf(result.testValue));
				psts.setString(5, result.logPath);
				psts.setString(6, result.methodTracePath);
				psts.setString(7, result.memoryHprofPath);
				// 正常数据，值是1，异常数据，值是0
				psts.setInt(8, result.result ? 1 : 0);
				psts.addBatch();
			}

			psts.executeBatch(); // 执行批量处理
			conn.commit(); // 提交
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * 将电量数据插入到数据库中
	 * 
	 * @param handleBatteryList
	 */
	public void insertBatteryData(List<BatteryHandleResult> handleBatteryList) {
		if (handleBatteryList == null || handleBatteryList.size() < 1) {
			return;
		}
		if (conn == null) {
			return;
		}

		String insertSql = "insert into `" + batteryTableName + "`(uid, testvalue, appPackageName, detailInfo, package, version) values (?,?,?,?,?,?)";
		PreparedStatement psts = null;
		try {
			conn.setAutoCommit(false);
			psts = conn.prepareStatement(insertSql);
			for (BatteryHandleResult result : handleBatteryList) {
				psts.setString(1, result.uid);
				psts.setString(2, result.testValue);
				psts.setString(3, result.appPackageName);
				psts.setString(4, result.detailInfo);
				psts.setString(5, result.packageName);
				psts.setString(6, result.version);
				psts.addBatch();
			}

			psts.executeBatch(); // 执行批量处理
			conn.commit(); // 提交
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 *  将公共数据保存起来
	 * @param packageName
	 * @param version
	 */
	public void saveCommonData(String packageName, String version) {
		if (CommonUtil.strIsNull(packageName) || CommonUtil.strIsNull(version)) {
			return;
		}
		
		if (conn == null) {
			return;
		}
		PreparedStatement psts = null;
		String searchSql = "select * from `" + commonTableName + "` where package = '" + packageName + "' and version='" + version+"'";
		String insertSql = "insert into `" + commonTableName + "`(package, version) values (?,?)";
		try {
			psts = conn.prepareStatement(insertSql);
			
			ResultSet resultSet = psts.executeQuery(searchSql);
			if (resultSet != null && resultSet.next()) {
				return;
			}
			psts.setString(1, packageName);
			psts.setString(2, version);
			psts.executeUpdate();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			if (psts != null) {
				try {
					psts.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}
}
