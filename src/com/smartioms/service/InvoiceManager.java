package com.smartioms.service;

import com.smartioms.model.Order;
import com.smartioms.model.OrderItem;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class InvoiceManager {
    public void printInvoice(Order order) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        System.out.println("\n" + "=".repeat(50));
        System.out.println("            OFFICIAL INVOICE");
        System.out.println("          SMART-IOMS SYSTEM");
        System.out.println("=".repeat(50));
        System.out.println("Date       : " + timestamp);
        System.out.printf("Order ID   : #%d%n", order.getOrderId());
        System.out.printf("Customer ID: %d%n", order.getCustomerId());
        System.out.println("-".repeat(50));

        System.out.printf("%-20s %-6s %-10s %-10s%n", "Item", "Qty", "Price", "Subtotal");
        System.out.println("-".repeat(50));

        double subtotal = 0;
        for (OrderItem item : order.getItems()) {
            double lineTotal = item.getProduct().getSellingPrice() * item.getQuantity();
            subtotal += lineTotal;
            System.out.printf("%-20s %-6d $%-9.2f $%-9.2f%n",
                    item.getProduct().getName(),
                    item.getQuantity(),
                    item.getProduct().getSellingPrice(),
                    lineTotal);
        }

        System.out.println("-".repeat(50));
        System.out.printf("Subtotal       : $%.2f%n", subtotal);

        // Show discount if applicable
        double finalAmount = order.getTotalAmount();
        if (finalAmount < subtotal) {
            System.out.printf("\u001B[32mDiscount       : -$%.2f\u001B[0m%n", subtotal - finalAmount);
        }

        System.out.println("=".repeat(50));
        System.out.printf("\u001B[33mPAYMENT METHOD : %s\u001B[0m%n", order.getPaymentMethod());
        System.out.printf("\u001B[32mTOTAL PAID     : $%.2f\u001B[0m%n", finalAmount);
        System.out.println("=".repeat(50));
        System.out.println("     Thank you for your purchase!");
        System.out.println("=".repeat(50) + "\n");
    }
}
