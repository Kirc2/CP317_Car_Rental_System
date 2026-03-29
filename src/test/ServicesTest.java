package test;

import main.java.com.carrental.model.*;
import main.java.com.carrental.service.*;
import main.java.com.carrental.dao.*;
import org.junit.jupiter.api.*;

import org.junit.jupiter.api.Test;

import java.sql.*;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for the service layer.
 * Uses the real MySQL database (assumed running) and cleans tables before each test.
 */
public class ServicesTest {

	
	
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

    // ----- Helper methods (copied from DAOTest) -----
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
                              LocalDate start, LocalDate end, double cost) {
        int vehicleId = getVehicleIdByPlate(vehiclePlate);
        int customerId = getCustomerIdByEmail(customerEmail);
        String sql = "INSERT INTO rentals (vehicle_id, customer_id, start_date, end_date, total_cost) " +
                     "VALUES (?, ?, ?, ?, ?)";
        assertTrue(MySQL.insert(sql, vehicleId, customerId,
                      java.sql.Timestamp.valueOf(start.atStartOfDay()),
                      java.sql.Timestamp.valueOf(end.atStartOfDay()),
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
            if (rs.next()) return rs.getInt("id");
        } catch (SQLException e) {
            fail("Could not retrieve customer ID for email: " + email, e);
        }
        fail("Customer not found: " + email);
        return -1;
    }

    // ========== CustomerService Tests ==========
    @Nested
    class CustomerServiceTest {
        @Test
        void testRegisterAndLogin() {
            CustomerService service = new CustomerService();

            // Register a new customer
            Customer registered = service.Register("test@example.com", "secret123", "TestUser");
            assertNotNull(registered);
            assertEquals("test@example.com", registered.getEmail());
            assertEquals("TestUser", registered.getCustomerName());
            // Password should be hashed
            assertNotEquals("secret123", registered.getPassword());

            // Login with correct credentials
            Customer loggedIn = service.Login("test@example.com", "secret123");
            assertNotNull(loggedIn);
            assertEquals("TestUser", loggedIn.getCustomerName());

            // Login with wrong password
            Customer failed = service.Login("test@example.com", "wrong");
            assertNull(failed);
        }

        @Test
        void testLoginNonExistentEmail() {
            CustomerService service = new CustomerService();
            Customer result = service.Login("nonexistent@example.com", "any");
            assertNull(result);
        }
    }

    // ========== VehicleService Tests ==========
    @Nested
    class VehicleServiceTest {
        @Test
        void testGetVehiclesFromFilters() {
            // Insert some vehicles
            insertVehicle("ABC-123", "Toyota", "Camry", 2022, 45.00, "SEDAN", "AVAILABLE");
            insertVehicle("XYZ-789", "Honda", "CRV", 2023, 60.00, "SUV", "AVAILABLE");
            insertVehicle("DEF-456", "Ford", "F-150", 2021, 95.00, "TRUCK", "RENTED"); // not available

            VehicleService service = new VehicleService();

            // Filter by type = SUV
            String jsonFilters = "{\"carType\":\"SUV\"}";
            List<Vehicle> suvs = service.getVehiclesFromFilters(jsonFilters);
            assertEquals(1, suvs.size());
            assertEquals("Honda", suvs.get(0).getMake());

            // Filter by price limit
            jsonFilters = "{\"pricelimit\":\"50\"}";
            List<Vehicle> cheap = service.getVehiclesFromFilters(jsonFilters);
            assertEquals(1, cheap.size()); // only Toyota (45) should match, Honda (60) is above
            assertEquals("Toyota", cheap.get(0).getMake());

            // Filter by year and type
            jsonFilters = "{\"carType\":\"SEDAN\",\"year\":\"2022\"}";
            List<Vehicle> sedan2022 = service.getVehiclesFromFilters(jsonFilters);
            assertEquals(1, sedan2022.size());
            assertEquals("Toyota", sedan2022.get(0).getMake());

            // Filter with date availability (no overlapping rentals)
            // Insert a rental for the Honda CRV
            insertCustomer("alice@example.com", "Alice", "555-1234", "LIC123");
            insertRental("XYZ-789", "alice@example.com",
                         LocalDate.of(2025, 6, 10), LocalDate.of(2025, 6, 15), 300.00);

            jsonFilters = "{\"carType\":\"SUV\",\"startDate\":\"2025-06-12\",\"endDate\":\"2025-06-14\"}";
            List<Vehicle> available = service.getVehiclesFromFilters(jsonFilters);
            // The SUV is rented during that period, so should not appear
            assertEquals(0, available.size());

            jsonFilters = "{\"carType\":\"SUV\",\"startDate\":\"2025-06-16\",\"endDate\":\"2025-06-18\"}";
            available = service.getVehiclesFromFilters(jsonFilters);
            // Now the rental period is over, should appear
            assertEquals(1, available.size());
            assertEquals("Honda", available.get(0).getMake());
        }
    }

    // ========== RentalService Tests ==========
    @Nested
    class RentalServiceTest {
        @Test
        void testReserveVehicle() {
            // Prepare data
            insertVehicle("CAR-001", "Toyota", "Camry", 2022, 45.00, "SEDAN", "AVAILABLE");
            insertCustomer("customer@example.com", "John Doe", "555-1234", "LIC123");
            int vehicleId = getVehicleIdByPlate("CAR-001");
            int customerId = getCustomerIdByEmail("customer@example.com");

            // Create a Rental object
            Rental rental = new Rental();
            rental.setVehicle(VehicleDAO.findByID(String.valueOf(vehicleId)));
            rental.setCustomer(CustomerDAO.findByID(String.valueOf(customerId)));
            rental.setPickupDate(LocalDate.of(2025, 7, 1));
            rental.setPlannedReturnDate(LocalDate.of(2025, 7, 5));
            rental.setTotalCost(180.00);
            rental.setStatus(Rental.RentalStatus.RESERVED);

            RentalService service = new RentalService();
            boolean success = service.reserveVehicle(rental);
            assertTrue(success);

            // Verify rental was inserted
            RentalDAO rentalDAO = new RentalDAO();
            List<Rental> rentals = rentalDAO.findByCustomerID(String.valueOf(customerId));
            assertEquals(1, rentals.size());
            assertEquals(LocalDate.of(2025, 7, 1), rentals.get(0).getPickupDate());
        }

        @Test
        void testGetRentalFromJson() {
            // Insert a vehicle and set PersistentData
            insertVehicle("CAR-002", "Honda", "Civic", 2023, 50.00, "SEDAN", "AVAILABLE");
            int vehicleId = getVehicleIdByPlate("CAR-002");
            String json = "{\"id\":\"" + vehicleId + "\",\"startDate\":\"2025-08-10\",\"endDate\":\"2025-08-15\"}";

            // Set a dummy customer in PersistentData
            Customer dummy = new Customer();
            dummy.setCustomerID("1");
            PersistentData.setCustomer(dummy);

            RentalService service = new RentalService();
            Rental rental = service.getRentalFromJson(json);
            assertNotNull(rental);
            assertEquals(rental.getVehicle().getId(), String.valueOf(vehicleId));
            assertEquals(LocalDate.of(2025, 8, 10), rental.getPickupDate());
            assertEquals(LocalDate.of(2025, 8, 15), rental.getPlannedReturnDate());
            assertEquals(5 * 50.00, rental.getTotalCost(), 0.01);
            assertEquals(Rental.RentalStatus.RESERVED, rental.getStatus());
        }
    }

    // ========== PaymentService Tests ==========
    @Nested
    class PaymentServiceTest {
        @Test
        void testProcessPayment_Success() {
            // Create a rental
            insertVehicle("CAR-003", "Ford", "Focus", 2021, 40.00, "SEDAN", "AVAILABLE");
            insertCustomer("pay@example.com", "PayUser", "555-0000", "LIC999");
            int vehicleId = getVehicleIdByPlate("CAR-003");
            int customerId = getCustomerIdByEmail("pay@example.com");

            Rental rental = new Rental();
            rental.setVehicle(VehicleDAO.findByID(String.valueOf(vehicleId)));
            rental.setCustomer(CustomerDAO.findByID(String.valueOf(customerId)));
            rental.setPickupDate(LocalDate.of(2025, 9, 1));
            rental.setPlannedReturnDate(LocalDate.of(2025, 9, 3));
            rental.setTotalCost(80.00);
            RentalDAO.insertRecord(rental);

            // Get the rental ID
            int rentalId = getRentalIdForCustomer(customerId);

            PaymentService service = new PaymentService();
            PaymentService.PaymentResult result = service.processPayment(rentalId, "4242424242424242");
            assertTrue(result.isSuccess());
            assertNotNull(result.getPaymentId());
            assertEquals("Payment successful", result.getMessage());

            // Check that payment record exists
            Payment payment = service.getPaymentStatus(result.getPaymentId());
            assertNotNull(payment);
            assertEquals(Payment.PaymentStatus.COMPLETED, payment.getStatus());
            assertNotNull(payment.getTransactionId());
        }

        @Test
        void testProcessPayment_Failure() {
            // Create a rental
            insertVehicle("CAR-004", "Chevy", "Spark", 2020, 35.00, "CAR", "AVAILABLE");
            insertCustomer("fail@example.com", "FailUser", "555-1111", "LIC888");
            int vehicleId = getVehicleIdByPlate("CAR-004");
            int customerId = getCustomerIdByEmail("fail@example.com");

            Rental rental = new Rental();
            rental.setVehicle(VehicleDAO.findByID(String.valueOf(vehicleId)));
            rental.setCustomer(CustomerDAO.findByID(String.valueOf(customerId)));
            rental.setPickupDate(LocalDate.of(2025, 10, 1));
            rental.setPlannedReturnDate(LocalDate.of(2025, 10, 2));
            rental.setTotalCost(35.00);
            RentalDAO.insertRecord(rental);

            int rentalId = getRentalIdForCustomer(customerId);

            PaymentService service = new PaymentService();
            PaymentService.PaymentResult result = service.processPayment(rentalId, "4000000000000002"); // fails
            assertFalse(result.isSuccess());
            assertEquals("Payment declined by gateway", result.getMessage());

            // Verify payment record is marked as FAILED
            Payment payment = service.getPaymentStatus(result.getPaymentId());
            assertNotNull(payment);
            assertEquals(Payment.PaymentStatus.FAILED, payment.getStatus());
        }

        // Helper to get the most recent rental ID for a customer (simplified)
        private int getRentalIdForCustomer(int customerId) {
            String sql = "SELECT id FROM rentals WHERE customer_id = " + customerId + " ORDER BY id DESC LIMIT 1";
            try (ResultSet rs = MySQL.fetch(sql)) {
                if (rs.next()) return rs.getInt("id");
            } catch (SQLException e) {
                fail("Could not fetch rental ID");
            }
            return -1;
        }
    }

    // ========== ReportService Tests ==========
    @Nested
    class ReportServiceTest {
        @Test
        void testDailyReport() {
            // Insert data for a specific day
            insertVehicle("RPT-001", "Toyota", "Camry", 2022, 45.00, "SEDAN", "AVAILABLE");
            insertCustomer("rpt@example.com", "ReportUser", "555-2222", "LIC777");
            insertRental("RPT-001", "rpt@example.com",
                         LocalDate.of(2025, 5, 15), LocalDate.of(2025, 5, 17), 90.00);
            insertRental("RPT-001", "rpt@example.com",
                         LocalDate.of(2025, 5, 15), LocalDate.of(2025, 5, 16), 45.00);

            ReportService service = new ReportService();
            String json = service.getDailyReportJson("2025-05-15");
            // Expected: rentalCount=2, totalRevenue=135.00
            assertTrue(json.contains("\"rentalCount\":2"));
            assertTrue(json.contains("\"totalRevenue\":135.0") || json.contains("\"totalRevenue\":135.00"));
        }

        @Test
        void testPopularCars() {
            insertVehicle("POP-1", "Toyota", "Camry", 2022, 45.00, "SEDAN", "AVAILABLE");
            insertVehicle("POP-2", "Honda", "CRV", 2023, 60.00, "SUV", "AVAILABLE");
            insertVehicle("POP-3", "Ford", "F-150", 2021, 95.00, "TRUCK", "AVAILABLE");
            insertCustomer("pop1@example.com", "User1", "555-1111", "LIC111");
            insertCustomer("pop2@example.com", "User2", "555-2222", "LIC222");

            // Rentals: SEDAN 2 times, SUV 1 time, TRUCK 0 times
            insertRental("POP-1", "pop1@example.com", LocalDate.of(2025,6,1), LocalDate.of(2025,6,3), 90.00);
            insertRental("POP-1", "pop2@example.com", LocalDate.of(2025,6,5), LocalDate.of(2025,6,6), 45.00);
            insertRental("POP-2", "pop1@example.com", LocalDate.of(2025,6,2), LocalDate.of(2025,6,4), 120.00);

            ReportService service = new ReportService();
            String json = service.getPopularCarsJson();
            // Expect order: SEDAN (2), SUV (1), TRUCK (0)
            // We can check that the first entry is SEDAN with count 2
            // Simplest: verify that the JSON contains the counts
            assertTrue(json.contains("\"type\":\"SEDAN\",\"count\":2"));
            assertTrue(json.contains("\"type\":\"SUV\",\"count\":1"));
            assertTrue(json.contains("\"type\":\"TRUCK\",\"count\":0"));
        }

        @Test
        void testMonthlyRevenue() {
            insertVehicle("MON-1", "Toyota", "Camry", 2022, 45.00, "SEDAN", "AVAILABLE");
            insertCustomer("mon@example.com", "MonUser", "555-3333", "LIC333");
            insertRental("MON-1", "mon@example.com", LocalDate.of(2025, 7, 10), LocalDate.of(2025, 7, 12), 90.00);
            insertRental("MON-1", "mon@example.com", LocalDate.of(2025, 7, 15), LocalDate.of(2025, 7, 16), 45.00);
            // Another rental in a different month
            insertRental("MON-1", "mon@example.com", LocalDate.of(2025, 8, 1), LocalDate.of(2025, 8, 3), 90.00);

            ReportService service = new ReportService();
            String json = service.getMonthlyRevenueJson(2025, 7);
            assertTrue(json.contains("\"totalRevenue\":135.0") || json.contains("\"totalRevenue\":135.00"));
        }
    }
}