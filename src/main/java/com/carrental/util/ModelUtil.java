package main.java.com.carrental.util;

import main.java.com.carrental.model.Rental.RentalStatus;
import main.java.com.carrental.model.Vehicle.VehicleStatus;
import main.java.com.carrental.model.Vehicle.VehicleType;

public class ModelUtil {

	/**
	 * Get type of vehicle from the string, mostly used to parse the databases response of enum vehicle_type
	 * @param String type
	 * @return VehicleType the enum vehicle type, returns null if not a valid vehicle
	 */
	public static VehicleType getTypeFromString(String typeS) {
		VehicleType type = null;
		switch(typeS) {
		case "SEDAN":
			type = VehicleType.SEDAN;
			break;
		case "SUV":
			type =  VehicleType.SUV;
			break;
		case "TRUCK":
			type = VehicleType.TRUCK;
			break;
		case "CAR":
			type = VehicleType.CAR;
			break;
		}
		
		return type;
	}
	/**
	 * Get status of vehicle from string type to enum type used to parse database and json response
	 * @param String status
	 * @return VehicleStatus enum
	 */
	public static VehicleStatus getVehicleStatusFromString(String statusS) {
		VehicleStatus vecstat = null;
		switch(statusS) {
		case "AVAILABLE":
			vecstat = VehicleStatus.AVAILABLE;
			break;
		case "RENTED":
			vecstat = VehicleStatus.RENTED;
			break;
		case "MAINTENANCE":
			vecstat = VehicleStatus.MAINTENANCE;
			break;
		}
		return vecstat;
	}
	
	public static String RentalStatusToString(RentalStatus rs) {
		switch(rs) {
			case RESERVED:
				return "RESERVED";
			case ACTIVE:
				return "ACTIVE";
			case COMPLETED:
				return "COMPLETED";
			case CANCELLED:
				return "CANCELLED";
			}
		return null;
	}
	
	public static RentalStatus getRentalStatusFromString(String status) {
		RentalStatus statusnum = null;
		switch(status) {
		case "RESERVED":
			statusnum = RentalStatus.RESERVED;
			break;
		case "ACTIVE":
			statusnum = RentalStatus.ACTIVE;
			break;
		case "COMPLETED":
			statusnum = RentalStatus.COMPLETED;
			break;
		case "CANCELLED":
			statusnum = RentalStatus.CANCELLED;
			break;
		}
		
		return statusnum;
	}
	
}
