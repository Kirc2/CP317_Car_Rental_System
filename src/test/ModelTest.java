package test;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import main.java.com.carrental.model.Customer;
import main.java.com.carrental.model.Payment;
import main.java.com.carrental.model.Rental;
import main.java.com.carrental.model.Vehicle;

import java.time.LocalDate;
import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for model classes (Customer, Vehicle, Rental, Payment).
 */
public class ModelTest {

    // ------------------------------------------------------------------------
    // Customer Tests
    // ------------------------------------------------------------------------
    @Nested
    class CustomerTest {

        @Test
        void testDefaultConstructor() {
            Customer customer = new Customer();
            assertNotNull(customer);
        }

        @Test
        void testParameterizedConstructor_withoutOptionalFields() {
            String id = "C001";
            String name = "John Doe";
            String email = "john@example.com";
            String password = "secret";
            Customer customer = new Customer(id, name, email, password);

            assertEquals(id, customer.getCustomerID());
            assertEquals(name, customer.getCustomerName());
            assertEquals(email, customer.getEmail());
            assertEquals(password, customer.getPassword());
            assertNull(customer.getPhone());
            assertNull(customer.getLicenseNumber());
            assertNull(customer.getLicenseExpiry());
            assertNull(customer.getType());
        }

        @Test
        void testParameterizedConstructor_withAllFields() {
            String id = "C002";
            String name = "Jane Doe";
            String email = "jane@example.com";
            String phone = "123-456-7890";
            String licenseNumber = "LIC123";
            LocalDate expiry = LocalDate.of(2025, 12, 31);
            Customer customer = new Customer(id, name, email, phone, licenseNumber, expiry);

            assertEquals(id, customer.getCustomerID());
            assertEquals(name, customer.getCustomerName());
            assertEquals(email, customer.getEmail());
            assertEquals(phone, customer.getPhone());
            assertEquals(licenseNumber, customer.getLicenseNumber());
            assertEquals(expiry, customer.getLicenseExpiry());
            assertNull(customer.getPassword()); // password not set in this constructor
        }

        @Test
        void testSettersAndGetters() {
            Customer customer = new Customer();
            customer.setCustomerID("C003");
            customer.setCustomerName("Alice");
            customer.setEmail("alice@test.com");
            customer.setPassword("pwd");
            customer.setPhone("987-654-3210");
            customer.setLicenseNumber("LIC456");
            LocalDate expiry = LocalDate.now().plusYears(1);
            customer.setLicenseExpiry(expiry);
            customer.setType(Customer.CustomerType.VIP);

            assertEquals("C003", customer.getCustomerID());
            assertEquals("Alice", customer.getCustomerName());
            assertEquals("alice@test.com", customer.getEmail());
            assertEquals("pwd", customer.getPassword());
            assertEquals("987-654-3210", customer.getPhone());
            assertEquals("LIC456", customer.getLicenseNumber());
            assertEquals(expiry, customer.getLicenseExpiry());
            assertEquals(Customer.CustomerType.VIP, customer.getType());
        }

        @Test
        void testCustomerTypeEnum() {
            Customer.CustomerType[] types = Customer.CustomerType.values();
            assertEquals(3, types.length);
            assertTrue(types[0] == Customer.CustomerType.REGULAR);
            assertTrue(types[1] == Customer.CustomerType.CORPERATE);
            assertTrue(types[2] == Customer.CustomerType.VIP);
        }
    }

    // ------------------------------------------------------------------------
    // Vehicle Tests
    // ------------------------------------------------------------------------
    @Nested
    class VehicleTest {

        @Test
        void testDefaultConstructor() {
            Vehicle vehicle = new Vehicle();
            assertNotNull(vehicle);
        }

        @Test
        void testParameterizedConstructor() {
            String id = "V001";
            String plate = "ABC-123";
            String make = "Toyota";
            String model = "Camry";
            int year = 2020;
            double rate = 45.0;
            Vehicle.VehicleType type = Vehicle.VehicleType.SEDAN;

            Vehicle vehicle = new Vehicle(id, plate, make, model, year, rate, type);

            assertEquals(id, vehicle.getId());
            assertEquals(plate, vehicle.getLicensePlate());
            assertEquals(make, vehicle.getMake());
            assertEquals(model, vehicle.getModel());
            assertEquals(year, vehicle.getYear());
            assertEquals(rate, vehicle.getDailyRate());
            assertEquals(type, vehicle.getType());
            assertEquals(Vehicle.VehicleStatus.AVAILABLE, vehicle.getStatus());
        }

        @Test
        void testSettersAndGetters() {
            Vehicle vehicle = new Vehicle();
            vehicle.setId("V002");
            vehicle.setLicensePlate("XYZ-789");
            vehicle.setMake("Honda");
            vehicle.setModel("Civic");
            vehicle.setYear(2021);
            vehicle.setDailyRate(55.0);
            vehicle.setStatus(Vehicle.VehicleStatus.RENTED);
            vehicle.setType(Vehicle.VehicleType.SUV);

            assertEquals("V002", vehicle.getId());
            assertEquals("XYZ-789", vehicle.getLicensePlate());
            assertEquals("Honda", vehicle.getMake());
            assertEquals("Civic", vehicle.getModel());
            assertEquals(2021, vehicle.getYear());
            assertEquals(55.0, vehicle.getDailyRate());
            assertEquals(Vehicle.VehicleStatus.RENTED, vehicle.getStatus());
            assertEquals(Vehicle.VehicleType.SUV, vehicle.getType());
        }

        @Test
        void testVehicleTypeEnum() {
            Vehicle.VehicleType[] types = Vehicle.VehicleType.values();
            assertEquals(4, types.length);
            assertTrue(types[0] == Vehicle.VehicleType.SEDAN);
            assertTrue(types[1] == Vehicle.VehicleType.SUV);
            assertTrue(types[2] == Vehicle.VehicleType.TRUCK);
            assertTrue(types[3] == Vehicle.VehicleType.CAR);
        }

        @Test
        void testVehicleStatusEnum() {
            Vehicle.VehicleStatus[] statuses = Vehicle.VehicleStatus.values();
            assertEquals(3, statuses.length);
            assertTrue(statuses[0] == Vehicle.VehicleStatus.AVAILABLE);
            assertTrue(statuses[1] == Vehicle.VehicleStatus.RENTED);
            assertTrue(statuses[2] == Vehicle.VehicleStatus.MAINTENANCE);
        }

        @Test
        void testGetTypeFromString_fixed() {
            Vehicle vehicle = new Vehicle();
            assertEquals(Vehicle.VehicleType.SEDAN, vehicle.getTypeFromString("SEDAN"));
            assertEquals(Vehicle.VehicleType.SUV, vehicle.getTypeFromString("SUV"));
            assertEquals(Vehicle.VehicleType.TRUCK, vehicle.getTypeFromString("TRUCK"));
            assertEquals(Vehicle.VehicleType.CAR, vehicle.getTypeFromString("CAR"));
        }
        
    }

    // ------------------------------------------------------------------------
    // Rental Tests
    // ------------------------------------------------------------------------
    @Nested
    class RentalTest {

        @Test
        void testSettersAndGetters() {
            Rental rental = new Rental();

            String rentalId = "R001";
            Customer customer = new Customer();
            customer.setCustomerID("C001");
            Vehicle vehicle = new Vehicle();
            vehicle.setId("V001");
            LocalDateTime pickup = LocalDateTime.of(2023, 6, 1, 10, 0);
            LocalDateTime plannedReturn = LocalDateTime.of(2023, 6, 5, 10, 0);
            LocalDateTime actualReturn = LocalDateTime.of(2023, 6, 5, 9, 30);
            double totalCost = 200.0;
            Rental.RentalStatus status = Rental.RentalStatus.ACTIVE;
            Payment payment = new Payment();

            rental.setRentalID(rentalId);
            rental.setCustomer(customer);
            rental.setVehicle(vehicle);
            rental.setPickupDate(pickup);
            rental.setPlannedReturnDate(plannedReturn);
            rental.setActualReturnDate(actualReturn);
            rental.setTotalCost(totalCost);
            rental.setStatus(status);
            rental.setPayment(payment);

            assertEquals(rentalId, rental.getRentalID());
            assertEquals(customer, rental.getCustomer());
            assertEquals(vehicle, rental.getVehicle());
            assertEquals(pickup, rental.getPickupDate());
            assertEquals(plannedReturn, rental.getPlannedReturnDate());
            assertEquals(actualReturn, rental.getActualReturnDate());
            assertEquals(totalCost, rental.getTotalCost());
            assertEquals(status, rental.getStatus());
            assertEquals(payment, rental.getPayment());
        }

        @Test
        void testRentalStatusEnum() {
            Rental.RentalStatus[] statuses = Rental.RentalStatus.values();
            assertEquals(4, statuses.length);
            assertTrue(statuses[0] == Rental.RentalStatus.RESERVED);
            assertTrue(statuses[1] == Rental.RentalStatus.ACTIVE);
            assertTrue(statuses[2] == Rental.RentalStatus.COMPLETED);
            assertTrue(statuses[3] == Rental.RentalStatus.CANCELLED);
        }
    }

    // ------------------------------------------------------------------------
    // Payment Tests
    // ------------------------------------------------------------------------
    @Nested
    class PaymentTest {

        @Test
        void testInstantiation() {
            Payment payment = new Payment();
            assertNotNull(payment);
            // Since Payment is empty, just verify it exists.
        }
    }
}
