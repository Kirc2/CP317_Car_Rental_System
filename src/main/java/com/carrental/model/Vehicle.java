package main.java.com.carrental.model;

/**
 * Vehicle class model, mostly used to store information for use in another function
 */
public class Vehicle {
	private String id;
	private String licensePlate;
	private String make;
	private String model;
	private int year;
	private double dailyRate;
	private VehicleStatus status;
	private VehicleType type;

	public enum VehicleType {SEDAN, SUV, TRUCK, CAR};
	public enum VehicleStatus {AVAILABLE, RENTED, MAINTENANCE};
	
	public Vehicle() {
		
	}
	
	public Vehicle(String id, String licensePlate, String make, String model, int year, double dailyRate, VehicleType type) {
		this.id = id;
		this.licensePlate = licensePlate;
		this.make = make;
		this.model = model;
		this.year = year;
		this.dailyRate = dailyRate;
		this.type = type;
		status = VehicleStatus.AVAILABLE;
	}
	
	/**
	 * Get type of vehicle from the string, mostly used to parse the databases response of enum vehicle_type
	 * @param String type
	 * @return VehicleType the enum vehicle type
	 */
	public VehicleType getTypeFromString(String typeS) {
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
	
		
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getLicensePlate() {
		return licensePlate;
	}

	public void setLicensePlate(String licensePlate) {
		this.licensePlate = licensePlate;
	}

	public String getMake() {
		return make;
	}

	public void setMake(String make) {
		this.make = make;
	}

	public String getModel() {
		return model;
	}

	public void setModel(String model) {
		this.model = model;
	}

	public int getYear() {
		return year;
	}

	public void setYear(int year) {
		this.year = year;
	}

	public double getDailyRate() {
		return dailyRate;
	}

	public void setDailyRate(double dailyRate) {
		this.dailyRate = dailyRate;
	}

	public VehicleStatus getStatus() {
		return status;
	}

	public void setStatus(VehicleStatus status) {
		this.status = status;
	}

	public VehicleType getType() {
		return type;
	}

	public void setType(VehicleType type) {
		this.type = type;
	}
	
}
