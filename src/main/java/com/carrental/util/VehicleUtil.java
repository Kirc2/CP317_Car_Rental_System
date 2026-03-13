package main.java.com.carrental.util;

import main.java.com.carrental.model.Vehicle.VehicleType;

public class VehicleUtil {

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
	
}
