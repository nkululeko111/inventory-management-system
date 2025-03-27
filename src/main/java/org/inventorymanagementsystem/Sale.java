package org.inventorymanagementsystem;

import java.sql.Timestamp;

public class Sale {
    private int id;
    private int productId;
    private String productName;
    private int quantitySold;
    private double unitPrice;
    private double totalPrice;
    private Timestamp saleDate;

    // Default constructor
    public Sale() {
        this.saleDate = new Timestamp(System.currentTimeMillis());
    }

    // Constructor for creating new sales
    public Sale(int productId, int quantitySold, double unitPrice) {
        this();
        this.productId = productId;
        this.quantitySold = quantitySold;
        this.unitPrice = unitPrice;
        this.totalPrice = quantitySold * unitPrice;
    }

    // Full constructor for loading existing sales
    public Sale(int id, int productId, String productName, int quantitySold,
                double unitPrice, Timestamp saleDate) {
        this(productId, quantitySold, unitPrice);
        this.id = id;
        this.productName = productName;
        this.saleDate = saleDate;
    }

    // Getters and setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getProductId() {
        return productId;
    }

    public void setProductId(int productId) {
        this.productId = productId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public int getQuantitySold() {
        return quantitySold;
    }

    public void setQuantitySold(int quantitySold) {
        this.quantitySold = quantitySold;
        this.totalPrice = this.quantitySold * this.unitPrice;
    }

    public double getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(double unitPrice) {
        this.unitPrice = unitPrice;
        this.totalPrice = this.quantitySold * unitPrice;
    }

    public double getTotalPrice() {
        return totalPrice;
    }

    public Timestamp getSaleDate() {
        return saleDate;
    }

    public void setSaleDate(Timestamp saleDate) {
        this.saleDate = saleDate;
    }

    @Override
    public String toString() {
        return "Sale{" +
                "id=" + id +
                ", productId=" + productId +
                ", productName='" + productName + '\'' +
                ", quantitySold=" + quantitySold +
                ", unitPrice=" + unitPrice +
                ", totalPrice=" + totalPrice +
                ", saleDate=" + saleDate +
                '}';
    }
}