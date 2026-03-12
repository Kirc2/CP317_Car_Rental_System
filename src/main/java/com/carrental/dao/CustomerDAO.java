package main.java.com.carrental.dao;
import java.sql.ResultSet;
import java.sql.SQLException;

import main.java.com.carrental.model.Customer;

/**
 * Get all customer info from this class, this
 * should be the only class that sends and
 * recieves data from the database
 */
public class CustomerDAO {
	
	/**
	 * Finds records of a customer based on the given username,
	 * pretty flawed since multiple names could exist in the database
	 * @param username
	 * @return the customer that was found, null if no customer
	 */
	public static Customer findByUsername(String username) {
		String query = "SELECT * FROM customers WHERE name = ?";
		ResultSet result = MySQL.fetch(query,username);
		Customer customer = null;
		try {
			result.next();
			String id = result.getString(0);
			String name = result.getString(1);
			String email = result.getString(2);
			String password = result.getString(3);
			customer = new Customer(id, name, email,password);
		} catch(SQLException e) {
			System.err.print("This username doesent exist in our database, would you like to register?");
		}
		return customer;
	}
	
	/**
	 * Finds records of a customer based on the given identification number
	 * @param customer_id
	 * @return the customer that was found, null if no customer
	 */
	public static Customer findByID (String customer_id) {
		String query = "SELECT * FROM customers WHERE id = ?";
		ResultSet result = MySQL.fetch(query,customer_id);
		Customer customer = null;
		try {
			result.next();
			String id = result.getString("id");
			String name = result.getString("name");
			String email = result.getString("email");
			String password = result.getString("password");
			
			customer = new Customer(id, name, email, password);

		} catch(SQLException e) {
			System.err.print("This Customer identification does not exist?");
			e.printStackTrace();
		}
		return customer;
	}
	
	/**
	 * Finds records of a customer based on the given email address
	 * @param email
	 * @return the customer that was found, null if no customer
	 */
	public static Customer findByEmail(String email) {
		String query = "SELECT * FROM customers WHERE email = ?";
		ResultSet result = MySQL.fetch(query,email);
		Customer customer = null;
		
		try {
			if(result.next()) {
				String id = result.getString("id");
				String name = result.getString("name");
				String password = result.getString("password");
				
				customer = new Customer(id, name, email, password);
			} else {
				System.err.print("This email doesent exist in our database, would you like to register?");
			}
		} catch(SQLException e) {
			e.printStackTrace();
			System.err.print("This email doesent exist in our database, would you like to register?");
		}
		return customer;
	}
	
	/**
	 * create a new customer record into the database, should be used in events
	 * of registration
	 * @param username
	 * @param email
	 * @param password
	 */
	public static void insertRecord(String username, String email, String password) {
		String query = "INSERT INTO customers(name, email, password) VALUES(?,?,?);";
		MySQL.insert(query,username,email,password);
		
	}
}
