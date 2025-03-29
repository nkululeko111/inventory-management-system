
# Inventory Management System

![Java](https://img.shields.io/badge/Java-17%2B-blue)
![MySQL](https://img.shields.io/badge/MySQL-8.0%2B-orange)
![License](https://img.shields.io/badge/License-MIT-green)

A Java-based inventory management solution for small retail businesses with MySQL backend.

## Features

- **Product Management**
  - Add new products with name, price, and stock quantity
  - Update existing product information
  - View complete product catalog

- **Inventory Control**
  - Adjust stock levels manually
  - Automatic stock deduction on sales
  - Low stock threshold alerts

- **Sales Tracking**
  - Record sales transactions
  - Maintain sales history
  - Link sales to inventory items

## Technology Stack

- **Core**: Java 17 (Object-Oriented Programming)
- **Database**: MySQL 8.0
- **Connectivity**: JDBC API
- **CLI Interface**: Java Scanner

## Database Schema

```sql
CREATE DATABASE inventory;
USE inventory;

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
```

## Installation

### Prerequisites
- JDK 17+
- MySQL Server 8.0+
- MySQL Connector/J

### Setup
1. Clone the repository:
   ```bash
   git clone https://github.com/nkululeko111/inventory-management-system.git
   ```

2. Import the project into your IDE (IntelliJ/Eclipse)

3. Configure database connection in `DatabaseConnection.java`:
   ```java
   private static final String DB_URL = "jdbc:mysql://localhost:3306/inventory";
   private static final String USER = "your_username";
   private static final String PASS = "your_password";
   ```

4. Execute the SQL schema to create database tables

## Usage

Run the application from `InventoryManagementSystem.java` and use the console menu:

```
Inventory Management System
1. Add Product
2. Update Stock
3. View Stock
4. Record Sale
5. Exit
```

## API Documentation (Optional)

For REST API usage, see the [API Documentation](API_DOCS.md).

## Roadmap

- [ ] Graphical User Interface (GUI)
- [ ] User authentication system
- [ ] Advanced reporting module
- [ ] Email/SMS stock alerts
- [ ] Barcode scanning integration

## Contributing

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/your-feature`)
3. Commit your changes (`git commit -m 'Add some feature'`)
4. Push to the branch (`git push origin feature/your-feature`)
5. Open a Pull Request

## License

Distributed under the MIT License. See `LICENSE` for more information.

## Contact

Project Maintainer: nkululeko
Project Link: https://github.com/nkululeko111/inventory-management-system
