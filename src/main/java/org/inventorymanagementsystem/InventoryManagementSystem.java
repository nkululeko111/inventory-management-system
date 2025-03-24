package org.inventorymanagementsystem;

import java.sql.*;
import java.util.Scanner;

// Inventory Management System Class
public class InventoryManagementSystem {
    public static void main(String[] args) {
        DatabaseConnection.startWebServer(); // Start H2 Console
        try (Connection conn = DatabaseConnection.getConnection()) {
            createTables(conn);
            Scanner scanner = new Scanner(System.in);
            int choice;
            do {
                System.out.println("1. Add Product\n2. Update Stock\n3. View Stock\n4. Record Sale\n5. Exit");
                System.out.print("Enter choice: ");
                choice = scanner.nextInt();
                scanner.nextLine(); // Consume newline

                switch (choice) {
                    case 1:
                        addProduct(conn, scanner);
                        break;
                    case 2:
                        updateStock(conn, scanner);
                        break;
                    case 3:
                        viewStock(conn);
                        break;
                    case 4:
                        recordSale(conn, scanner);
                        break;
                    case 5:
                        System.out.println("Exiting...");
                        break;
                    default:
                        System.out.println("Invalid choice. Try again.");
                }
            } while (choice != 5);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void createTables(Connection conn) throws SQLException {
        String createProductsTable = "CREATE TABLE IF NOT EXISTS products (" +
                "id INT AUTO_INCREMENT PRIMARY KEY, " +
                "name VARCHAR(255), " +
                "price DOUBLE, " +
                "stock_quantity INT)";

        String createSalesTable = "CREATE TABLE IF NOT EXISTS sales (" +
                "id INT AUTO_INCREMENT PRIMARY KEY, " +
                "product_id INT, " +
                "quantity_sold INT, " +
                "sale_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                "FOREIGN KEY (product_id) REFERENCES products(id))";

        try (Statement stmt = conn.createStatement()) {
            stmt.execute(createProductsTable);
            stmt.execute(createSalesTable);
        }
    }

    private static void addProduct(Connection conn, Scanner scanner) throws SQLException {
        System.out.print("Enter product name: ");
        String name = scanner.nextLine();
        System.out.print("Enter price: ");
        double price = scanner.nextDouble();
        System.out.print("Enter stock quantity: ");
        int quantity = scanner.nextInt();

        String sql = "INSERT INTO products (name, price, stock_quantity) VALUES (?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, name);
            stmt.setDouble(2, price);
            stmt.setInt(3, quantity);
            stmt.executeUpdate();
            System.out.println("Product added successfully.");
        }
    }

    private static void updateStock(Connection conn, Scanner scanner) throws SQLException {
        System.out.print("Enter product ID: ");
        int productId = scanner.nextInt();
        System.out.print("Enter new stock quantity: ");
        int newQuantity = scanner.nextInt();

        String sql = "UPDATE products SET stock_quantity = ? WHERE id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, newQuantity);
            stmt.setInt(2, productId);
            stmt.executeUpdate();
            System.out.println("Stock updated successfully.");
        }
    }

    private static void viewStock(Connection conn) throws SQLException {
        String sql = "SELECT * FROM products";
        try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                System.out.printf("ID: %d, Name: %s, Price: %.2f, Stock: %d\n",
                        rs.getInt("id"), rs.getString("name"), rs.getDouble("price"), rs.getInt("stock_quantity"));
            }
        }
    }

    private static void recordSale(Connection conn, Scanner scanner) throws SQLException {
        System.out.print("Enter product ID: ");
        int productId = scanner.nextInt();
        System.out.print("Enter quantity sold: ");
        int quantitySold = scanner.nextInt();

        String sql = "INSERT INTO sales (product_id, quantity_sold, sale_date) VALUES (?, ?, CURRENT_TIMESTAMP)";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, productId);
            stmt.setInt(2, quantitySold);
            stmt.executeUpdate();
            System.out.println("Sale recorded successfully.");
        }
    }
}
