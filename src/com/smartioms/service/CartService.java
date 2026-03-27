package com.smartioms.service;

import com.smartioms.model.CartItem;
import com.smartioms.model.Product;

import java.util.ArrayList;
import java.util.List;

public class CartService {
    private List<CartItem> cart = new ArrayList<>();

    public void addToCart(Product product, int quantity) {
        for (CartItem item : cart) {
            if (item.getProduct().getId() == product.getId()) {
                item.setQuantity(item.getQuantity() + quantity);
                return;
            }
        }
        cart.add(new CartItem(product, quantity));
    }

    public void removeFromCart(int productId) {
        cart.removeIf(item -> item.getProduct().getId() == productId);
    }

    public void updateQuantity(int productId, int quantity) {
        for (CartItem item : cart) {
            if (item.getProduct().getId() == productId) {
                item.setQuantity(quantity);
                return;
            }
        }
    }

    public List<CartItem> getCartItems() {
        return cart;
    }

    public double getTotalAmount() {
        return cart.stream().mapToDouble(CartItem::getTotalPrice).sum();
    }

    public void clearCart() {
        cart.clear();
    }

    public boolean isEmpty() {
        return cart.isEmpty();
    }
}