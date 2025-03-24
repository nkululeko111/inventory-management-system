Inventory Management System

Overview

This Inventory Management System is a simple Java-based application designed for small retail businesses. It allows users to manage product stock levels, record sales transactions, and receive notifications when stock is low. The system uses a MySQL database for data storage and retrieval.

Features

Add Products: Register new products with a name, price, and stock quantity.

Update Stock: Modify stock levels for existing products.

View Stock: Display all products with their current stock levels.

Record Sales: Log sales transactions and update inventory accordingly.

Low Stock Alerts: Notify users when stock falls below a predefined threshold.

Technologies Used

Java (Object-Oriented Programming)

MySQL (Relational Database Management System)

JDBC (Database Connectivity)

Scanner (Java.util) (For user input handling)

Prerequisites

Before running the project, ensure you have the following installed:

Java Development Kit (JDK) 11 or later

MySQL Server

MySQL Connector/J (JDBC driver)

Database Setup

Create a new MySQL database:

CREATE DATABASE inventory;
USE inventory;

Create the required tables:

CREATE TABLE products (
    id INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(255) NOT NULL,
    price DECIMAL(10,2) NOT NULL,
    stock_quantity INT NOT NULL
);

CREATE TABLE sales (
    id INT PRIMARY KEY AUTO_INCREMENT,
    product_id INT,
    quantity_sold INT NOT NULL,
    sale_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (product_id) REFERENCES products(id)
);

How to Run the Project

Clone the repository or download the source code.

Open the project in your preferred Java IDE (e.g., IntelliJ, Eclipse, or VS Code).

Configure the database connection in DatabaseConnection class:

private static final String DB_URL = "jdbc:mysql://localhost:3306/inventory";
private static final String USER = "root";
private static final String PASS = "yourpassword";

Compile and run the InventoryManagementSystem.java file.

Use the command-line interface to interact with the system.

Usage Guide

Adding a Product: Enter the product name, price, and initial stock quantity.

Updating Stock: Provide the product ID and new stock quantity.

Viewing Stock: Displays all products with stock levels.

Recording a Sale: Enter the product ID and quantity sold.

Future Enhancements

Implement a graphical user interface (GUI).

Add user authentication and role management.

Generate detailed sales reports.

Integrate email notifications for restocking alerts.

License

This project is open-source and free to use under the MIT License.


