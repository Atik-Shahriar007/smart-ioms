package com.smartioms.model;
import java.util.List;

public class Order {
    private int orderId;
    private int customerId;
    private List<OrderItem> items;
    private String paymentMethod;
    private double totalAmount;

    public Order(int orderId, int customerId, List<OrderItem> items, String paymentMethod, double totalAmount) {
        this.orderId = orderId;
        this.customerId = customerId;
        this.items = items;
        this.paymentMethod = paymentMethod;
        this.totalAmount = totalAmount;
    }

    public int getOrderId() { return orderId; }
    public int getCustomerId() { return customerId; }
    public List<OrderItem> getItems() { return items; }
    public String getPaymentMethod() { return paymentMethod; }
    public double getTotalAmount() { return totalAmount; }
}