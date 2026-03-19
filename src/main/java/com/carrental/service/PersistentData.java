package main.java.com.carrental.service;

import main.java.com.carrental.model.Customer;


/*
 * Persistent data used for the duration of the customers login, as a new customer has logged in 
 * persistent data will change to match customer
 */

public class PersistentData {

	public static Customer Persistentcustomer; 
	
	public static void setCustomer(Customer customer) {
		Persistentcustomer = customer;
	}
}
