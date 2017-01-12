package com.xdja.database;

import java.sql.*;  

public class performancedata {
	static Connection conn;
	static Statement stat ;
	static ResultSet result ;
	String url ;
	String username ;
	String password;
	
	private performancedata(String url,String username,String password){
		this.url=url;
		this.username=username;
		this.password=password;
	}
	
	private performancedata(){
		
	}
	
	
	//创建连接数据库//
	private  void conperformance(String test) throws ClassNotFoundException, SQLException{
		Class.forName("com.mysql.jdbc.Driver");  // 注册 JDBC 驱动
		//一开始必须填一个已经存在的数据库  
        String url = "jdbc:mysql://localhost:3306/"+test+"?useUnicode=true&characterEncoding=utf-8";     
        conn = DriverManager.getConnection(url, "root", "");  //密码为空
        stat = conn.createStatement();  
	
	}
	
	//创建数据库performance//
	private void creatperformance(Statement stat,Connection conn) throws SQLException{
		//创建数据库performance 
		try {
			String url = "jdbc:mysql://localhost:3306/performance?useUnicode=true&characterEncoding=utf-8";
		    Connection conn1= DriverManager.getConnection(url, "root", "");
		    conn1.close();
		   } catch (SQLException e) {
//		    e.printStackTrace();
		    stat.executeUpdate("create database performance");
		   } 
		
        stat.close();  
        conn.close(); 
    }
	
	//创建CPU数据表//
	private void CPUteble(Statement stat){
		 String checkTable="show tables like \"CPUteble\"";  
		try {
			ResultSet resultSet=stat.executeQuery(checkTable);
			if (resultSet.next()) {  
	            System.out.println("table exist!");  
	        }else{ 
			stat.executeUpdate("create table CPUteble(CPU double, Activity varchar(80), package varchar(80),version varchar(50),abnormal varchar(20),screenshot varchar(50),logcat varchar(50),traceview varchar(50),hprof varchar(50))");
		} resultSet.close();
			}catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 	
	}

	//创建插入CPU数据表的数据//
	private void insertCPUteble(Statement stat,double d,String Activity, String pkg,String version ,String result,String  screenshotpath, String logpath,String traceviewpath, String hprofpath){
		try {
		stat.executeUpdate("insert into CPUteble values('"+d +"', '"+Activity+"','"+pkg+"','"+version+"','"+result+"','"+screenshotpath+"', '"+logpath+"','"+traceviewpath+"','"+hprofpath+"')");

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	//创建关闭数据库的方法//
	private void closeperformance(Connection conn,Statement stat,ResultSet result){
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
		performancedata perfor = new performancedata();
		perfor.conperformance("test"); 
		perfor.creatperformance(performancedata.stat,performancedata.conn);
		perfor.conperformance("performance"); 
		perfor.CPUteble(performancedata.stat);
		perfor.insertCPUteble(performancedata.stat, 50.0, "xdja.actoma", "com.xdja.actoma", "2.3.3.3", "true" , "D:/log", "D:/log", "D:/log", "D:/log");
		perfor.closeperformance(performancedata.conn,performancedata.stat,performancedata.result);
    }
	
	
	public static void main(String[] args) throws Exception  
    {   
		CPUtestfordatabase ();
    }
	
	
}
