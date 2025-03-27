package org.inventorymanagementsystem;


class InventoryReport {
    private int totalProducts;
    private double totalValue;
    private int lowStockItems;
    private int outOfStockItems;

    // Getters and setters
    public int getTotalProducts() { return totalProducts; }
    public void setTotalProducts(int totalProducts) { this.totalProducts = totalProducts; }
    public double getTotalValue() { return totalValue; }
    public void setTotalValue(double totalValue) { this.totalValue = totalValue; }
    public int getLowStockItems() { return lowStockItems; }
    public void setLowStockItems(int lowStockItems) { this.lowStockItems = lowStockItems; }
    public int getOutOfStockItems() { return outOfStockItems; }
    public void setOutOfStockItems(int outOfStockItems) { this.outOfStockItems = outOfStockItems; }
}
