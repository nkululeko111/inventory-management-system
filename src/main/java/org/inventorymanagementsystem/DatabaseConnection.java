package org.inventorymanagementsystem;
import java.sql.*;
import org.h2.tools.Server;


// Database Connection Class
class DatabaseConnection {
    private static final String DB_URL = "jdbc:h2:./inventoryDB;AUTO_SERVER=TRUE;DB_CLOSE_DELAY=-1";    private static final String USER = "sa";
    private static final String PASS = "";

    public static Connection getConnection() throws SQLException {
        try {
            Class.forName("org.h2.Driver"); // Ensure the driver is loaded
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return DriverManager.getConnection(DB_URL, USER, PASS);
    }

    public static void startWebServer() {
        try {
            Server.createWebServer("-web", "-webAllowOthers", "-webPort", "8082").start();
            System.out.println("H2 Console started at http://localhost:8082");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}


