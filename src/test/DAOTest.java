package test;

import main.java.com.carrental.dao.CustomerDAO;
import main.java.com.carrental.dao.MySQL;
import main.java.com.carrental.dao.RentalDAO;
import main.java.com.carrental.dao.VehicleDAO;
import main.java.com.carrental.model.Customer;
import main.java.com.carrental.model.Rental;
import main.java.com.carrental.model.Vehicle;
import main.java.com.carrental.util.SecurePasswordHasher;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests all DAO methods using the MySQL utility class.
 * Assumes the database schema is already created and the MySQL server is running.
 * Tables are cleared before each test to ensure isolation.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class DAOTest {

    @BeforeAll
    static void connectToDatabase() {
        MySQL mysql = new MySQL();
        mysql.Connect_to_MySQL_Database();
    }

    @BeforeEach
    void cleanTables() {
        MySQL.update("DELETE FROM rentals");
        MySQL.update("DELETE FROM vehicles");
        MySQL.update("DELETE FROM customers");
    }
    
    private void insertVehicle(String plate, String make, String model, int year, double rate, String type, String status) {
        String sql = String.format(
            "INSERT INTO vehicles (license_plate, make, model, year, daily_rate, vehicle_type, status) VALUES ('%s', '%s', '%s', %d, %.2f, '%s', '%s')",
            plate, make, model, year, rate, type, status
        );
        assertTrue(MySQL.insert(sql), "Failed to insert vehicle: " + plate);
    }

    private void insertCustomer(String email, String name, String phone, String license) {
        String sql = String.format(
            "INSERT INTO customers (name, email, password, phone, license_number) VALUES ('%s', '%s', 'hashed123', '%s', '%s')",
            name, email, phone, license
        );
        assertTrue(MySQL.insert(sql), "Failed to insert customer: " + email);
    }

    private void insertRental(String vehiclePlate, String customerEmail, 
        LocalDateTime start, LocalDateTime end, double cost) {
		int vehicleId = getVehicleIdByPlate(vehiclePlate);
		int customerId = getCustomerIdByEmail(customerEmail);
		String sql = "INSERT INTO rentals (vehicle_id, customer_id, start_date, end_date, total_cost) " +
		   "VALUES (?, ?, ?, ?, ?)";
		assertTrue(MySQL.insert(sql, vehicleId, customerId, 
		              java.sql.Timestamp.valueOf(start), 
		              java.sql.Timestamp.valueOf(end), 
		              cost), 
		 "Failed to insert rental");
    }

    private int getVehicleIdByPlate(String plate) {
        String sql = "SELECT id FROM vehicles WHERE license_plate = '" + plate + "'";
        try (ResultSet rs = MySQL.fetch(sql)) {
            if (rs.next()) return rs.getInt("id");
        } catch (SQLException e) {
            fail("Could not retrieve vehicle ID for plate: " + plate, e);
        }
        fail("Vehicle not found: " + plate);
        return -1;
    }

    private int getCustomerIdByEmail(String email) {
        String sql = "SELECT id FROM customers WHERE email = '" + email + "'";
        try (ResultSet rs = MySQL.fetch(sql)) {
            if (rs.next())  {
            	int id = rs.getInt("id");
            	return id;
            }
        } catch (SQLException e) {
            fail("Could not retrieve customer ID for email: " + email, e);
        }
        fail("Customer not found: " + email);
        return -1;
    }


    // ========== VehicleDAO Tests ==========

    @Test
    void testFindByType() {
        // Insert test vehicles
        insertVehicle("ASD-123", "Toyota", "Camry", 2022, 45.00, "SEDAN", "AVAILABLE");
        insertVehicle("GYT-789", "Honda", "Civic", 2023, 50.00, "SEDAN", "RENTED");
        insertVehicle("COP-456", "Ford", "F-150", 2021, 95.00, "TRUCK", "AVAILABLE");

        VehicleDAO vehicleDAO = new VehicleDAO(); // assume no-arg constructor
        List<Vehicle> sedans = vehicleDAO.findByType(Vehicle.VehicleType.SEDAN);
        assertEquals(2, sedans.size());

        List<Vehicle> trucks = vehicleDAO.findByType(Vehicle.VehicleType.TRUCK);
        assertEquals(1, trucks.size());
        assertEquals("F-150", trucks.get(0).getModel());
    }

    @Test
    void testFindAvailableVehicles() {
        insertVehicle("ABC-123", "Toyota", "Camry", 2022, 45.00, "SEDAN", "RENTED");
        insertVehicle("XYZ-789", "Honda", "Civic", 2023, 50.00, "SEDAN", "AVAILABLE");
        insertCustomer("alice@example.com", "Alice", "555-1234", "LIC123");
        insertRental("ABC-123", "alice@example.com",
                LocalDateTime.of(2025, 3, 1, 10, 0),
                LocalDateTime.of(2025, 3, 5, 10, 0),
                200.00);
        VehicleDAO vehicleDAO = new VehicleDAO();
        List<Vehicle> available = vehicleDAO.findAvailableVehicles();

        // Only ABC-123 should be available (XYZ-789 is rented during that period)
        assertEquals(1, available.size());
        assertEquals("XYZ-789", available.get(0).getLicensePlate());
    }

    @Test
    void testSaveVehicle() {
        VehicleDAO vehicleDAO = new VehicleDAO();

        // Create a new vehicle object (assumes appropriate constructor/setters)
        Vehicle newCar = new Vehicle(); // or Vehicle depending on your model
        newCar.setLicensePlate("SUV-001");
        newCar.setMake("Jeep");
        newCar.setModel("Wrangler");
        newCar.setYear(2024);
        newCar.setDailyRate(120.00);
        newCar.setType(Vehicle.VehicleType.SUV);
        newCar.setStatus(Vehicle.VehicleStatus.AVAILABLE);
        
        VehicleDAO.insertRecord(newCar);
        
        // Verify by fetching all SUVs
        List<Vehicle> suvs = vehicleDAO.findByType(Vehicle.VehicleType.SUV);
        assertEquals(1, suvs.size());
        assertEquals("Jeep", suvs.get(0).getMake());
    }

    @Test
    void testUpdateStatus() {
        insertVehicle("ABC-321", "Toyota", "Camry", 2022, 45.00, "SEDAN", "AVAILABLE");
        int id = getVehicleIdByPlate("ABC-321");

        VehicleDAO vehicleDAO = new VehicleDAO();
        boolean updated = vehicleDAO.updateStatus(Integer.toString(id), Vehicle.VehicleStatus.MAINTENANCE);
        assertTrue(updated);

        // Verify directly via SQL
        String sql = "SELECT status FROM vehicles WHERE id = " + id;
        try (ResultSet rs = MySQL.fetch(sql)) {
            assertTrue(rs.next());
            assertEquals("MAINTENANCE", rs.getString("status"));
        } catch (SQLException e) {
            fail("Failed to verify status update", e);
        }
    }

    // ========== CustomerDAO Tests ==========

    @Test
    void testFindCustomerByEmail() {
        insertCustomer("alice@example.com", "Alice", "555-1234", "LIC123");

        Customer customer = CustomerDAO.findByEmail("alice@example.com");
        assertTrue(customer!=null);
        assertEquals("Alice", customer.getCustomerName());
    }

    @Test
    void testSaveCustomer() {
        Customer newCust = new Customer();
        newCust.setCustomerName("Bob");
        newCust.setEmail("bob@example.com");
        newCust.setPassword(SecurePasswordHasher.hashPassword("hashed456"));
        newCust.setPhone("555-5678");
        newCust.setLicenseNumber("LIC456");
        
        CustomerDAO.insertRecord(newCust.getCustomerName(), newCust.getEmail(), newCust.getPassword());
        
        Customer found = CustomerDAO.findByEmail("bob@example.com");
        assertTrue(found!=null);
        assertEquals("Bob", found.getCustomerName());
    }

    // ========== RentalDAO Tests ==========

    @Test
    void testFindOverlappingRentals() {
        // Insert test data
        insertVehicle("XYZ-789", "Honda", "Civic", 2023, 50.00, "SEDAN", "AVAILABLE");
        insertCustomer("alice@example.com", "Alice", "555-1234", "LIC123");
        insertRental("XYZ-789", "alice@example.com",
                LocalDateTime.of(2025, 3, 1, 10, 0),
                LocalDateTime.of(2025, 3, 5, 10, 0),
                200.00);

        RentalDAO rentalDAO = new RentalDAO();
        String vehicleId = String.valueOf(getVehicleIdByPlate("XYZ-789"));
        LocalDateTime start = LocalDateTime.of(2025, 3, 2, 12, 0);
        LocalDateTime end   = LocalDateTime.of(2025, 3, 4, 12, 0);

        List<Rental> overlapping = rentalDAO.findOverlappingRentals(vehicleId, start, end);
        assertEquals(1, overlapping.size());
    }

    @Test
    void testFindOverlappingRentalsNoOverlap() {
        insertVehicle("XYZ-789", "Honda", "Civic", 2023, 50.00, "SEDAN", "AVAILABLE");
        insertCustomer("alice@example.com", "Alice", "555-1234", "LIC123");
        insertRental("XYZ-789", "alice@example.com",
                LocalDateTime.of(2025, 3, 1, 10, 0),
                LocalDateTime.of(2025, 3, 5, 10, 0),
                200.00);

        RentalDAO rentalDAO = new RentalDAO();
        LocalDateTime start = LocalDateTime.of(2025, 3, 6, 10, 0);
        LocalDateTime end = LocalDateTime.of(2025, 3, 7, 10, 0);

        List<Rental> overlapping = rentalDAO.findOverlappingRentals(
                String.valueOf(getVehicleIdByPlate("XYZ-789")), start, end);
        assertTrue(overlapping.isEmpty());
    }

    @Test
    void testSaveRental() {
        insertVehicle("ABC-123", "Toyota", "Camry", 2022, 45.00, "SEDAN", "AVAILABLE");
        insertCustomer("alice@example.com", "Alice", "555-1234", "LIC123");
        int vehicleId = getVehicleIdByPlate("ABC-123");
        int customerId = getCustomerIdByEmail("alice@example.com");

        Rental rental = new Rental();
        rental.setVehicle(VehicleDAO.findByID(Integer.toString(vehicleId)));
        rental.setCustomer(CustomerDAO.findByID(Integer.toString(customerId)));
        rental.setPickupDate(LocalDateTime.of(2025, 4, 1, 9, 0));
        rental.setPlannedReturnDate(LocalDateTime.of(2025, 4, 5, 9, 0));
        rental.setTotalCost(180.00);

        RentalDAO.insertRecord(rental);
        List<Rental> rentallist = RentalDAO.findByCustomerID(rental.getCustomer().getCustomerID());
        assertNotNull(rentallist);
        assertFalse(rentallist.isEmpty());

        // Verify via SQL
        String sql = "SELECT * FROM rentals WHERE vehicle_id = ? AND customer_id = ?";
        try (ResultSet rs = MySQL.fetch(sql,vehicleId,customerId)) {
            assertTrue(rs.next());
            assertEquals(180.00, rs.getDouble("total_cost"));
        } catch (SQLException e) {
            fail("Failed to verify rental", e);
        }
    }
}