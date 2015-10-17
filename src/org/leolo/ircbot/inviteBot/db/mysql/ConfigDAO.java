package org.leolo.ircbot.inviteBot.db.mysql;

import org.apache.commons.dbcp2.BasicDataSource;
import org.slf4j.LoggerFactory;

import java.sql.*;

public class ConfigDAO implements org.leolo.ircbot.inviteBot.db.ConfigDAO {
	
	private BasicDataSource datasource;
	final org.slf4j.Logger logger = LoggerFactory.getLogger(ConfigDAO.class);
	
	public ConfigDAO(BasicDataSource datasource) {
		this.datasource = datasource;
	}

	@Override
	public String get(String key) {
		String sql = "SELECT `value` FROM config WHERE `key`=?;";
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs =  null;
		String value = null;
		try{
			conn = datasource.getConnection();
			pstmt  = conn.prepareStatement(sql);
			pstmt.setString(1, key);
			rs = pstmt.executeQuery();
			if(rs.next()){
				value = rs.getString(1);
			}
		}catch(SQLException sqle){
			logger.error(sqle.getMessage(),sqle);
		}finally{
			try{
				if(rs!=null){
					rs.close();
					rs = null;
				}
				if(pstmt!=null){
					pstmt.close();
					pstmt = null;
				}
				if(conn!=null){
					conn.close();
					conn  = null;
				}
			}catch(SQLException sqle){
				logger.error(sqle.getMessage(),sqle);
			}
		}
		return value;
	}

	@Override
	public void set(String key, String value) {
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		try{
			conn = datasource.getConnection();
			pstmt  = conn.prepareStatement("SELECT * FROM config WHERE `key`=?;");
			pstmt.setString(1, key);
			rs  = pstmt.executeQuery();
			pstmt.close();
			if(rs.next()){
				pstmt = conn.prepareStatement("UPDATE config SET `value`=? WHERE `key`=?;");
				pstmt.setString(1, value);
				pstmt.setString(2, key);
				pstmt.execute();
			}else{
				pstmt = conn.prepareStatement("INSERT INTO config (`key`,`value`) VALUES (?,?);");
				pstmt.setString(1, key);
				pstmt.setString(2, value);
				pstmt.execute();
			}
		}catch(SQLException sqle){
			logger.error(sqle.getMessage(),sqle);
		}finally{
			try{
				if(rs!=null){
					rs.close();
					rs = null;
				}
				if(pstmt!=null){
					pstmt.close();
					pstmt = null;
				}
				if(conn!=null){
					conn.close();
					conn  = null;
				}
			}catch(SQLException sqle){
				logger.error(sqle.getMessage(),sqle);
			}
		}
	}

	@Override
	public boolean isExist(String key) {
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		boolean result = false;
		try{
			conn = datasource.getConnection();
			pstmt  = conn.prepareStatement("SELECT * FROM config WHERE `key`=?;");
			pstmt.setString(1, key);
			rs  = pstmt.executeQuery();
			result = rs.next();
		}catch(SQLException sqle){
			logger.error(sqle.getMessage(),sqle);
		}finally{
			try{
				if(rs!=null){
					rs.close();
					rs = null;
				}
				if(pstmt!=null){
					pstmt.close();
					pstmt = null;
				}
				if(conn!=null){
					conn.close();
					conn  = null;
				}
			}catch(SQLException sqle){
				logger.error(sqle.getMessage(),sqle);
			}
		}
		return result;
	}

	@Override
	public void delete(String key) {
		Connection conn = null;
		PreparedStatement pstmt = null;
		try{
			conn = datasource.getConnection();
			pstmt  = conn.prepareStatement("DELETE FROM `config` WHERE `key`=?;");
			pstmt.setString(1, key);
			pstmt.execute();
		}catch(SQLException sqle){
			logger.error(sqle.getMessage(),sqle);
		}finally{
			try{
				if(pstmt!=null){
					pstmt.close();
					pstmt = null;
				}
				if(conn!=null){
					conn.close();
					conn  = null;
				}
			}catch(SQLException sqle){
				logger.error(sqle.getMessage(),sqle);
			}
		}
	}

}