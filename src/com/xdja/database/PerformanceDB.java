package com.xdja.database;

import java.sql.*;
import java.util.List;

import com.xdja.collectdata.CollectDataImpl;
import com.xdja.collectdata.entity.BaseTestInfo;
import com.xdja.collectdata.handleData.HandleDataResult;
import com.xdja.constant.Constants;
import com.xdja.constant.GlobalConfig;
import com.xdja.log.LoggerManager;
import com.xdja.util.CommonUtil;

public class PerformanceDB {
	private static PerformanceDB mInstance = null;
	private static Connection conn;
	private static Statement stat;
	private static ResultSet result;
	private static String tableUrl = "jdbc:mysql://11.12.109.38:3306/performanceData";
	private static String dbUrl = "jdbc:mysql://11.12.109.38:3306/";
	private static String driverClass = "com.mysql.jdbc.Driver";
	private final static String DBNAME = "performanceData";
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
	
	private String cpuTableName, memoryTableName, kpiTableName, fpsTableName, batteryTableName, flowTableName;

	private PerformanceDB() {
		try {
			Class.forName(driverClass);
			// 创建数据库
			createDb();
			// 创建表的连接
			conn = DriverManager.getConnection(tableUrl, GlobalConfig.DBUSERNAME, GlobalConfig.DBUSERPWD);
			stat = conn.createStatement();

			// 获取packageName和version
			BaseTestInfo baseTestInfo = CollectDataImpl.getBaseTestInfo();
			if (baseTestInfo == null) {
				return;
			}
			String packageName = baseTestInfo.packageName;
			String version = baseTestInfo.versionName;
			// 拼接表的名称
			cpuTableName = getFormatDbName(packageName, version, Constants.TYPE_CPU);
			memoryTableName = getFormatDbName(packageName, version, Constants.TYPE_MEMORY);
			kpiTableName = getFormatDbName(packageName, version, Constants.TYPE_KPI);
			fpsTableName = getFormatDbName(packageName, version, Constants.TYPE_FPS);
			batteryTableName = getFormatDbName(packageName, version, Constants.TYPE_BATTERY);
			flowTableName = getFormatDbName(packageName, version, Constants.TYPE_FLOW);
			
			//创建数据表
			createTables();
		} catch (ClassNotFoundException | SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * 关闭数据库的一些资源
	 * 在所有操作完成之后调用
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
			connection = DriverManager.getConnection(dbUrl, GlobalConfig.DBUSERNAME, GlobalConfig.DBUSERPWD);
			stat = connection.createStatement();
			String sql = "create database if not exists" + DBNAME;
			stat.executeUpdate(sql);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			LoggerManager.logError(PerformanceDB.class.getSimpleName(), "createDb", "数据库已经存在：" + DBNAME);
			return;
		}finally {
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
	 * 创建数据表 适用于公共字段
	 * @throws SQLException 
	 */
	private void createTable(String tableName) throws SQLException {
		String sql = "CREATE TABLE IF NOT EXISTS `" + tableName + "`("
					 + "id int(11) not null AUTO_INCREMENT, page varchar(80),testvalue varchar(50), screenshotPath varchar(50),logPath varchar(50),methodTracePath varchar(50),hprofPath varchar(50), pass int, PRIMARY KEY(`id`))";
		stat.executeUpdate(sql);
	}

	/**
	 * 创建数据表，适用需要自定义或者扩展字段的场景
	 * 
	 * @param tableName
	 * @param sql
	 * @throws SQLException 
	 */
	@SuppressWarnings("unused")
	private void createTable(String tableName, String sql) throws SQLException {
		if (CommonUtil.strIsNull(sql) || CommonUtil.strIsNull(tableName)) {
			return;
		}
		stat.executeUpdate(sql);
	}

	/**
	 * 创建所有的数据表
	 * @throws SQLException 
	 */
	public void createTables() throws SQLException {

		// 创建CPU数据表
		createTable(cpuTableName);
		// 创建Memory数据表
		createTable(memoryTableName);
	}

	/**
	 * 得到格式化后的数据表名字， 格式是： testType#packageName#version
	 * 
	 * @param packageName
	 * @param version
	 * @param testType
	 * @return
	 */
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
	public void insertCpuData(List<HandleDataResult> handleDataList) {
		if (handleDataList == null || handleDataList.size() < 1) {
			return;
		}
		String insertSql = "insert into `" + cpuTableName
				+ "`(page, testvalue, screenshotPath, logPath, methodTracePath, hprofPath, pass) values (?,?,?,?,?,?,?)";
		PreparedStatement psts = null;
		try {
			conn.setAutoCommit(false);
			psts = conn.prepareStatement(insertSql);
			for (HandleDataResult result : handleDataList) {
				psts.setString(1, result.activityName);
				psts.setFloat(2, Float.valueOf(result.testValue));
				psts.setString(3, result.screenshotsPath);
				psts.setString(4, result.logPath);
				psts.setString(5, result.methodTracePath);
				psts.setString(6, result.memoryTracePath);
				// 正常数据，值是1，异常数据，值是0
				psts.setInt(7, result.result ? 1 : 0);
				psts.addBatch();
			}
			
			psts.executeBatch(); // 执行批量处理  
	        conn.commit();  // 提交 
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
