package main.java.com.carrental.service;

import main.java.com.carrental.dao.CustomerDAO;
import main.java.com.carrental.model.Customer;
import main.java.com.carrental.util.SecurePasswordHasher;

/**
 * All business related processes that deals with the customer
 * such as login and registration
 */
public class CustomerService {
	
	
	/**
	 * function to login, as it is named. Pulls information from the database
	 * in order to verify
	 * @param email
	 * @param raw password unhashed
	 * @return customer null if customer not found
	 */
	public Customer Login(String email,String rawPassword) {
		Customer customer = CustomerDAO.findByEmail(email);
		if(customer == null) {
			System.out.println("Could not find email in database");
			return null;
		}
		boolean login = SecurePasswordHasher.verifyPassword(rawPassword, customer.getPassword());
		if(!login) { 
			customer = null;
			System.err.println("Password incorrect, try again!");
		}
		return customer;
	}
	/**
	 * 
	 * Function to register, will insert a new record into the database
	 * @param email
	 * @param rawPassword
	 * @param username
	 * @return Customer 
	 */
	public Customer Register(String email, String rawPassword, String username) {
		String hashedpassword = SecurePasswordHasher.hashPassword(rawPassword);
		CustomerDAO.insertRecord(username, email, hashedpassword);
		Customer customer = new Customer();
		customer.setEmail(email);
		customer.setCustomerName(username);
		customer.setPassword(hashedpassword);
		return customer;
	}

	/**
	 * 
	 * Function to register, will insert a new record into the database
	 * @param customer
	 * @return Customer
	 */
	public void Register(Customer customer) {
		CustomerDAO.insertRecord(customer.getCustomerName(), customer.getEmail(), customer.getPassword());
	}
	
}
