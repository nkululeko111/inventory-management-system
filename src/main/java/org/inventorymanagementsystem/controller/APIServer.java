package org.inventorymanagementsystem.controller;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import org.inventorymanagementsystem.InventoryManagementSystem;
import org.inventorymanagementsystem.model.Product;
import org.inventorymanagementsystem.model.Sale;
import org.inventorymanagementsystem.model.Supplier;

import java.sql.Connection;
import java.sql.SQLException;
import static spark.Spark.*;

public class APIServer {
    private static final Gson gson = new Gson();

    public static void start() {
        port(4567);
        configureCORS();
        setupExceptionHandling();
        setupEndpoints();
    }

    private static void configureCORS() {
        options("/*", (request, response) -> {
            String accessControlRequestHeaders = request.headers("Access-Control-Request-Headers");
            if (accessControlRequestHeaders != null) {
                response.header("Access-Control-Allow-Headers", accessControlRequestHeaders);
            }
            String accessControlRequestMethod = request.headers("Access-Control-Request-Method");
            if (accessControlRequestMethod != null) {
                response.header("Access-Control-Allow-Methods", accessControlRequestMethod);
            }
            return "OK";
        });

        before((request, response) -> {
            response.header("Access-Control-Allow-Origin", "*");
            response.header("Content-Type", "application/json");
        });
        before((req, res) -> {
            System.out.println("Received " + req.requestMethod() + " at " + req.pathInfo());
            if (req.body() != null && !req.body().isEmpty()) {
                System.out.println("Request body: " + req.body());
            }
        });
    }

    private static void setupExceptionHandling() {
        exception(JsonSyntaxException.class, (e, req, res) -> {
            res.status(400);
            res.body(gson.toJson(new ErrorResponse("Invalid JSON format: " + e.getMessage())));
        });

        exception(SQLException.class, (e, req, res) -> {
            res.status(500);
            res.body(gson.toJson(new ErrorResponse("Database error: " + e.getMessage())));
        });

        exception(IllegalArgumentException.class, (e, req, res) -> {
            res.status(400);
            res.body(gson.toJson(new ErrorResponse("Bad Request: " + e.getMessage())));
        });

        exception(Exception.class, (e, req, res) -> {
            res.status(500);
            res.body(gson.toJson(new ErrorResponse("Internal Server Error: " + e.getMessage())));
        });
    }

    private static void setupEndpoints() {
        // Product Endpoints
        get("/api/products", (req, res) -> {
            try (Connection conn = DatabaseConnection.getConnection()) {
                return InventoryManagementSystem.getAllProducts(conn);
            }
        }, gson::toJson);

        get("/api/products/:id", (req, res) -> {
            int id = Integer.parseInt(req.params(":id"));
            try (Connection conn = DatabaseConnection.getConnection()) {
                return InventoryManagementSystem.getProductById(conn, id);
            }
        }, gson::toJson);

        post("/api/products", (req, res) -> {
            Product product = gson.fromJson(req.body(), Product.class);
            try (Connection conn = DatabaseConnection.getConnection()) {
                return InventoryManagementSystem.addProduct(conn, product);
            }
        }, gson::toJson);

        // Update Product
        put("/api/products/:id", (req, res) -> {
            int id = Integer.parseInt(req.params(":id"));
            Product product = gson.fromJson(req.body(), Product.class);
            product.setId(id);  // Ensure ID matches path
            try (Connection conn = DatabaseConnection.getConnection()) {
                return InventoryManagementSystem.updateProduct(conn, product);
            }
        }, gson::toJson);

        delete("/api/products/:id", (req, res) -> {
            int id = Integer.parseInt(req.params(":id"));
            try (Connection conn = DatabaseConnection.getConnection()) {
                return InventoryManagementSystem.deleteProduct(conn, id);
            }
        }, gson::toJson);

        // Stock Management Endpoints
        patch("/api/products/:id/stock", (req, res) -> {
            int id = Integer.parseInt(req.params(":id"));
            int quantity = Integer.parseInt(req.queryParams("quantity"));
            try (Connection conn = DatabaseConnection.getConnection()) {
                return InventoryManagementSystem.updateStockQuantity(conn, id, quantity);
            }
        }, gson::toJson);

        post("/api/products/:id/stock/adjust", (req, res) -> {
            int id = Integer.parseInt(req.params(":id"));
            StockAdjustment adjustment = gson.fromJson(req.body(), StockAdjustment.class);
            try (Connection conn = DatabaseConnection.getConnection()) {
                return InventoryManagementSystem.adjustStock(conn, id, adjustment.getDelta());
            }
        }, gson::toJson);
//        ...

            // Supplier Endpoints
            get("/api/suppliers", (req, res) -> {
                try (Connection conn = DatabaseConnection.getConnection()) {
                    return InventoryManagementSystem.getAllSuppliers(conn);
                }
            }, gson::toJson);
        get("/api/reports/sales", (req, res) -> {
            String fromDate = req.queryParams("from");
            String toDate = req.queryParams("to");
            try (Connection conn = DatabaseConnection.getConnection()) {
                return InventoryManagementSystem.generateSalesReport(conn, fromDate, toDate);
            }
        }, gson::toJson);

            get("/api/suppliers/:id", (req, res) -> {
                int id = Integer.parseInt(req.params(":id"));
                try (Connection conn = DatabaseConnection.getConnection()) {
                    return InventoryManagementSystem.getSupplierById(conn, id);
                }
            }, gson::toJson);

            post("/api/suppliers", (req, res) -> {
                Supplier supplier = gson.fromJson(req.body(), Supplier.class);
                try (Connection conn = DatabaseConnection.getConnection()) {
                    return InventoryManagementSystem.addSupplier(conn, supplier);
                }
            }, gson::toJson);

            put("/api/suppliers/:id", (req, res) -> {
                int id = Integer.parseInt(req.params(":id"));
                Supplier supplier = gson.fromJson(req.body(), Supplier.class);
                supplier.setId(id);
                try (Connection conn = DatabaseConnection.getConnection()) {
                    return InventoryManagementSystem.updateSupplier(conn, supplier);
                }
            }, gson::toJson);

            delete("/api/suppliers/:id", (req, res) -> {
                int id = Integer.parseInt(req.params(":id"));
                try (Connection conn = DatabaseConnection.getConnection()) {
                    return InventoryManagementSystem.deleteSupplier(conn, id);
                }
            }, gson::toJson);

            // Update product endpoints to include supplier
            post("/api/products", (req, res) -> {
                Product product = gson.fromJson(req.body(), Product.class);
                try (Connection conn = DatabaseConnection.getConnection()) {
                    return InventoryManagementSystem.addProduct(conn, product);
                }
            }, gson::toJson);

            put("/api/products/:id", (req, res) -> {
                int id = Integer.parseInt(req.params(":id"));
                Product product = gson.fromJson(req.body(), Product.class);
                product.setId(id);
                try (Connection conn = DatabaseConnection.getConnection()) {
                    return InventoryManagementSystem.updateProduct(conn, product);
                }
            }, gson::toJson);


        // Sales Endpoints
        get("/api/sales", (req, res) -> {
            try (Connection conn = DatabaseConnection.getConnection()) {
                return InventoryManagementSystem.getAllSales(conn);
            }
        }, gson::toJson);

        get("/api/sales/:id", (req, res) -> {
            int id = Integer.parseInt(req.params(":id"));
            try (Connection conn = DatabaseConnection.getConnection()) {
                return InventoryManagementSystem.getSaleById(conn, id);
            }
        }, gson::toJson);

        post("/api/sales", (req, res) -> {
            Sale sale = gson.fromJson(req.body(), Sale.class);
            try (Connection conn = DatabaseConnection.getConnection()) {
                return InventoryManagementSystem.recordSale(conn, sale);
            }
        }, gson::toJson);

        // Reports Endpoints
        get("/api/reports/inventory", (req, res) -> {
            try (Connection conn = DatabaseConnection.getConnection()) {
                return InventoryManagementSystem.generateInventoryReport(conn);
            }
        }, gson::toJson);

        get("/api/reports/sales", (req, res) -> {
            String fromDate = req.queryParams("from");
            String toDate = req.queryParams("to");
            try (Connection conn = DatabaseConnection.getConnection()) {
                return InventoryManagementSystem.generateSalesReport(conn, fromDate, toDate);
            }
        }, gson::toJson);

        // System Endpoints
        get("/api/system/status", (req, res) -> {
            return new SystemStatus("Operational", Runtime.getRuntime().availableProcessors());
        }, gson::toJson);
    }

    // DTO Classes for API
    private static class ErrorResponse {
        private final String message;
        public ErrorResponse(String message) { this.message = message; }
        public String getMessage() { return message; }
    }

    private static class StockAdjustment {
        private int delta;
        public int getDelta() { return delta; }
        public void setDelta(int delta) { this.delta = delta; }
    }

    private static class SystemStatus {
        private final String status;
        private final int availableProcessors;
        public SystemStatus(String status, int availableProcessors) {
            this.status = status;
            this.availableProcessors = availableProcessors;
        }
        public String getStatus() { return status; }
        public int getAvailableProcessors() { return availableProcessors; }
    }
}
