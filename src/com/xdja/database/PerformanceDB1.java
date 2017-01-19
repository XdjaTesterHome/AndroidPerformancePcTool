package com.xdja.database;

import java.sql.*;
import java.util.ArrayList;

import com.xdja.constant.GlobalConfig;  

public class PerformanceDB1 {
	public static Connection conn;
	public static Statement stat ;
	public static ResultSet result ;
	static String url ;
	String username ;
	String password;
	
	private PerformanceDB1(String url,String username,String password){
		PerformanceDB1.url=url;
		this.username=username;
		this.password=password;
	}
	
	
	
	
	public PerformanceDB1() {
		// TODO Auto-generated constructor stub
	}




	public  void conperformance(String test) throws ClassNotFoundException, SQLException{
		Class.forName("com.mysql.jdbc.Driver");  // 注册 JDBC 驱动
		//一开始必须填一个已经存在的数据库  
        String url = "jdbc:mysql://localhost:3306/"+test+"?useUnicode=true&characterEncoding=utf-8";     
        conn = DriverManager.getConnection(url, "root", "");  //密码为空
        stat = conn.createStatement();  
	
	}
	
	//创建数据库performance//
	public void creatperformance(Statement stat,Connection conn) throws SQLException{
		//创建数据库performance 
		try {
			String url = "jdbc:mysql://11.12.109.38:3306/performance?useUnicode=true&characterEncoding=utf-8";
		    Connection conn1= DriverManager.getConnection(url, GlobalConfig.DBUSERNAME, GlobalConfig.DBUSERPWD);
		    conn1.close();
		   } catch (SQLException e) {
		    e.printStackTrace();
		    stat.executeUpdate("create database performance");
		   } 
		
        stat.close();  
        conn.close(); 
    }
	
	//创建CPU数据表//
	public void CPUteble(Statement stat,String table1,String pkg ,String version,String projectname){
		 String checkTable="show tables like \'"+table1+"'";  
		try {
			ResultSet resultSet=stat.executeQuery(checkTable);
			if (resultSet.next()) {  
	            System.out.println("table exist!");  
	        }else{ 
	        String pkg1 = pkg.replace(".", "_");
	        String version1 = version.replace(".", "_");
	        String firstid = projectname+pkg1+version1;
			stat.executeUpdate("create table "+table1+"("+firstid+" double, Activity varchar(80),abnormal varchar(20),screenshot varchar(50),logcat varchar(50),traceview varchar(50),hprof varchar(50))");
		} resultSet.close();
			}catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 	
	}

	//创建插入CPU数据表的数据//
	private void insertCPUteble(Statement stat,String table1,double d,String Activity ,String result,String  screenshotpath, String logpath,String traceviewpath, String hprofpath){
		try {
		stat.executeUpdate("insert into "+table1+" values('"+d +"', '"+Activity+"','"+result+"','"+screenshotpath+"', '"+logpath+"','"+traceviewpath+"','"+hprofpath+"')");

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	
	//向数据表中插入数据，传入参数为列表类型，批量插入数据//
		public void insertDatas(Statement stat,ArrayList<ArrayList<String>> d,String table1) throws SQLException{
			String insert_sql = "INSERT INTO tb_ipinfos (CPU, Activity, abnormal,screenshot,logcat,traceview,hprof) VALUES (?,?,?,?,?,?,?)";
			PreparedStatement psts = conn.prepareStatement(insert_sql);
			if (d!=null){
				int i = d.size();
				for (int j =0; j<i ;j++){
					try {
						psts.setString(1, d.get(j).get(0));
						psts.setString(2, d.get(j).get(1));
						psts.setString(3, d.get(j).get(2));
						psts.setString(4, d.get(j).get(3));
						psts.setString(5, d.get(j).get(4));
						psts.setString(6, d.get(j).get(5));
						psts.setString(7, d.get(j).get(6));
						psts.addBatch(); 
			
					} catch (SQLException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				psts.executeBatch(); // 执行批量处理  
		        conn.commit();  // 提交  
			}
		}
		
		
		
	
	//创建关闭数据库的方法//
	public void closeperformance(Connection conn,Statement stat,ResultSet result){
		try {
			if(result !=null){
			  result.close();	
			}
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}  //关闭游标
        try {
        	if (stat !=null){
        	stat.close();	
        	}
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}    //关闭数据库操作对象
        try {
        	if (conn !=null){
        		conn.close();	
            }
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}   //关闭数据库操作连接对象	
	}
	
	static void CPUtestfordatabase () throws ClassNotFoundException, SQLException{
		PerformanceDB1 perfor = new PerformanceDB1();//创建数据库类perfor
		perfor.conperformance("test"); //连接数据库test
		perfor.creatperformance(PerformanceDB1.stat,PerformanceDB1.conn);//创建数据库performance
		perfor.conperformance("performance"); //连接数据库performance
		perfor.CPUteble(PerformanceDB1.stat,"cputable","com.xdja.actoma","V3.3056.1","CPU");//创建数据表cputable
		perfor.insertCPUteble(PerformanceDB1.stat,"cputable", 50.0,  "com.xdja.actoma", "true" , "D:/log", "D:/log", "D:/log", "D:/log");//插入单个模拟数据
		perfor.closeperformance(PerformanceDB1.conn,PerformanceDB1.stat,PerformanceDB1.result);//关闭数据库连接
    }
	
	
	public static void main(String[] args) throws Exception  
    {   
		CPUtestfordatabase();
    }
	
	
}
