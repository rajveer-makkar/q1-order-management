-- Database and table used by the Order Management System.
-- Run this script first:  mysql -u root -p < database/ecommerce_db.sql

CREATE DATABASE IF NOT EXISTS ecommerce_db;
USE ecommerce_db;

-- order_id is the PRIMARY KEY, so duplicate IDs are rejected by MySQL itself.
CREATE TABLE IF NOT EXISTS Orders (
    order_id      INT PRIMARY KEY,
    customer_name VARCHAR(100),
    product_name  VARCHAR(100),
    quantity      INT,
    unit_price    DOUBLE,
    status        VARCHAR(30)
);

-- The application itself inserts four sample orders when menu option 1 is chosen.
-- Keep the INSERT below commented out unless you want to preload rows manually;
-- otherwise option 1 will correctly report those IDs as duplicates.
-- INSERT INTO Orders (order_id, customer_name, product_name, quantity, unit_price, status) VALUES
-- (101, 'Aarav Sharma', 'Laptop',   2, 55000.0, 'Pending'),
-- (102, 'Priya Verma',  'Mouse',    5,   799.0, 'Shipped'),
-- (103, 'Rohan Gupta',  'Keyboard', 4,  1999.0, 'Delivered'),
-- (104, 'Neha Singh',   'Monitor',  3, 12000.0, 'Cancelled');
