package main.java.com.carrental.service;

import java.time.LocalDateTime;

import main.java.com.carrental.dao.RentalDAO;
import main.java.com.carrental.dao.VehicleDAO;
import main.java.com.carrental.model.Customer;
import main.java.com.carrental.model.Rental;
import main.java.com.carrental.model.Vehicle;

public class RentalService {

	/**
	 * Reserves a single vehicle for a customer, creates a record in the Rentals table
	 * Once, reservation is done and or cancelled, the record is deleted
	 * @param Customer that wants to reserve this vehicle
	 * @param Vehicle to reserve
	 * @param Pickup date
	 * @param Planned return date
	 * @return The newly inserted record of Rental
	 */
	public Rental reserveVehicle(Customer customer,Vehicle vec, LocalDateTime pickupDate, LocalDateTime plannedReturnDate) {
		Rental rental = new Rental();
		rental.setCustomer(customer);
		rental.setVehicle(vec);
		rental.setPickupDate(pickupDate);
		rental.setPlannedReturnDate(plannedReturnDate);
		rental.setStatus(Rental.RentalStatus.RESERVED);

		RentalDAO.insertRecord(rental);
		
		return rental;
	}
	
}
