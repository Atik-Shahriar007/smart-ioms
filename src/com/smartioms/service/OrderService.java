package com.smartioms.service;
import com.smartioms.model.Order;
import java.util.*;

public class OrderService {
    private List<Order> orders = new ArrayList<>();
    public OrderService(ProductService ps) {}
    public int getNextOrderId() { return orders.size() + 1; }
    public void placeOrder(Order order, int customerId) {
        orders.add(order);
        System.out.println("Order recorded for Customer ID: " + customerId);
    }
    public List<Order> getAllOrders() { return orders; }
}