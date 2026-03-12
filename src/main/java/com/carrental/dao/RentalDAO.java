package main.java.com.carrental.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.time.LocalDateTime;
import main.java.com.carrental.model.Rental;

/**
 * Get all Rental info from this class, this
 * should be the only class that sends and
 * recieves data from the database
 */
public class RentalDAO {

	public boolean cancelRental(int rentalId) {
	    String sqlUpdateRental = "UPDATE rentals SET status = 'CANCELLED' WHERE rental_id = ? AND status != 'CANCELLED'";
	    String sqlUpdateVehicle = "UPDATE vehicles SET status = 'AVAILABLE' WHERE vehicle_id = (SELECT vehicle_id FROM rentals WHERE rental_id = ?)";
	    String insertNote = "INSERT INTO rental_notes (rental_id, note, created_at) VALUES (?, 'Cancelled by customer', NOW())";

	    try {
	        // Use transaction if possible (assuming MySQL.update supports it)
	        boolean rentalUpdated = MySQL.update(sqlUpdateRental, rentalId);
	        if (rentalUpdated) {
	            MySQL.update(sqlUpdateVehicle, rentalId);
	            MySQL.insert(insertNote, rentalId);
	            return true;
	        }
	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	    return false;
	}
	
	public List<Rental> getRentalsByCustomer(int customerId) {
	    List<Rental> rentals = new ArrayList<>();
	    String sql = "SELECT * FROM rentals WHERE customer_id = ? ORDER BY start_date DESC";
	    ResultSet rs = MySQL.fetch(sql, customerId);
	    try {
	        while (rs != null && rs.next()) {
	            Rental rental = mapRowToRental(rs);
	            rentals.add(rental);
	        }
	    } catch (SQLException e) {
	        e.printStackTrace();
	    } finally {

	    }
	    return rentals;
	}
	
	/**
	 * Finds a list of current rentals using a vehicles Identification number, 
	 * however the id should be in string format
	 * @param String vehicles identification
	 * @return List of vehicles that have been founds through an id
	 */
	public static List<Rental> findByVehicleID(String vehicleID) {
		List<Rental> rentals = new ArrayList<Rental>();
		String query = "SELECT * FROM rentals WHERE vehicle_id = ?";
		ResultSet result = MySQL.fetch(query, vehicleID);
		try {
			while(result.next()) {
				String id = result.getString("id");
			    String vec_id = vehicleID;
			    String customer_id = result.getString("customer_id");
			    LocalDateTime start_date = result.getTimestamp("start_date").toLocalDateTime();
			    LocalDateTime end_date = result.getTimestamp("end_date").toLocalDateTime();
			    double total_cost = result.getDouble("total_cost");
			    
			    Rental rental = new Rental();
			    rental.setRentalID(id);
			    rental.setVehicle(VehicleDAO.findByID(vec_id));
			    rental.setCustomer(CustomerDAO.findByID(customer_id));
			    rental.setPickupDate(start_date);
			    rental.setPlannedReturnDate(end_date);
			    rental.setTotalCost(total_cost);
			    rentals.add(rental);
			}
		} catch(SQLException e) {
			System.err.print("Rental doesent exist");
		}	
		return rentals;
	}
	
	/**
	 * Get a list of rentals by customer identification however the customer
	 * identification should be in string format
	 * @param String customerID
	 * @return
	 */
	public static List<Rental> findByCustomerID(String customerID) {
		List<Rental> rentals = new ArrayList<Rental>();
		String query = "SELECT * FROM rentals WHERE customer_id = ?";
		ResultSet result = MySQL.fetch(query, customerID);
		
		try {
			while(result.next()) {
				String id = result.getString("id");
			    String vec_id = result.getString("vehicle_id");
			    String customer_id = customerID;
			    LocalDateTime start_date = result.getTimestamp("start_date").toLocalDateTime();
			    LocalDateTime end_date = result.getTimestamp("end_date").toLocalDateTime();
			    double total_cost = result.getDouble("total_cost");
			    
			    Rental rental = new Rental();
			    rental.setRentalID(id);
			    rental.setVehicle(VehicleDAO.findByID(vec_id));
			    rental.setCustomer(CustomerDAO.findByID(customer_id));
			    rental.setPickupDate(start_date);
			    rental.setPlannedReturnDate(end_date);
			    rental.setTotalCost(total_cost);
			    rentals.add(rental);
			}	    
		  
		} catch(SQLException e) {
			System.err.print("Rental doesent exist");
		}
		return rentals;
	}
	
	/**
	 * Helper function to map the row that was obtained by MySQL to a Rental object
	 * @param rs
	 * @return
	 * @throws SQLException
	 */
	private Rental mapRowToRental(ResultSet rs) throws SQLException {
        Rental rental = new Rental();
        rental.setRentalID(rs.getString("id"));
        rental.setVehicle(VehicleDAO.findByID(rs.getString("vehicle_id")));
        rental.setCustomer(CustomerDAO.findByID(rs.getString("customer_id")));
        rental.setPickupDate(rs.getObject("start_date", LocalDateTime.class));
        rental.setPlannedReturnDate(rs.getObject("end_date", LocalDateTime.class));
        rental.setStatus(Rental.RentalStatus.valueOf(rs.getString("status")));
        return rental;
    }

	/**
	 * Finds overlapping rentals using the vehicle identification, start and end date of the rental
	 * @param String vehicle id
	 * @param LocalDateTime start date
	 * @param LocalDateTime end date
	 * @return A list of overlapping rentals
	 */
	public List<Rental> findOverlappingRentals(String vehicleIdStr, LocalDateTime proposedStart, LocalDateTime proposedEnd) {
	    List<Rental> overlapping = new ArrayList<>();
	    try {
	        int vehicleId = Integer.parseInt(vehicleIdStr);
	        // Correct overlap condition without status filter
	        String query = "SELECT * FROM rentals WHERE vehicle_id = ? AND start_date < ? AND end_date > ?";
	        ResultSet rs = MySQL.fetch(query, vehicleId, proposedEnd, proposedStart);
	        if (rs == null) return overlapping;
	        while (rs.next()) {
	            overlapping.add(mapRowToRental(rs));
	        }
	    } catch (NumberFormatException e) {
	        // handle invalid ID
	    } catch (SQLException e) {
	        e.printStackTrace();
	    } finally {
	        // Consider closing ResultSet (but MySQL.fetch returns an open one)
	    }
	    return overlapping;
	}	
	/**
	 * insert a record of a rental into the MySQL database
	 * @param rental to insert into the database
	 */
	public static void insertRecord(Rental rental) {
		String query = "INSERT INTO rentals(vehicle_id,customer_id,start_date, end_date, total_cost) "
					 + "VALUES(?,?,?,?,?);";
		MySQL.insert(query,rental.getVehicle().getId(), 
							rental.getCustomer().getCustomerID(),
							rental.getPickupDate(),
							rental.getPlannedReturnDate(),
							rental.getTotalCost());
	}

	
	
}
