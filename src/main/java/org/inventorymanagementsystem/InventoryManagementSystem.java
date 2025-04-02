package org.inventorymanagementsystem;

import com.google.gson.Gson;
import org.inventorymanagementsystem.controller.DatabaseConnection;

import org.inventorymanagementsystem.model.Supplier;
import org.inventorymanagementsystem.model.Product;
import org.inventorymanagementsystem.model.Sale;


import static org.inventorymanagementsystem.controller.APIServer.start;
import static spark.Spark.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class InventoryManagementSystem {
    private static final Scanner scanner = new Scanner(System.in);
    private static final Gson gson = new Gson();

    public static void main(String[] args) {
        // Start the API server
        start();
        // Start the console interface
        startConsoleInterface();
    }

    private static void startConsoleInterface() {
        try {
            DatabaseConnection.startWebServer();
            try (Connection conn = DatabaseConnection.getConnection()) {
                awaitInitialization();
            }
        } catch (SQLException e) {
            System.err.println("Database error: " + e.getMessage());
            e.printStackTrace();
        } finally {
            scanner.close();
        }
    }



    // Shared business logic methods
    public static List<Product> getAllProducts(Connection conn) throws SQLException {
        List<Product> products = new ArrayList<>();
        // In getAllProducts(), modify the query:
        String sql = "SELECT p.id, p.name, p.price, p.stock_quantity, " +
                "p.supplier_id, s.name as supplier_name " +
                "FROM products p LEFT JOIN suppliers s ON p.supplier_id = s.id " +
                "ORDER BY p.name";
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                products.add(new Product(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getDouble("price"),
                        rs.getInt("stock_quantity")

                ));
            }
        }
        return products;
    }

    public static Product addProduct(Connection conn, Product product) throws SQLException {
        String sql = "INSERT INTO products (name, price, stock_quantity, supplier_id) VALUES (?, ?, ?, ?)";

        try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, product.getName());
            stmt.setDouble(2, product.getPrice());
            stmt.setInt(3, product.getStockQuantity());
            stmt.setInt(4, product.getSupplierId());

            stmt.executeUpdate();

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    return new Product(
                            rs.getInt(1),
                            product.getName(),
                            product.getPrice(),
                            product.getStockQuantity()
                    );
                }
            }
        }
        return product;
    }

    public static Product updateProductStock(Connection conn, int productId, int newQuantity) throws SQLException {
        String sql = "UPDATE products SET stock_quantity = ? WHERE id = ?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, newQuantity);
            stmt.setInt(2, productId);
            stmt.executeUpdate();
        }

        // Return updated product
        return getProductById(conn, productId);
    }

    // In InventoryManagementSystem.recordSale()
    public static Sale recordSale(Connection conn, Sale sale) throws SQLException {
        // Validate input
        if (sale.getQuantitySold() <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }

        conn.setAutoCommit(false);
        try {
            Product product = getProductById(conn, sale.getProductId());
            sale.setUnitPrice(product.getPrice()); // Capture current price

            if (product.getStockQuantity() < sale.getQuantitySold()) {
                throw new SQLException("Insufficient stock");
            }

            // Record sale (updated SQL to include price)
            String saleSql = "INSERT INTO sales (product_id, quantity_sold, unit_price) VALUES (?, ?, ?)";
            try (PreparedStatement stmt = conn.prepareStatement(saleSql, Statement.RETURN_GENERATED_KEYS)) {
                stmt.setInt(1, sale.getProductId());
                stmt.setInt(2, sale.getQuantitySold());
                stmt.setDouble(3, sale.getUnitPrice());
                stmt.executeUpdate();

                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) sale.setId(rs.getInt(1));
                }
            }

            // Update stock
            updateProductStock(conn, sale.getProductId(),
                    product.getStockQuantity() - sale.getQuantitySold());

            conn.commit();
            return sale;
        } catch (Exception e) {
            conn.rollback();
            throw e;
        } finally {
            conn.setAutoCommit(true);
        }
    }
    /**
     * Updates an existing product in the database
     * @param conn Database connection
     * @param product Product with updated values
     * @return The updated product
     * @throws SQLException If database error occurs
     */
    public static Product updateProduct(Connection conn, Product product) throws SQLException {
        String sql = "UPDATE products SET name = ?, price = ?, stock_quantity = ?, supplier_id = ? WHERE id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, product.getName());
            stmt.setDouble(2, product.getPrice());
            stmt.setInt(3, product.getStockQuantity());
            stmt.setInt(4, product.getId());

            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Product not found with ID: " + product.getId());
            }

            return product;
        }
    }

    /**
     * Deletes a product from the database
     * @param conn Database connection
     * @param id ID of product to delete
     * @return true if deletion was successful, false otherwise
     * @throws SQLException If database error occurs
     */
    public static boolean deleteProduct(Connection conn, int id) throws SQLException {
        String sql = "DELETE FROM products WHERE id = ?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;
        }
    }

    /**
     * Adjusts product stock by a delta value
     * @param conn Database connection
     * @param id Product ID
     * @param delta Amount to adjust stock by (positive or negative)
     * @return The updated product
     * @throws SQLException If database error occurs or insufficient stock
     */
    public static Product adjustStock(Connection conn, int id, int delta) throws SQLException {
        // First get current stock
        Product product = getProductById(conn, id);

        // Check if adjustment would make stock negative
        if (product.getStockQuantity() + delta < 0) {
            throw new SQLException("Insufficient stock for this adjustment");
        }

        String sql = "UPDATE products SET stock_quantity = stock_quantity + ? WHERE id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, delta);
            stmt.setInt(2, id);
            stmt.executeUpdate();

            // Return updated product
            return getProductById(conn, id);
        }
    }

    /**
     * Retrieves all sales records
     * @param conn Database connection
     * @return List of all sales
     * @throws SQLException If database error occurs
     */
    public static List<Sale> getAllSales(Connection conn) throws SQLException {
        List<Sale> sales = new ArrayList<>();
        String sql = "SELECT s.id, s.product_id, p.name as product_name, " +
                "s.quantity_sold, s.unit_price, s.sale_date " +
                "FROM sales s JOIN products p ON s.product_id = p.id " +
                "ORDER BY COALESCE(s.sale_date, CURRENT_TIMESTAMP) DESC";

        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Sale sale = new Sale();
                sale.setId(rs.getInt("id"));
                sale.setProductId(rs.getInt("product_id"));
                sale.setProductName(rs.getString("product_name"));
                sale.setQuantitySold(rs.getInt("quantity_sold"));
                sale.setUnitPrice(rs.getDouble("unit_price"));

                // Handle potential null sale_date
                Timestamp saleDate = rs.getTimestamp("sale_date");
                sale.setSaleDate(saleDate != null ? saleDate : new Timestamp(System.currentTimeMillis()));

                sales.add(sale);
            }
        }
        return sales;
    }

    /**
     * Retrieves a single sale by ID
     * @param conn Database connection
     * @param id Sale ID
     * @return The sale record
     * @throws SQLException If database error occurs or sale not found
     */
    public static Sale getSaleById(Connection conn, int id) throws SQLException {
        String sql = "SELECT s.id, s.product_id, p.name as product_name, " +
                "s.quantity_sold, s.sale_date as sale_date " +
                "FROM sales s JOIN products p ON s.product_id = p.id " +
                "WHERE s.id = ?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Sale sale = new Sale();
                    sale.setId(rs.getInt("id"));
                    sale.setProductId(rs.getInt("product_id"));
                    sale.setProductName(rs.getString("product_name"));
                    sale.setQuantitySold(rs.getInt("quantity_sold"));
                    sale.setSaleDate(rs.getTimestamp("sale_date"));
                    return sale;
                }
            }
        }
        throw new SQLException("Sale not found with ID: " + id);
    }

    /**
     * Generates an inventory report
     * @param conn Database connection
     * @return Inventory report containing summary data
     * @throws SQLException If database error occurs
     */
    public static InventoryReport generateInventoryReport(Connection conn) throws SQLException {
        InventoryReport report = new InventoryReport();

        // Get total products count
        String countSql = "SELECT COUNT(*) as total_products FROM products";
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(countSql)) {
            if (rs.next()) {
                report.setTotalProducts(rs.getInt("total_products"));
            }
        }

        // Get total stock value
        String valueSql = "SELECT SUM(price * stock_quantity) as total_value FROM products";
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(valueSql)) {
            if (rs.next()) {
                report.setTotalValue(rs.getDouble("total_value"));
            }
        }

        // Get low stock items (less than 10)
        String lowStockSql = "SELECT COUNT(*) as low_stock_items FROM products WHERE stock_quantity < 10";
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(lowStockSql)) {
            if (rs.next()) {
                report.setLowStockItems(rs.getInt("low_stock_items"));
            }
        }

        // Get out of stock items
        String outOfStockSql = "SELECT COUNT(*) as out_of_stock FROM products WHERE stock_quantity = 0";
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(outOfStockSql)) {
            if (rs.next()) {
                report.setOutOfStockItems(rs.getInt("out_of_stock"));
            }
        }

        return report;
    }

    /**
     * Generates a sales report for the given date range
     * @param conn Database connection
     * @param fromDate Start date (yyyy-MM-dd) or null for no lower bound
     * @param toDate End date (yyyy-MM-dd) or null for no upper bound
     * @return Sales report containing summary data
     * @throws SQLException If database error occurs
     */
    public static SalesReport generateSalesReport(Connection conn, String fromDate, String toDate) throws SQLException {
        SalesReport report = new SalesReport();
        report.setFromDate(fromDate);
        report.setToDate(toDate);

        // Base SQL with optional date filtering
        StringBuilder sql = new StringBuilder(
                "SELECT COUNT(*) as total_sales, SUM(s.quantity_sold) as total_units, " +
                        "SUM(s.quantity_sold * p.price) as total_revenue " +
                        "FROM sales s JOIN products p ON s.product_id = p.id "
        );

        List<String> conditions = new ArrayList<>();
        List<Object> parameters = new ArrayList<>();

        if (fromDate != null) {
            conditions.add("s.sale_date >= ?");
            parameters.add(fromDate);
        }
        if (toDate != null) {
            conditions.add("s.sale_date <= ?");
            parameters.add(toDate + " 23:59:59"); // Include entire end day
        }

        if (!conditions.isEmpty()) {
            sql.append(" WHERE ").append(String.join(" AND ", conditions));
        }

        try (PreparedStatement stmt = conn.prepareStatement(sql.toString())) {
            // Set parameters
            for (int i = 0; i < parameters.size(); i++) {
                stmt.setObject(i + 1, parameters.get(i));
            }

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    report.setTotalSales(rs.getInt("total_sales"));
                    report.setTotalUnitsSold(rs.getInt("total_units"));
                    report.setTotalRevenue(rs.getDouble("total_revenue"));
                }
            }
        }

        // Get top selling products
        String topProductsSql = "SELECT p.id, p.name, SUM(s.quantity_sold) as units_sold " +
                "FROM sales s JOIN products p ON s.product_id = p.id ";

        if (!conditions.isEmpty()) {
            topProductsSql += " WHERE " + String.join(" AND ", conditions);
        }

        topProductsSql += " GROUP BY p.id, p.name ORDER BY units_sold DESC LIMIT 5";

        try (PreparedStatement stmt = conn.prepareStatement(topProductsSql)) {
            // Set parameters
            for (int i = 0; i < parameters.size(); i++) {
                stmt.setObject(i + 1, parameters.get(i));
            }

            try (ResultSet rs = stmt.executeQuery()) {
                List<TopProduct> topProducts = new ArrayList<>();
                while (rs.next()) {
                    TopProduct topProduct = new TopProduct();
                    topProduct.setProductId(rs.getInt("id"));
                    topProduct.setProductName(rs.getString("name"));
                    topProduct.setUnitsSold(rs.getInt("units_sold"));
                    topProducts.add(topProduct);
                }
                report.setTopProducts(topProducts);
            }
        }

        return report;
    }
    /**
     * Retrieves paginated list of products
     * @param conn Valid database connection
     * @param limit Maximum number of products to return
     * @param offset Number of products to skip
     * @return List of products
     * @throws SQLException If database error occurs
     * @throws IllegalArgumentException If limit or offset are negative
     * @since 1.1
     */
    public static List<Product> getProducts(Connection conn, int limit, int offset) throws SQLException {
        if (limit < 0 || offset < 0) {
            throw new IllegalArgumentException("Limit and offset must be non-negative");
        }

        final String sql = "SELECT id, name, price, stock_quantity FROM products ORDER BY name LIMIT ? OFFSET ?";
        List<Product> products = new ArrayList<>();

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, limit);
            stmt.setInt(2, offset);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    products.add(new Product(
                            rs.getInt("id"),
                            rs.getString("name"),
                            rs.getDouble("price"),
                            rs.getInt("stock_quantity")
                    ));
                }
            }
        }

        return products;
    }
    // Supplier CRUD operations
    public static List<Supplier> getAllSuppliers(Connection conn) throws SQLException {
        List<Supplier> suppliers = new ArrayList<>();
        String sql = "SELECT id, name, contact_person, email, phone FROM suppliers ORDER BY name";

        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                suppliers.add(new Supplier(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("contact_person"),
                        rs.getString("email"),
                        rs.getString("phone")
                ));
            }
        }
        return suppliers;
    }

    public static Supplier addSupplier(Connection conn, Supplier supplier) throws SQLException {
        String sql = "INSERT INTO suppliers (name, contact_person, email, phone) VALUES (?, ?, ?, ?)";

        try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, supplier.getName());
            stmt.setString(2, supplier.getContactPerson());
            stmt.setString(3, supplier.getEmail());
            stmt.setString(4, supplier.getPhone());
            stmt.executeUpdate();

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    return new Supplier(
                            rs.getInt(1),
                            supplier.getName(),
                            supplier.getContactPerson(),
                            supplier.getEmail(),
                            supplier.getPhone()
                    );
                }
            }
        }
        return supplier;
    }

    public static Supplier getSupplierById(Connection conn, int id) throws SQLException {
        String sql = "SELECT id, name, contact_person, email, phone FROM suppliers WHERE id = ?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new Supplier(
                            rs.getInt("id"),
                            rs.getString("name"),
                            rs.getString("contact_person"),
                            rs.getString("email"),
                            rs.getString("phone")
                    );
                }
            }
        }
        throw new SQLException("Supplier not found with ID: " + id);
    }

    public static boolean deleteSupplier(Connection conn, int id) throws SQLException {
        String sql = "DELETE FROM suppliers WHERE id = ?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;
        }
    }

    public static Supplier updateSupplier(Connection conn, Supplier supplier) throws SQLException {
        String sql = "UPDATE suppliers SET name = ?, contact_person = ?, email = ?, phone = ? WHERE id = ?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, supplier.getName());
            stmt.setString(2, supplier.getContactPerson());
            stmt.setString(3, supplier.getEmail());
            stmt.setString(4, supplier.getPhone());
            stmt.setInt(5, supplier.getId());

            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Supplier not found with ID: " + supplier.getId());
            }

            return supplier;
        }
    }
    // Helper method to get product by ID
    public static Product getProductById(Connection conn, int id) throws SQLException {
        String sql = "SELECT id, name, price, stock_quantity, supplier_id FROM products WHERE id = ?";        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Product product = new Product();
                    product.setId(rs.getInt("id"));
                    product.setName(rs.getString("name"));
                    product.setPrice(rs.getDouble("price"));
                    product.setStockQuantity(rs.getInt("stock_quantity"));
                    return product;
                }
            }
        }
        throw new SQLException("Product not found with ID: " + id);
    }

    // Method to update stock quantity
    public static boolean updateStockQuantity(Connection conn, int id, int quantity) throws SQLException {
        String sql = "UPDATE products SET stock_quantity = ? WHERE id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, quantity);
            stmt.setInt(2, id);

            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        }
    }
}

