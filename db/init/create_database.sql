
use Car_Rental_System;

CREATE TABLE vehicles (
	id INT AUTO_INCREMENT PRIMARY KEY,
	license_plate VARCHAR(20) UNIQUE NOT NULL,
	make VARCHAR(225),
	model VARCHAR(255),
	year INT,
	daily_rate DECIMAL(10,2),
	vehicle_type enum('SEDAN', 'SUV', 'TRUCK', 'CAR'),
	car_status enum('AVAILABLE', 'RENTED', 'MAINTENANCE') DEFAULT 'AVAILABLE'
);

CREATE TABLE customers(
	id INT AUTO_INCREMENT PRIMARY KEY,
	name VARCHAR(100) NOT NULL,
	email VARCHAR(100) UNIQUE NOT NULL,
	password VARCHAR(255) NOT NULL,
	phone VARCHAR(20),
	license_number VARCHAR(50)
);

CREATE TABLE rentals(
	id INT AUTO_INCREMENT PRIMARY KEY UNIQUE,
    vehicle_id INT,
    customer_id INT,
    start_date DATETIME,
    end_date DATETIME,
    total_cost DECIMAL(10,2),
    rental_status enum('RESERVED', 'ACTIVE', 'COMPLETED', 'CANCELLED'),
    FOREIGN KEY(vehicle_id) REFERENCES vehicles(id),
    FOREIGN KEY(customer_id) REFERENCES customers(id)
);

CREATE TABLE payments (
    id INT AUTO_INCREMENT PRIMARY KEY,
    rental_id INT NOT NULL,
    amount DECIMAL(10,2) NOT NULL,
    status ENUM('PENDING', 'COMPLETED', 'FAILED', 'REFUNDED') DEFAULT 'PENDING',
    transaction_id VARCHAR(100),
    payment_date DATETIME,
    FOREIGN KEY (rental_id) REFERENCES rentals(id)
);

-- TEST DATA
-- Insert 10 vehicles
INSERT INTO vehicles (license_plate, make, model, year, daily_rate, vehicle_type, car_status) VALUES
('ABC-123', 'Toyota', 'Camry', 2026, 45.00, 'SEDAN', 'AVAILABLE'),
('DEF-456', 'Honda', 'Civic', 2023, 40.00, 'SEDAN', 'AVAILABLE'),
('GHI-789', 'Ford', 'Explorer', 2025, 70.00, 'SUV', 'AVAILABLE'),
('JKL-012', 'Jeep', 'Wrangler', 2026, 80.00, 'SUV', 'AVAILABLE'),
('MNO-345', 'Ford', 'F-150', 2026, 90.00, 'TRUCK', 'AVAILABLE'),
('PQR-678', 'Ram', '1500', 2026, 95.00, 'TRUCK', 'AVAILABLE'),
('STU-901', 'Chevrolet', 'Malibu', 2026, 42.00, 'SEDAN', 'AVAILABLE'),
('VWX-234', 'Toyota', 'RAV4', 2022, 65.00, 'SUV', 'AVAILABLE'),
('YZA-567', 'Nissan', 'Versa', 2020, 35.00, 'CAR', 'AVAILABLE'),
('BCD-890', 'Hyundai', 'Elantra', 2023, 38.00, 'SEDAN', 'AVAILABLE');

-- Insert 10 customers, password is '123'
INSERT INTO customers (name, email, password, phone, license_number) VALUES
('John Doe', 'john.doe@example.com', 'yoUhCPbeAcvMoZ1cxFcIXQ==:C+K5mU406fAh/2tHKvp0AwvOZqbQec8RZp1YXicE9rajtISPg9z2dd08DQUGTzFSxXyvEKYROA9ZzmmuYSj4zQ==:66536:512', '555-123-4567', 'LIC001'),
('Jane Smith', 'jane.smith@example.com', 'yoUhCPbeAcvMoZ1cxFcIXQ==:C+K5mU406fAh/2tHKvp0AwvOZqbQec8RZp1YXicE9rajtISPg9z2dd08DQUGTzFSxXyvEKYROA9ZzmmuYSj4zQ==:66536:512', '555-234-5678', 'LIC002'),
('Robert Johnson', 'robert.j@example.com', 'yoUhCPbeAcvMoZ1cxFcIXQ==:C+K5mU406fAh/2tHKvp0AwvOZqbQec8RZp1YXicE9rajtISPg9z2dd08DQUGTzFSxXyvEKYROA9ZzmmuYSj4zQ==:66536:512', '555-345-6789', 'LIC003'),
('Emily Davis', 'emily.davis@example.com', '$2a$10$HashedPassword4', '555-456-7890', 'LIC004'),
('Michael Brown', 'michael.brown@example.com', '$2a$10$HashedPassword5', '555-567-8901', 'LIC005'),
('Sarah Wilson', 'sarah.wilson@example.com', '$2a$10$HashedPassword6', '555-678-9012', 'LIC006'),
('David Martinez', 'david.martinez@example.com', '$2a$10$HashedPassword7', '555-789-0123', 'LIC007'),
('Laura Garcia', 'laura.garcia@example.com', '$2a$10$HashedPassword8', '555-890-1234', 'LIC008'),
('James Anderson', 'james.anderson@example.com', '$2a$10$HashedPassword9', '555-901-2345', 'LIC009'),
('Linda Thomas', 'linda.thomas@example.com', '$2a$10$HashedPassword10', '555-012-3456', 'LIC010');

-- Insert 10 rentals (assumes vehicle_id and customer_id values 1-10 exist from previous inserts)
INSERT INTO rentals (vehicle_id, customer_id, start_date, end_date, total_cost) VALUES
(1, 3, '2026-01-10 10:00:00', '2026-01-15 10:00:00', 225.00),   -- 5 days * 45
(2, 5, '2026-02-03 12:00:00', '2026-02-07 12:00:00', 160.00),   -- 4 days * 40
(3, 1, '2026-03-01 09:00:00', '2026-03-05 09:00:00', 350.00),   -- 5 days * 70
(4, 2, '2026-01-20 14:00:00', '2026-01-25 14:00:00', 400.00),   -- 5 days * 80
(5, 4, '2026-02-10 08:00:00', '2026-02-12 08:00:00', 180.00),   -- 2 days * 90
(6, 6, '2026-03-15 11:00:00', '2026-03-20 11:00:00', 475.00),   -- 5 days * 95
(7, 7, '2026-04-01 13:00:00', '2026-04-04 13:00:00', 126.00),   -- 3 days * 42
(8, 8, '2026-04-10 16:00:00', '2026-04-14 16:00:00', 260.00),   -- 4 days * 65
(9, 9, '2026-05-05 10:00:00', '2026-05-06 10:00:00', 35.00),    -- 1 day * 35
(10, 10, '2026-05-20 09:30:00', '2026-05-23 09:30:00', 114.00); -- 3 days * 38
