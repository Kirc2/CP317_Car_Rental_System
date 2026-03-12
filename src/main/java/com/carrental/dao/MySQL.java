/*
 * Author : Navin Sethi
 * ID	  : 169086962
 * Email  : seth6962@mylaurier.ca
 * 
 */

package main.java.com.carrental.dao;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * <p>
 * Create a new instance of the mysql server for the car_rental_system.
 * Only one instance of this class needs to be created however most fuctions
 * can be called anywhere in the program
 * <p>
 */
public class MySQL {
	
	private String URL;
	private String username;
	private String password;
	private static Connection connection;
	
	
	private static void setParameters(PreparedStatement pstmt, Object... params) throws SQLException {
	    for (int i = 0; i < params.length; i++) {
	        Object param = params[i];
	        if (param == null) {
	            pstmt.setNull(i + 1, java.sql.Types.NULL);
	        } else if (param instanceof String) {
	            pstmt.setString(i + 1, (String) param);
	        } else if (param instanceof Integer) {
	            pstmt.setInt(i + 1, (Integer) param);
	        } else if (param instanceof Long) {
	            pstmt.setLong(i + 1, (Long) param);
	        } else if (param instanceof Double) {
	            pstmt.setDouble(i + 1, (Double) param);
	        } else if (param instanceof Boolean) {
	            pstmt.setBoolean(i + 1, (Boolean) param);
	        } else if (param instanceof java.time.LocalDate) {
	            pstmt.setDate(i + 1, java.sql.Date.valueOf((java.time.LocalDate) param));
	        } else if (param instanceof java.time.LocalTime) {
	            pstmt.setTime(i + 1, java.sql.Time.valueOf((java.time.LocalTime) param));
	        } else {
	            pstmt.setObject(i + 1, param);
	        }
	    }
	}
	
	/**
	 * Call this function to connect to the mysql database, should only be ran once
	 */
	public void Connect_to_MySQL_Database() {
		
		String dbHost = System.getenv().getOrDefault("MYSQL_HOST", "localhost");
		String dbPort = System.getenv().getOrDefault("MYSQL_PORT", "3306");
		String dbName = System.getenv().getOrDefault("MYSQL_DATABASE", "Car_Rental_System");
		String dbUser = System.getenv().getOrDefault("MYSQL_USER", "root");
		String dbPass = System.getenv().getOrDefault("MYSQL_PASSWORD", "root");
		
		
	    URL = "jdbc:mysql://" + dbHost + ":" + dbPort + "/" + dbName +
	            "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
	      username = dbUser;
	      password = dbPass;
		try {
			connection = DriverManager.getConnection(URL, username, password);
		} catch (SQLException e) {
			System.err.print("Database is offline. Please try again later");
		}
		
		System.out.println("Success connecting to the DATABASE");
	}
	
	/**
	 * Fetch function to get information from the MySQL server
	 * @param selectStmt the string statement of which the MySQL server will execute
	 * @param you can put as many parameters as you please, just make sure they match the sql statements ?
	 * @return result the ResultSet representation of the MySQL servers reply
	 * 
	 */
	public static ResultSet fetch(String selectStmt, Object... params) {
		ResultSet result = null;
		try {
			PreparedStatement pstmt = connection.prepareStatement(selectStmt);
			setParameters(pstmt, params);
			result = pstmt.executeQuery();
		} catch(SQLException e) {
			e.printStackTrace();
			System.err.print("Failed to fetch data from database, Please try again later");
		}
		
		return result;
	}
	
	/**
	 * Insert function to create a record of data for the MySQL server
	 * @param insertStmt the string statement of which the MySQL server will execute
	 * @param you can put as many parameters as you please, just make sure they match the sql statements
	 * @return success or not
	 */
	
	public static boolean insert(String insertStmt, Object... params) {
	    boolean success = false;
	    try (PreparedStatement pstmt = connection.prepareStatement(insertStmt)) {
	        setParameters(pstmt, params);
	        int rowsAffected = pstmt.executeUpdate();
	        success = rowsAffected > 0;
	    } catch (SQLException e) {
	        System.err.print("Failed to execute insert statement");
	        e.printStackTrace();
	    }
	    return success;
	}
	
	/**
	 * 
	 * Update function to update a record and or table for the MySQL server
	 * @param the MySQL statement
	 * @return boolean if statement successfully executed
	 */
	public static boolean update(String updateStmt, Object... params) {
	    boolean success = false;
	    try (PreparedStatement pstmt = connection.prepareStatement(updateStmt)) {
	        setParameters(pstmt, params);
	        int rowsAffected = pstmt.executeUpdate();
	        success = rowsAffected > 0;
	    } catch (SQLException e) {
	        System.err.print("Failed to execute update statement statement");
	        e.printStackTrace();
	    }
	    return success;
	}
	
	/**
	 * Function to close the database connection cleanly
	 */
	public static void closeConnection() {
		try {
			connection.close();
		} catch (SQLException e) {
			System.err.print("Could not teardown connection with the database server");
		}
	}
	
}
