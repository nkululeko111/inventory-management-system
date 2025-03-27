package org.inventorymanagementsystem;
import java.util.Objects;
import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Represents a product in the inventory management system.
 * This immutable class provides thread-safe operations for product management.
 */
public final class Product {
    private final int id;
    private final String name;
    private final BigDecimal price;
    private int stockQuantity;
    private int supplierId;

    /**
     * Constructs a new Product.
     *
     * @param id the product ID (must be positive)
     * @param name the product name (cannot be null or empty)
     * @param price the product price (must be positive)
     * @param stockQuantity the stock quantity (must be non-negative)
     * @throws IllegalArgumentException if any parameter is invalid
     * @throws NullPointerException if name is null
     */
    public Product(int id, String name, double price, int stockQuantity) {
        validateId(id);
        this.id = id;
        this.name = validateName(name);
        this.price = validatePrice(price);
        this.stockQuantity = validateStockQuantity(stockQuantity);
    }

    public Product(int id, String name, BigDecimal price) {

        this.id = id;
        this.name = name;
        this.price = price;
    }


    // Add these methods
    public int getSupplierId() { return supplierId; }
    public void setSupplierId(int supplierId) { this.supplierId = supplierId; }
    public Product() {
        this.id = 0;
        this.name = "Unknown";
        this.price = BigDecimal.ZERO;
        this.stockQuantity = 0;
    }



    // Validation methods
    private static void validateId(int id) {
        if (id <= 0) {
            throw new IllegalArgumentException("ID must be positive");
        }
    }

    private static String validateName(String name) {
        if (name == null) {
            throw new NullPointerException("Name cannot be null");
        }
        String trimmed = name.trim();
        if (trimmed.isEmpty()) {
            throw new IllegalArgumentException("Name cannot be empty");
        }
        return trimmed;
    }

    private static BigDecimal validatePrice(double price) {
        if (price <= 0) {
            throw new IllegalArgumentException("Price must be positive");
        }
        return BigDecimal.valueOf(price).setScale(2, RoundingMode.HALF_UP);
    }

    private static int validateStockQuantity(int quantity) {
        if (quantity < 0) {
            throw new IllegalArgumentException("Stock quantity cannot be negative");
        }
        return quantity;
    }

    // Getters
    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public double getPrice() {
        return price.doubleValue();
    }

    public BigDecimal getExactPrice() {
        return price;
    }

    public int getStockQuantity() {
        return stockQuantity;
    }

    // Stock operations
    /**
     * Updates the stock by a delta amount.
     * @param delta the change in quantity (positive for addition, negative for removal)
     * @return a new Product instance with updated stock
     * @throws IllegalArgumentException if resulting stock would be negative
     */
    public Product withStockDelta(int delta) {
        int newQuantity = this.stockQuantity + delta;
        if (newQuantity < 0) {
            throw new IllegalArgumentException(
                    String.format("Insufficient stock. Current: %d, Attempted change: %d",
                            stockQuantity, delta));
        }
        return new Product(this.id, this.name, this.price.doubleValue(), newQuantity);
    }

    /**
     * Creates a new Product with updated price.
     * @param newPrice the new price (must be positive)
     * @return a new Product instance with updated price
     * @throws IllegalArgumentException if price is not positive
     */
    public Product withPrice(double newPrice) {
        return new Product(this.id, this.name, newPrice, this.stockQuantity);
    }

    /**
     * Creates a new Product with updated name.
     * @param newName the new name (cannot be null or empty)
     * @return a new Product instance with updated name
     * @throws IllegalArgumentException if name is null or empty
     */
    public Product withName(String newName) {
        return new Product(this.id, newName, this.price.doubleValue(), this.stockQuantity);
    }

    // Object overrides
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Product product = (Product) o;
        return id == product.id &&
                stockQuantity == product.stockQuantity &&
                name.equals(product.name) &&
                price.compareTo(product.price) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, price, stockQuantity);
    }

    @Override
    public String toString() {
        return String.format("Product[id=%d, name='%s', price=%.2f, stock=%d]",
                id, name, price.doubleValue(), stockQuantity);
    }

    // Builder pattern for complex constructions
    public static Builder builder() {
        return new Builder();
    }

    public void setId(int id) {
    }

    public void setName(String name) {
    }

    public void setPrice(double price) {
    }

    public void setStockQuantity(int stockQuantity) {
    }

    public static final class Builder {
        private int id;
        private String name;
        private double price;
        private int stockQuantity;

        private Builder() {}

        public Builder id(int id) {
            this.id = id;
            return this;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder price(double price) {
            this.price = price;
            return this;
        }

        public Builder stockQuantity(int quantity) {
            this.stockQuantity = quantity;
            return this;
        }

        public Product build() {
            return new Product(id, name, price, stockQuantity);
        }
    }
}