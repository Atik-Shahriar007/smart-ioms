package com.smartioms.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class Invoice {
    private int invoiceId;
    private int orderId;
    private List<OrderItem> items;
    private double totalAmount;
    private double tax; 
    private double serviceCharge; 
    private LocalDateTime timestamp;

    public Invoice(int invoiceId, int orderId, List<OrderItem> items) {
        this.invoiceId = invoiceId;
        this.orderId = orderId;
        this.items = items;
        this.totalAmount = calculateTotal();
        this.tax = totalAmount * 0.05;
        this.serviceCharge = totalAmount * 0.02;
        this.timestamp = LocalDateTime.now();
    }

    private double calculateTotal() {
        double sum = 0;
        for (OrderItem item : items) {
            // Fix: Use getSellingPrice() to resolve the "cannot find symbol" error
            sum += item.getProduct().getSellingPrice() * item.getQuantity();
        }
        return sum;
    }

    public void printInvoice() {
        System.out.println("\n========== INVOICE ==========");
        System.out.println("Invoice ID: " + invoiceId);
        System.out.println("Order ID: " + orderId);
        System.out.println("Date: " + timestamp.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        System.out.println("------------------------------");
        System.out.printf("%-20s %-10s %-10s\n", "Product", "Qty", "Price");

        for (OrderItem item : items) {
            // Fix: Update to getSellingPrice() here as well
            System.out.printf("%-20s %-10d %-10.2f\n",
                    item.getProduct().getName(),
                    item.getQuantity(),
                    item.getProduct().getSellingPrice() * item.getQuantity());
        }

        System.out.println("------------------------------");
        System.out.printf("Subtotal: %.2f\n", totalAmount);
        System.out.printf("Tax (5%%): %.2f\n", tax);
        System.out.printf("Service Charge (2%%): %.2f\n", serviceCharge);
        // Using green text for the final total to match your theme
        System.out.printf("\u001B[32mTotal Amount: %.2f\u001B[0m\n", totalAmount + tax + serviceCharge);
        System.out.println("==============================\n");
    }

    public int getInvoiceId() { return invoiceId; }
    public int getOrderId() { return orderId; }
    public double getTotalAmount() { return totalAmount + tax + serviceCharge; }
}