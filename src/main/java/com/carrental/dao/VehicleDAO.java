package main.java.com.carrental.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import main.java.com.carrental.model.Vehicle;
import main.java.com.carrental.model.Vehicle.VehicleStatus;
import main.java.com.carrental.model.Vehicle.VehicleType;
import main.java.com.carrental.util.VehicleUtil;

/**
 * Get all Vehicle info from this class, this should be the only class that
 * sends and receives data from the database
 */
public class VehicleDAO {

	public VehicleDAO() {

	}

	/**
	 * Master search function, will search from every single filter there is
	 * 
	 * @param type
	 * @param startdate
	 * @param endDate
	 * @param price
	 * @return list of specific vehicles searched
	 */
	public List<Vehicle> searchVehicle(Vehicle.VehicleType type, Vehicle.VehicleStatus status, LocalDate startDate,
			LocalDate endDate, double price, int year) {
		StringBuilder sql = new StringBuilder("SELECT v.* FROM vehicles v WHERE 1=1");
		List<Object> params = new ArrayList<>();

		if (type != null) {
			sql.append(" AND v.vehicle_type = ?");
			params.add(type.name());
		}

		if (status != null) {
			sql.append(" AND v.car_status = ?");
			params.add(status.name());
		}

		if (year != 0) {
			sql.append(" AND v.year = ?");
			params.add(year);
		}

		if (price > 0) {
			sql.append(" AND v.daily_rate <= ?");
			params.add(price);
		}

		if (startDate != null && endDate != null) {
			sql.append(
					" AND NOT EXISTS (SELECT 1 FROM rentals r WHERE r.vehicle_id = v.id AND r.start_date < ? AND r.end_date > ?)");
			params.add(endDate);
			params.add(startDate);
		}

		ResultSet rs = MySQL.fetch(sql.toString(), params.toArray());
		List<Vehicle> vehicles = new ArrayList<>();
		try {
			if (rs == null) {
				System.out.println("Database fetch returned null – check connection or query");
				return vehicles;
			}
			while (rs.next()) {
				Vehicle vec = new Vehicle(rs.getString("id"), rs.getString("license_plate"), rs.getString("make"),
						rs.getString("model"), rs.getInt("year"), rs.getDouble("daily_rate"),
						VehicleUtil.getTypeFromString(rs.getString("vehicle_type")) // column name
				);
				vehicles.add(vec);
			}
		} catch (SQLException e) {
			System.out.println("Error mapping vehicle: " + e.getMessage());
			e.printStackTrace();
		}
		return vehicles;
	}

	/**
	 * Find every vehicle available for rent
	 * 
	 * @return a list of vehicles that are available
	 */
	public List<Vehicle> findAvailableVehicles() {
		List<Vehicle> vehicles = new ArrayList<Vehicle>();
		String query = "SELECT * from vehicles where car_status='AVAILABLE'";
		ResultSet set = MySQL.fetch(query);
		try {
			Vehicle vehicle = new Vehicle();
			while (set.next()) {
				vehicle.setId(set.getString("id"));
				vehicle.setLicensePlate(set.getString("license_plate"));
				vehicle.setMake(set.getString("make"));
				vehicle.setModel(set.getString("model"));
				vehicle.setYear(set.getInt("year"));
				vehicle.setDailyRate(set.getDouble("daily_rate"));
				String type = set.getString("vehicle_type");
				vehicle.setType(VehicleUtil.getTypeFromString(type));
				vehicles.add(vehicle);
			}
		} catch (SQLException e) {
			System.err.print("Could not find SQL column");
		}
		return vehicles;
	}

	/**
	 * Find every vehicle that exists in the database
	 * 
	 * @return List of every vehicle in the database
	 */
	public List<Vehicle> findAllVehicles() {
		List<Vehicle> vehicles = new ArrayList<Vehicle>();
		String query = "SELECT * from vehicles";
		ResultSet set = MySQL.fetch(query);
		try {
			Vehicle vehicle = new Vehicle();
			while (set.next()) {
				vehicle.setId(set.getString("id"));
				vehicle.setLicensePlate(set.getString("license_plate"));
				vehicle.setMake(set.getString("make"));
				vehicle.setModel(set.getString("model"));
				vehicle.setYear(set.getInt("year"));
				vehicle.setDailyRate(set.getDouble("daily_rate"));
				String type = set.getString("vehicle_type");
				vehicle.setType(VehicleUtil.getTypeFromString(type));
				vehicles.add(vehicle);
			}
		} catch (SQLException e) {
			System.err.print("Could not find SQL column");
		}
		return vehicles;
	}

	/**
	 * Find every vehicle with given type
	 * 
	 * @param VehicleType type of vehicle to display
	 * @return a list of vehicles that align with specified type
	 */
	public List<Vehicle> findByType(VehicleType type) {
		List<Vehicle> vehicles = new ArrayList<Vehicle>();
		String query = "SELECT * from vehicles where vehicle_type='" + type.toString() + "'";
		ResultSet set = MySQL.fetch(query);
		try {
			Vehicle vehicle = new Vehicle();

			while (set.next()) {
				vehicle.setId(set.getString("id"));
				vehicle.setLicensePlate(set.getString("license_plate"));
				vehicle.setMake(set.getString("make"));
				vehicle.setModel(set.getString("model"));
				vehicle.setYear(set.getInt("year"));
				vehicle.setDailyRate(set.getDouble("daily_rate"));
				vehicle.setType(type);
				vehicles.add(vehicle);
			}
		} catch (SQLException e) {
			System.err.print("Could not find SQL column");
		}
		return vehicles;
	}

	/**
	 * Find a vehicle with given identification
	 * 
	 * @param ID
	 * @return Vehicle vehicle found null if vehicle wasnt found
	 */
	public static Vehicle findByID(String ID) {
		Vehicle vehicle = null;
		String query = "SELECT * FROM vehicles WHERE id = ?";
		ResultSet set = MySQL.fetch(query, ID);
		try {
			if (set != null && set.next()) {
				vehicle = new Vehicle();
				vehicle.setId(ID);
				vehicle.setLicensePlate(set.getString("license_plate"));
				vehicle.setMake(set.getString("make"));
				vehicle.setModel(set.getString("model"));
				vehicle.setYear(set.getInt("year"));
				vehicle.setDailyRate(set.getDouble("daily_rate"));

				String typeStr = set.getString("vehicle_type");
				if (typeStr != null) {
					vehicle.setType(VehicleUtil.getTypeFromString(typeStr));
				}

				String statusStr = set.getString("car_status");
				if (statusStr != null) {
					vehicle.setStatus(VehicleUtil.getStatusFromString(statusStr));
				}
			} else {
				System.out.println("No vehicle found with ID: " + ID);
			}
		} catch (SQLException e) {
			System.out.println("SQL error in findByID: " + e.getMessage());
			e.printStackTrace();
		}

		return vehicle; // will be null if not found
	}

	/**
	 * Update the static of the vehicle, for example, "AVAILABLE" "RENTED"
	 * "MAINTENANCE"
	 * 
	 * @param Vehicle identification
	 * @param Status  the status you want the car to change to
	 * @return boolean true if MySQL query was successful
	 */
	public boolean updateStatus(String id, VehicleStatus status) {
		String query = "UPDATE vehicles SET car_status='" + status.toString() + "' where id='" + id + "'";
		return MySQL.update(query);
	}

	/**
	 * Insert a record of a vehicle into the MySQL database
	 * 
	 * @param Vehicle to insert into the database
	 */
	public static void insertRecord(Vehicle vec) {
		String query = "INSERT INTO vehicles (license_plate, make, model, year, daily_rate, vehicle_type, car_status) "
				+ "VALUES ('" + vec.getLicensePlate() + "','" + vec.getMake() + "','" + vec.getModel() + "',"
				+ vec.getYear() + "," + vec.getDailyRate() + ",'" + vec.getType().toString() + "','"
				+ vec.getStatus().toString() + "')";

		MySQL.insert(query);
	}

}
