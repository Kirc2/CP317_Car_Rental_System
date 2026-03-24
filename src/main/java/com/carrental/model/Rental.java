package main.java.com.carrental.model;

import java.time.LocalDate;
import java.time.LocalDate;

/**
 * Rental class model, mostly used to store information for use in another function
 */
public class Rental {
	private String rentalID;
	private Customer customer;
	private Vehicle vehicle;
	private LocalDate pickupDate;
	private LocalDate plannedReturnDate;
	private LocalDate actualReturnDate;
	private double totalCost;
	private RentalStatus status;
	private Payment payment;
	
	public enum RentalStatus {RESERVED, ACTIVE, COMPLETED, CANCELLED};
	
	public Rental() {}	
	
	public String getRentalID() {
		return rentalID;
	}

	public void setRentalID(String rentalID) {
		this.rentalID = rentalID;
	}

	public Customer getCustomer() {
		return customer;
	}

	public void setCustomer(Customer customer) {
		this.customer = customer;
	}

	public Vehicle getVehicle() {
		return vehicle;
	}

	public void setVehicle(Vehicle vehicle) {
		this.vehicle = vehicle;
	}

	public LocalDate getPickupDate() {
		return pickupDate;
	}

	public void setPickupDate(LocalDate pickupDate) {
		this.pickupDate = pickupDate;
	}

	public LocalDate getPlannedReturnDate() {
		return plannedReturnDate;
	}

	public void setPlannedReturnDate(LocalDate plannedReturnDate) {
		this.plannedReturnDate = plannedReturnDate;
	}

	public LocalDate getActualReturnDate() {
		return actualReturnDate;
	}

	public void setActualReturnDate(LocalDate actualReturnDate) {
		this.actualReturnDate = actualReturnDate;
	}

	public double getTotalCost() {
		return totalCost;
	}

	public void setTotalCost(double totalCost) {
		this.totalCost = totalCost;
	}

	public RentalStatus getStatus() {
		return status;
	}

	public void setStatus(RentalStatus status) {
		this.status = status;
	}

	public Payment getPayment() {
		return payment;
	}

	public void setPayment(Payment payment) {
		this.payment = payment;
	};
	
	
}
