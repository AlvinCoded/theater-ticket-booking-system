-- Create database
CREATE DATABASE musical_tickets;
USE musical_tickets;

-- Staff table for admin and staff users
CREATE TABLE staff (
    id INT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    role ENUM('ADMIN', 'STAFF') NOT NULL,
    permissions VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Customers table for registered customers
CREATE TABLE customers (
    id INT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(100) NOT NULL,
    phone_number VARCHAR(20) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Venues table
CREATE TABLE venues (
    id INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(255) NOT NULL,
    total_capacity INT NOT NULL,
    section_count INT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Sections table for venue sections
CREATE TABLE sections (
    id INT PRIMARY KEY AUTO_INCREMENT,
    venue_id INT,
    name VARCHAR(50) NOT NULL,
    capacity INT NOT NULL,
    base_price DECIMAL(10,2) NOT NULL,
    FOREIGN KEY (venue_id) REFERENCES venues(id) ON DELETE CASCADE
);

-- Musicals table
CREATE TABLE musicals (
    id INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(255) NOT NULL,
    run_time VARCHAR(50),
    categories VARCHAR(255),
    age_restriction VARCHAR(50),
    price DECIMAL(10,2),
    available_tickets INT,
    available_days VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Showtimes for musicals 
CREATE TABLE show_times (
    id INT PRIMARY KEY AUTO_INCREMENT,
    musical_id INT,
    show_time TIME NOT NULL,
    FOREIGN KEY (musical_id) REFERENCES musicals(id) ON DELETE CASCADE
);

-- Musical venues linking table
CREATE TABLE musical_venues (
    musical_id INT,
    venue_id INT,
    price_multiplier DECIMAL(4,2) DEFAULT 1.00,
    PRIMARY KEY (musical_id, venue_id),
    FOREIGN KEY (musical_id) REFERENCES musicals(id) ON DELETE CASCADE,
    FOREIGN KEY (venue_id) REFERENCES venues(id) ON DELETE CASCADE
);

-- Receipts table for booking records
CREATE TABLE receipts (
    id INT PRIMARY KEY AUTO_INCREMENT,
    customer_id INT NOT NULL,
    musical_id INT NOT NULL,
    total_price DECIMAL(10,2) NOT NULL,
    show_date DATE NOT NULL,
    show_time VARCHAR(25) NOT NULL,
    receipt_text TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (customer_id) REFERENCES customers(id),
    FOREIGN KEY (musical_id) REFERENCES musicals(id)
);

-- Income data table for financial tracking
CREATE TABLE income_data (
    id INT PRIMARY KEY AUTO_INCREMENT,
    amount DECIMAL(10,2) NOT NULL,
    transaction_date DATE NOT NULL,
    category VARCHAR(50),
    description VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Seat booking table
CREATE TABLE booked_seats (
    id INT PRIMARY KEY AUTO_INCREMENT,
    musical_id INT,
    venue_id INT,
    section_id INT,
    seat_number VARCHAR(25),
    show_date DATE,
    show_time VARCHAR(25),
    booking_id INT,
    FOREIGN KEY (musical_id) REFERENCES musicals(id),
    FOREIGN KEY (venue_id) REFERENCES venues(id),
    FOREIGN KEY (section_id) REFERENCES sections(id),
    UNIQUE KEY unique_seat (musical_id, venue_id, section_id, seat_number, show_date, show_time)
);


-- Demo data for venues
INSERT INTO venues (id, name, total_capacity, section_count) VALUES
(1, 'Royal Theatre', 1000, 3),
(2, 'Broadway Hall', 800, 2),
(3, 'Starlight Arena', 1500, 4),
(4, 'Denture', 100, 1);

-- Demo data for sections
INSERT INTO sections (id, venue_id, name, capacity, base_price) VALUES
(1, 1, 'Orchestra', 400, 100.00),
(2, 1, 'Mezzanine', 300, 80.00),
(3, 1, 'Balcony', 300, 60.00),
(4, 2, 'Main Floor', 500, 90.00),
(5, 2, 'Upper Level', 300, 70.00),
(6, 3, 'VIP', 100, 150.00),
(7, 3, 'Lower Bowl', 600, 100.00),
(8, 3, 'Upper Bowl', 500, 75.00),
(9, 3, 'Standing Room', 300, 50.00),
(10, 4, 'General Admission', 100, 50.00);

-- Demo data for musicals
INSERT INTO musicals (id, name, run_time, categories, age_restriction, price, available_tickets, available_days) VALUES
(1, 'The Phantom of the Opera', '2h 30min', 'Drama,Romance', '12+', 79.99, 200, 'Monday,Wednesday,Friday,Saturday'),
(2, 'Les Misérables', '2h 50min', 'Drama,Musical', 'All ages', 69.99, 138, 'Tuesday,Thursday,Saturday,Sunday'),
(3, 'The Lion King', '2h 30min', 'Family,Musical', 'All ages', 89.99, 200, 'Wednesday,Friday,Saturday,Sunday'),
(4, 'Wicked', '2h 45min', 'Fantasy,Musical', '8+', 75.99, 156, 'Monday,Tuesday,Friday,Sunday'),
(5, 'Hamilton', '2h 40min', 'Historical,Musical', '12+', 99.99, 250, 'Tuesday,Wednesday,Saturday,Sunday');

-- Demo data for musical_venues
INSERT INTO musical_venues (musical_id, venue_id, price_multiplier) VALUES
(1, 1, 1.2),
(2, 2, 1.1),
(3, 3, 1.3),
(4, 1, 1.1),
(5, 2, 1.2);

-- Demo data for show_times
INSERT INTO show_times (musical_id, show_time) VALUES
(1, '19:30:00'),
(1, '14:00:00'),
(2, '20:00:00'),
(3, '18:30:00'),
(4, '19:00:00'),
(5, '20:30:00');

-- Demo data for booked_seats
INSERT INTO booked_seats (musical_id, venue_id, section_id, seat_number, show_date, show_time, booking_id) VALUES
(1, 1, 1, 'A1', '2025-01-15', '19:30:00', 1),
(1, 1, 1, 'A2', '2025-01-15', '19:30:00', 1),
(1, 1, 1, 'A3', '2025-01-15', '19:30:00', 1),
(2, 2, 4, 'B5', '2025-01-20', '20:00:00', 2),
(2, 2, 4, 'B6', '2025-01-20', '20:00:00', 2);

-- Demo data for customers
INSERT INTO customers (id, username, password, email, phone_number) VALUES
(1, 'john_doe', 'password123', 'john@example.com', '1234567890'),
(2, 'jane_smith', 'securepass', 'jane@example.com', '9876543210'),
(3, 'mike_johnson', 'mikepass', 'mike@example.com', '5551234567');

-- Demo data for staff
INSERT INTO staff (id, username, password, role, permissions) VALUES
(1, 'admin', 'admin123', 'ADMIN', 'View Bookings,Generate Reports,Manage Musicals'),
(2, 'john_staff', 'staffpass1', 'STAFF', 'View Bookings,Manage Musicals'),
(3, 'sarah_manager', 'managerpass', 'ADMIN', 'View Bookings,Generate Reports,Manage Musicals,Manage Staff');

-- Demo data for income_data
INSERT INTO income_data (amount, transaction_date, category, description) VALUES
(500.00, '2025-01-15', 'Ticket Sales', 'The Phantom of the Opera - 2 tickets'),
(350.00, '2025-01-20', 'Ticket Sales', 'Les Misérables - 1 ticket'),
(450.00, '2025-02-01', 'Ticket Sales', 'The Lion King - 1 VIP ticket'),
(300.00, '2025-02-10', 'Ticket Sales', 'Wicked - 1 ticket'),
(400.00, '2025-02-15', 'Ticket Sales', 'Hamilton - 1 ticket');

-- Demo data for receipts
INSERT INTO receipts (id, customer_id, musical_id, total_price, show_date, show_time, receipt_text) VALUES
(1, 1, 3, 450.00, '2024-12-29', '10:00:00', 'Date and Time: 2024-12-29 01:19:59
Musical: The Lion King
Show Time: 2024-12-29 10:00
Ticket Details:
Adult ticket - Seat VIP1: £150.00
Adult ticket - Seat VIP3: £150.00
Adult ticket - Seat VIP2: £150.00

Total Price: £450.00
--------------------------------------'),
(2, 2, 1, 200.00, '2025-01-15', '19:30:00', 'Date and Time: 2025-01-15 10:30:45
Musical: The Phantom of the Opera
Show Time: 2025-01-15 19:30
Ticket Details:
Adult ticket - Seat A1: £100.00
Adult ticket - Seat A2: £100.00

Total Price: £200.00
--------------------------------------'),
(3, 3, 2, 350.00, '2025-01-20', '20:00:00', 'Date and Time: 2025-01-20 14:15:30
Musical: Les Misérables
Show Time: 2025-01-20 20:00
Ticket Details:
Adult ticket - Seat B5: £350.00

Total Price: £350.00
--------------------------------------');
