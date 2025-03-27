package org.inventorymanagementsystem;

import java.sql.*;
import org.h2.tools.Server;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.Properties;

public final class DatabaseConnection {
    private static final Logger LOGGER = Logger.getLogger(DatabaseConnection.class.getName());

    // Configuration using properties for flexibility
    private static final Properties DB_CONFIG = new Properties();
    static {
        DB_CONFIG.setProperty("url", "jdbc:h2:file:./target/db/inventoryDB;DB_CLOSE_DELAY=-1;AUTO_SERVER=TRUE");
        DB_CONFIG.setProperty("username", "sa");
        DB_CONFIG.setProperty("password", "");
        DB_CONFIG.setProperty("webPort", "8082");
        DB_CONFIG.setProperty("apiPort", "4567");
    }

    private static volatile Server webServer;
    private static volatile ConnectionPool connectionPool;

    static {
        initializeDatabase();
    }

    private static void initializeDatabase() {
        try {
            Class.forName("org.h2.Driver");
            new java.io.File("./target/db").mkdirs();

            try (Connection conn = createConnection()) {
                Statement stmt = conn.createStatement();

                // Create all tables with proper relationships in one go
                stmt.execute("CREATE TABLE IF NOT EXISTS suppliers (" +
                        "id INT AUTO_INCREMENT PRIMARY KEY, " +
                        "name VARCHAR(255) NOT NULL, " +
                        "contact_person VARCHAR(255), " +
                        "email VARCHAR(255), " +
                        "phone VARCHAR(50))");

                stmt.execute("CREATE TABLE IF NOT EXISTS products (" +
                        "id INT AUTO_INCREMENT PRIMARY KEY, " +
                        "name VARCHAR(255) NOT NULL, " +
                        "price DECIMAL(10,2) NOT NULL, " +
                        "stock_quantity INT NOT NULL DEFAULT 0, " +
                        "supplier_id INT, " +
                        "FOREIGN KEY (supplier_id) REFERENCES suppliers(id))");

                stmt.execute("CREATE TABLE IF NOT EXISTS sales (" +
                        "id INT AUTO_INCREMENT PRIMARY KEY, " +
                        "product_id INT NOT NULL, " +
                        "quantity_sold INT NOT NULL, " +
                        "sale_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                        "FOREIGN KEY (product_id) REFERENCES products(id))");

                stmt.execute("CREATE TABLE IF NOT EXISTS inventory_log (" +
                        "id INT AUTO_INCREMENT PRIMARY KEY, " +
                        "product_id INT NOT NULL, " +
                        "operation VARCHAR(50) NOT NULL, " +
                        "quantity INT NOT NULL, " +
                        "timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                        "FOREIGN KEY (product_id) REFERENCES products(id))");

                conn.commit();
                LOGGER.info("Database tables initialized successfully");

                // Initialize connection pool
                connectionPool = new ConnectionPool(10, () -> {
                    try {
                        return createConnection();
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                });
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Database initialization failed", e);
        }
    }

    private static Connection createConnection() throws SQLException {
        return DriverManager.getConnection(
                DB_CONFIG.getProperty("url"),
                DB_CONFIG.getProperty("username"),
                DB_CONFIG.getProperty("password")
        );
    }

    public static Connection getConnection() throws SQLException {
        if (connectionPool == null) {
            throw new SQLException("Connection pool not initialized");
        }
        return connectionPool.getConnection();
    }

    public static void releaseConnection(Connection conn) {
        if (connectionPool != null && conn != null) {
            connectionPool.releaseConnection(conn);
        }
    }

    public static synchronized void startWebServer() throws SQLException {
        if (webServer == null) {
            webServer = Server.createWebServer(
                    "-web",
                    "-webAllowOthers",
                    "-webPort", DB_CONFIG.getProperty("webPort"),
                    "-baseDir", "./target/db"
            ).start();
            LOGGER.info("H2 Console available at http://localhost:" + DB_CONFIG.getProperty("webPort"));
            LOGGER.info("API Server available at http://localhost:" + DB_CONFIG.getProperty("apiPort"));
            LOGGER.info("JDBC URL: " + DB_CONFIG.getProperty("url"));
        }
    }

    public static synchronized void stopWebServer() {
        if (webServer != null) {
            webServer.stop();
            webServer = null;
            LOGGER.info("H2 Console stopped");
        }
        if (connectionPool != null) {
            connectionPool.shutdown();
            connectionPool = null;
            LOGGER.info("Connection pool shutdown");
        }
    }

    // Simple connection pool implementation
    private static class ConnectionPool {
        private final java.util.Queue<Connection> pool;
        private final java.util.function.Supplier<Connection> connectionSupplier;
        private final int maxSize;

        public ConnectionPool(int maxSize, java.util.function.Supplier<Connection> connectionSupplier) {
            this.pool = new java.util.ArrayDeque<>(maxSize);
            this.connectionSupplier = connectionSupplier;
            this.maxSize = maxSize;
        }

        public synchronized Connection getConnection() throws SQLException {
            if (!pool.isEmpty()) {
                return pool.poll();
            }
            return connectionSupplier.get();
        }

        public synchronized void releaseConnection(Connection conn) {
            if (pool.size() < maxSize) {
                pool.offer(conn);
            } else {
                try { conn.close(); } catch (SQLException e) { LOGGER.log(Level.WARNING, "Failed to close connection", e); }
            }
        }

        public synchronized void shutdown() {
            for (Connection conn : pool) {
                try { conn.close(); } catch (SQLException e) { LOGGER.log(Level.WARNING, "Failed to close connection", e); }
            }
            pool.clear();
        }
    }
}