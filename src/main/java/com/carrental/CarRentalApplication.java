package main.java.com.carrental;

import main.java.com.carrental.dao.MySQL;
import main.java.com.carrental.gui.CarRentalServer;

public class CarRentalApplication {

	public static void main(String[] args) {
		MySQL mysql = new MySQL();
		mysql.Connect_to_MySQL_Database();
		
		new CarRentalServer();
		
		
	}

}
