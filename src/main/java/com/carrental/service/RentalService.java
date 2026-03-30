package main.java.com.carrental.service;

import java.time.LocalDate;
import main.java.com.carrental.dao.RentalDAO;
import main.java.com.carrental.dao.VehicleDAO;
import main.java.com.carrental.model.Customer;
import main.java.com.carrental.model.Rental;
import main.java.com.carrental.model.Vehicle;
import main.java.com.carrental.util.JSONUtil;

/**
 * All business related processes that deals with the customer
 * such as vehicle reservation
 */

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
	public boolean reserveVehicle(Rental rental) {
		boolean insertted = RentalDAO.insertRecord(rental);
		
		return insertted;
	}
	
	public Rental reserveVehicle(Customer customer,Vehicle vec, LocalDate pickupDate, LocalDate plannedReturnDate) {
		Rental rental = new Rental();
		rental.setCustomer(customer);
		rental.setVehicle(vec);
		rental.setPickupDate(pickupDate);
		rental.setPlannedReturnDate(plannedReturnDate);
		rental.setStatus(Rental.RentalStatus.RESERVED);

		RentalDAO.insertRecord(rental);
		
		return rental;
	}
	
	
	public Rental getRentalFromJson (String body) {
		String vehicleID = JSONUtil.extractField(body, "id");
		String startDateStr = JSONUtil.extractField(body, "startDate");
		String endDateStr = JSONUtil.extractField(body, "endDate");
		System.out.println(vehicleID);

		LocalDate startDate = (startDateStr != null && !startDateStr.isEmpty()) 
                ? LocalDate.parse(startDateStr) : null;
		LocalDate endDate = (endDateStr != null && !endDateStr.isEmpty()) 
              ? LocalDate.parse(endDateStr) : null;
		
		Rental rental = new Rental();
		rental.setCustomer(PersistentData.Persistentcustomer);
		rental.setVehicle(VehicleDAO.findByID(vehicleID));
		rental.setPickupDate(startDate);
		rental.setPlannedReturnDate(endDate);
		rental.setStatus(Rental.RentalStatus.RESERVED);
		
		long daysElapsed = java.time.temporal.ChronoUnit.DAYS.between(startDate, endDate);
		
		rental.setTotalCost(daysElapsed*rental.getVehicle().getDailyRate());
		
		return rental;
	}
	
	
}