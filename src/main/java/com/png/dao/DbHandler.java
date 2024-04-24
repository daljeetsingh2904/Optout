package com.png.dao;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.sql.DataSource;

import org.apache.commons.dbcp2.BasicDataSource;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import com.png.services.Constants;
import com.png.services.PropertyHandler;
import com.png.services.Utility;

public class DbHandler {

	private static final Logger logger = Logger.getLogger(DbHandler.class);

	public Connection getConnection() {
		Connection connection = null;
		try {
			// Load the MySQL JDBC driver
			Class.forName("com.mysql.cj.jdbc.Driver");

			// Establish the connection
			String url =PropertyHandler.getInstance().getValue("url"); 
			String username = PropertyHandler.getInstance().getValue("username");
			String password = PropertyHandler.getInstance().getValue("password");
			connection = DriverManager.getConnection(url, username, password);
		} catch (Exception e) {
			System.out.println("Error " + e.getMessage());
		}
		return connection;
	}

	public void insertOptOut(Connection con, String mobileNumber, String callerId) {
		String query = null;
		PreparedStatement pstmt = null;
		PropertyConfigurator.configure(com.png.services.Constants.LOG_PAH);
		try {
			query = "INSERT IGNORE INTO _tbl_optout_details (MobileNumberId,CallerId,OptOut,Status) VALUES (?, ?, now(), ?)";
			pstmt = con.prepareStatement(query);
			logger.info("Query is --->> " + query);
			pstmt.setString(1, mobileNumber);
			pstmt.setString(2, callerId);
			pstmt.setInt(3, 1);
			logger.info("MobileNumber is -->> " + mobileNumber + " and CallerId is -->> " + callerId);

			pstmt.executeUpdate();
		} catch (Exception exception) {
			exception.printStackTrace();
		}
	}

	public String fetchMobileNumberUniqueId(Connection conn, String mobileNumber) {
		PropertyConfigurator.configure(Constants.LOG_PAH);
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		int mobileUniqueId = 0;
		String encryptedMobileNumbe;
		String query = null;
		try {
			query = "SELECT MobileNumberId,MobileNumber FROM _tbl_mobilenumber_details WHERE MobileNumber=? AND Status=1";
			pstmt = conn.prepareStatement(query);
			pstmt.setString(1, Utility.encrypt(mobileNumber));
			rs = pstmt.executeQuery();
			if (rs.next()) {
				mobileUniqueId = rs.getInt("MobileNumberId");
				mobileNumber=rs.getString("MobileNumber");
			} else {
				 if (pstmt != null) {
		                pstmt.close();
		            }
				 if(rs!=null) {
					 rs.close();
				 }
				query = "INSERT IGNORE INTO _tbl_mobilenumber_details (MobileNumber) VALUES (?)";
				pstmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
				pstmt.setString(1, Utility.encrypt(mobileNumber));
				int rowsAffected = pstmt.executeUpdate();
				if (rowsAffected > 0) {
					rs = pstmt.getGeneratedKeys();
					if (rs.next()) {
						mobileUniqueId = rs.getInt(1);
					}
				}
			}
		} catch (Exception exception) {
			logger.error("Error: " + exception.getMessage(), exception);
		} finally {
			// Close PreparedStatement and ResultSet
			if (rs != null) {
				try {
					rs.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			if (pstmt != null) {
				try {
					pstmt.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		return mobileNumber;
	}
	
	
	public boolean updateOptDetails(Connection conn, String mobileNumber) {
		PreparedStatement stmt = null;
		String query = null;
		ResultSet rs = null;
		PropertyConfigurator.configure(Constants.LOG_PAH);
		int mobileNumberId=0;
		int rowsAffected=0;
		try {
			DatabaseMetaData metaData = conn.getMetaData();
			query = "SELECT MobileNumberId FROM _tbl_mobilenumber_details WHERE MobileNumber=? AND Status=1";
			stmt = conn.prepareStatement(query);
			stmt.setString(1, mobileNumber);
			rs = stmt.executeQuery();
		   
			while(rs.next()) {
				mobileNumberId=rs.getInt("MobileNumberId");
			}
			if (stmt != null) {
				stmt.close();
			}
			int optin=fetchOptinStatus(conn, mobileNumberId);
			if(optin==1) {
			query = "update _tbl_obdcall_details set OptinStatus=0,OptOutDate=now() where MobileNumberId=?";
			stmt = conn.prepareStatement(query);
			stmt.setInt(1, mobileNumberId);
			rowsAffected=stmt.executeUpdate();
			logger.info("Query is --->> " + query+" Mobile Number is "+mobileNumberId+" for database -->>> "+metaData.getURL().replaceAll(".*/([^/?]+).*", "$1"));
			logger.info("Rows affected --->>>> "+rowsAffected);
			}else {
				logger.info("OptinStatus is already 0 for MobileNumber -->> "+mobileNumberId+" in database --->> "+metaData.getURL().replaceAll(".*/([^/?]+).*", "$1"));
			}
			return true;
		} catch (Exception exception) {
			exception.printStackTrace();
		} finally {
			if (stmt != null) {
				try {
					stmt.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		return false;
	}
	
	
	
	 public DataSource[] initializeDataSources() {
		 String[] database=PropertyHandler.getInstance().getValue("optout_databases").split(",");
//		 String[] database= {"travelkit_application","pilot_application","h2h_application"};
	    	BasicDataSource[] dataSources = new BasicDataSource[database.length];
	        for (int i = 0; i < dataSources.length; i++) {
	            BasicDataSource dataSource = new BasicDataSource();
//	            logger.info("database is "+database[i]);
	            dataSource.setDriverClassName("com.mysql.cj.jdbc.Driver");
	            dataSource.setUrl("jdbc:mysql://localhost:3306/" + database[i]);
	            dataSource.setUsername(PropertyHandler.getInstance().getValue("username"));
	            dataSource.setPassword(PropertyHandler.getInstance().getValue("password"));
	            // Set other connection pool properties if needed
	            dataSources[i] = dataSource;
	        }
	        return dataSources;
	    }
	 
	 
	 
	 public int fetchOptOutDid(Connection conn,String did) {
			PreparedStatement pstmt = null;
			String query = null;
			PropertyConfigurator.configure(Constants.LOG_PAH);
			ResultSet resultSet = null;
			int count=0;
			try {
				query = "select count(*) as count from _tbl_did_configuration where servicenumber=? and OptOutFlag=1 and process_status=1";
				logger.info("Query is -->> " + query);
				pstmt = conn.prepareStatement(query);
				pstmt.setString(1, did);
				resultSet = pstmt.executeQuery();
				while (resultSet.next()) {
					count=resultSet.getInt("count");
				}
				logger.info("DID is --->> " + did+", did is "+(count>0?"present":"not present")+" in database .");
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
			}
			return count;
		}
	 
	 
	 public int fetchOptinStatus(Connection conn,int mobileNumberId) {
			PreparedStatement pstmt = null;
			String query = null;
			PropertyConfigurator.configure(Constants.LOG_PAH);
			ResultSet resultSet = null;
			int optinStatus=0;
			try {
				DatabaseMetaData metaData = conn.getMetaData();
				query = "select OptinStatus from _tbl_obdcall_details where MobileNumberId=?";
				logger.info("Query is -->> " + query+" for database -->> "+metaData.getURL().replaceAll(".*/([^/?]+).*", "$1"));
				pstmt = conn.prepareStatement(query);
				pstmt.setInt(1, mobileNumberId);
				resultSet = pstmt.executeQuery();
				while (resultSet.next()) {
					optinStatus=resultSet.getInt("OptinStatus");
				}
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
			}
			return optinStatus;
		}

}
