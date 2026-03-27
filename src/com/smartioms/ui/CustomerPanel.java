package com.smartioms.ui;

import com.smartioms.model.*;
import com.smartioms.service.*;
import java.util.*;

public class CustomerPanel {
    private final ProductService productService;
    private final CartService cartService = new CartService();
    private final OrderService orderService;
    private final InvoiceManager invoiceManager;
    private final UserService userService;
    private final User customer;
    private final Scanner sc;

    public CustomerPanel(ProductService ps, OrderService os, InvoiceManager im,
                         UserService us, User user, Scanner sc) {
        this.productService = ps; this.orderService = os;
        this.invoiceManager = im; this.userService = us;
        this.customer = user; this.sc = sc;
    }

    private int readInt(String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = sc.nextLine().trim();
            try { return Integer.parseInt(input); }
            catch (NumberFormatException e) {
                System.out.println("\u001B[31mInvalid input! Enter a number.\u001B[0m");
            }
        }
    }

    public void showMenu() {
        int choice;
        do {
            ConsoleMenu.clearScreen();
            ConsoleMenu.drawHeader("\u001B[1;94mCUSTOMER STORE: " + customer.getName() + "\u001B[0m");
            if (customer.isTrusted()) {
                System.out.println("\u001B[33m*** TRUSTED CUSTOMER — 5% Loyalty Discount Active ***\u001B[0m");
            }
            System.out.printf("Visits: %d | Past Orders: %d%n%n", customer.getVisitCount(), customer.getPurchaseCount());
            System.out.println("1. Browse Products");
            System.out.println("2. Add to Cart");
            System.out.println("3. View Cart");
            System.out.println("4. Edit Cart (Change Qty / Remove)");
            System.out.println("5. Clear Entire Cart");
            System.out.println("6. Checkout");
            System.out.println("\u001B[31m7. Logout\u001B[0m");
            choice = readInt("\nSelect Option: ");

            switch (choice) {
                case 1 -> { viewProducts(); pause(); }
                case 2 -> { addToCart(); pause(); }
                case 3 -> { viewCart(); pause(); }
                case 4 -> { editCart(); pause(); }
                case 5 -> { clearCart(); pause(); }
                case 6 -> { checkout(); pause(); }
                case 7 -> {
                    ConsoleMenu.clearScreen();
                    if (customer.getSessionPurchases().isEmpty()) {
                        customer.addSessionPurchase("0");
                    }
                    userService.saveUsers();
                    System.out.println("Logging out...");
                }
                default -> System.out.println("\u001B[31mInvalid option! Choose 1-7.\u001B[0m");
            }
        } while (choice != 7);
    }

    private void pause() { System.out.println("\nPress Enter to go back..."); sc.nextLine(); }

    private void viewProducts() {
        ConsoleMenu.clearScreen();
        ConsoleMenu.drawHeader("PRODUCT CATALOG");
        System.out.printf("%-5s | %-15s | %-12s | %-10s | %-8s%n", "ID", "Name", "Category", "Price", "In Stock");
        System.out.println("-".repeat(60));
        for (Product p : productService.getAllProducts()) {
            System.out.printf("%-5d | %-15s | %-12s | %-10.2f | %-8d%n",
                p.getId(), p.getName(), p.getCategory(), p.getSellingPrice(), p.getStock());
        }
    }

    private void addToCart() {
        ConsoleMenu.clearScreen();
        ConsoleMenu.drawHeader("ADD TO CART");
        viewProducts();
        int id = readInt("\nProduct ID (0 to cancel): ");
        if (id == 0) { System.out.println("Cancelled."); return; }
        Product p = productService.getProductById(id);
        if (p == null) { System.out.println("\u001B[31mProduct not found!\u001B[0m"); return; }
        if (p.getStock() <= 0) {
            System.out.println("\u001B[31mSorry, this product is out of stock!\u001B[0m");
            return;
        }
        System.out.printf("'%s' — $%.2f | Available: %d%n", p.getName(), p.getSellingPrice(), p.getStock());
        int q = readInt("Quantity (0 to cancel): ");
        if (q == 0) { System.out.println("Cancelled."); return; }
        if (q < 0) {
            System.out.println("\u001B[31mQuantity cannot be negative!\u001B[0m");
            return;
        }
        if (q > p.getStock()) {
            System.out.println("\u001B[31mInsufficient stock! Only " + p.getStock() + " available.\u001B[0m"); return;
        }
        cartService.addToCart(p, q);
        System.out.printf("\u001B[32mAdded %d x '%s' ($%.2f)\u001B[0m%n", q, p.getName(), p.getSellingPrice() * q);
        showCartSummary();
    }

    private void viewCart() {
        ConsoleMenu.clearScreen();
        ConsoleMenu.drawHeader("YOUR SHOPPING CART");
        if (cartService.isEmpty()) { System.out.println("Cart is empty."); return; }
        printCartTable();
    }

    private void printCartTable() {
        System.out.printf("%-4s %-5s %-15s %-8s %-12s %-12s%n", "#", "ID", "Product", "Qty", "Unit Price", "Subtotal");
        System.out.println("-".repeat(60));
        int idx = 1;
        for (CartItem item : cartService.getCartItems()) {
            System.out.printf("%-4d %-5d %-15s %-8d $%-11.2f $%-11.2f%n",
                idx++, item.getProduct().getId(), item.getProduct().getName(), item.getQuantity(),
                item.getProduct().getSellingPrice(), item.getTotalPrice());
        }
        System.out.println("-".repeat(60));
        System.out.printf("\u001B[32mCart Total: $%.2f | Items: %d\u001B[0m%n",
            cartService.getTotalAmount(), cartService.getCartItems().size());
    }

    private void showCartSummary() {
        System.out.printf("Cart: %d item(s) | $%.2f%n", cartService.getCartItems().size(), cartService.getTotalAmount());
    }

    private void editCart() {
        ConsoleMenu.clearScreen();
        ConsoleMenu.drawHeader("EDIT CART");
        if (cartService.isEmpty()) { System.out.println("Cart is empty."); return; }
        printCartTable();
        System.out.println("\n1. Change quantity\n2. Remove item\n3. Cancel");
        int action = readInt("Choice: ");
        if (action == 3 || (action != 1 && action != 2)) { System.out.println("Cancelled."); return; }

        int pid = readInt("Product ID (0 to cancel): ");
        if (pid == 0) { System.out.println("Cancelled."); return; }
        CartItem target = null;
        for (CartItem item : cartService.getCartItems())
            if (item.getProduct().getId() == pid) { target = item; break; }
        if (target == null) { System.out.println("\u001B[31mNot in cart!\u001B[0m"); return; }

        if (action == 1) {
            System.out.printf("Current qty: %d%n", target.getQuantity());
            int newQty = readInt("New quantity (0 to remove): ");
            if (newQty == 0) { cartService.removeFromCart(pid); System.out.println("\u001B[32mRemoved!\u001B[0m"); }
            else if (newQty < 0) {
                System.out.println("\u001B[31mQuantity cannot be negative!\u001B[0m");
            }
            else if (newQty > target.getProduct().getStock()) {
                System.out.println("\u001B[31mExceeds stock (" + target.getProduct().getStock() + ")!\u001B[0m");
            } else {
                cartService.updateQuantity(pid, newQty);
                System.out.printf("\u001B[32mUpdated to %d\u001B[0m%n", newQty);
            }
        } else {
            cartService.removeFromCart(pid);
            System.out.println("\u001B[32mRemoved!\u001B[0m");
        }
        if (!cartService.isEmpty()) showCartSummary();
        else System.out.println("Cart is now empty.");
    }

    private void clearCart() {
        if (cartService.isEmpty()) { System.out.println("Already empty."); return; }
        System.out.printf("Clear %d item(s) ($%.2f)? (Y/N): ",
            cartService.getCartItems().size(), cartService.getTotalAmount());
        if (sc.nextLine().toUpperCase().startsWith("Y")) {
            cartService.clearCart();
            System.out.println("\u001B[32mCart cleared!\u001B[0m");
        } else System.out.println("Not cleared.");
    }

    // ========================== CHECKOUT ==========================
    private void checkout() {
        ConsoleMenu.clearScreen();
        ConsoleMenu.drawHeader("CHECKOUT");
        if (cartService.isEmpty()) { System.out.println("Cart is empty!"); return; }
        printCartTable();

        System.out.println("\n1. Proceed to Payment\n2. Cancel (go back)");
        int confirm = readInt("Choice: ");
        if (confirm != 1) { System.out.println("\u001B[33mCheckout cancelled.\u001B[0m"); return; }

        double total = cartService.getTotalAmount();
        boolean isFirstOrder = (customer.getPurchaseCount() == 0);
        boolean highValue = (total > 50000);

        // Loyalty discount for trusted customers (>5 orders)
        double loyaltyDiscount = customer.getLoyaltyDiscount();

        // Payment method
        String paymentMethod = selectPaymentMethod(isFirstOrder, highValue);
        if (paymentMethod == null) return; // cancelled

        // Apply loyalty discount if applicable
        if (loyaltyDiscount > 0) {
            double loyaltyAmt = total * loyaltyDiscount;
            total -= loyaltyAmt;
            System.out.printf("\u001B[32mLoyalty discount (5%%): -$%.2f\u001B[0m%n", loyaltyAmt);
        }

        System.out.printf("\u001B[33mFinal Amount: $%.2f | Payment: %s\u001B[0m%n", total, paymentMethod);
        System.out.print("Confirm purchase? (Y/N): ");
        if (!sc.nextLine().toUpperCase().startsWith("Y")) {
            System.out.println("\u001B[33mPurchase cancelled.\u001B[0m"); return;
        }

        // Process
        List<OrderItem> orderItems = new ArrayList<>();
        List<int[]> deductions = new ArrayList<>();
        for (CartItem item : cartService.getCartItems()) {
            Product p = item.getProduct();
            orderItems.add(new OrderItem(p, item.getQuantity()));
            deductions.add(new int[]{ p.getId(), item.getQuantity() });
            customer.addSessionPurchase(p.getName() + " x" + item.getQuantity() + " = $" +
                String.format("%.2f", item.getTotalPrice()));
        }

        Order order = new Order(orderService.getNextOrderId(), customer.getUserId(),
            orderItems, paymentMethod, total);
        orderService.placeOrder(order, customer.getUserId());
        for (int[] d : deductions) productService.deductStockForSale(d[0], d[1]);
        productService.recordSale(total);
        userService.recordPurchase(customer);
        invoiceManager.printInvoice(order);
        cartService.clearCart();
        System.out.println("\u001B[32mPurchase complete! Thank you for shopping.\u001B[0m");
    }

    // ========================== PAYMENT SELECTION ==========================
    private String selectPaymentMethod(boolean isFirstOrder, boolean highValue) {
        while (true) {
            ConsoleMenu.clearScreen();
            ConsoleMenu.drawHeader("SELECT PAYMENT METHOD");
            System.out.printf("Order Total: $%.2f%n", cartService.getTotalAmount());
            if (isFirstOrder && highValue) {
                System.out.println("\u001B[33mFirst order over $50,000 — Cash is not available.\u001B[0m");
            }
            System.out.println();
            System.out.println("1. Visa");
            System.out.println("2. MasterCard");
            System.out.println("3. American Express");
            System.out.println("4. bKash");
            System.out.println("5. Nagad");
            System.out.println("6. Rocket");

            if (!(isFirstOrder && highValue)) {
                System.out.println("7. Cash");
            } else {
                System.out.println("\u001B[90m7. Cash (BLOCKED)\u001B[0m");
            }
            System.out.println("8. Cancel");

            int pay = readInt("\nChoice: ");

            switch (pay) {
                case 1 -> { String r = processCardPayment("Visa"); if (r != null) return r; }
                case 2 -> { String r = processCardPayment("MasterCard"); if (r != null) return r; }
                case 3 -> { String r = processCardPayment("American Express"); if (r != null) return r; }
                case 4 -> { String r = processMobilePayment("bKash"); if (r != null) return r; }
                case 5 -> { String r = processMobilePayment("Nagad"); if (r != null) return r; }
                case 6 -> { String r = processMobilePayment("Rocket"); if (r != null) return r; }
                case 7 -> {
                    ConsoleMenu.clearScreen();
                    if (isFirstOrder && highValue) {
                        System.out.println("\u001B[31mSorry, we can't process this order via Cash.\u001B[0m");
                        System.out.println("\u001B[31mPlease pay via card or mobile payment for first orders over $50,000.\u001B[0m");
                        System.out.println("\n1. Go back to payment methods\n2. Cancel checkout");
                        int ch = readInt("Choice: ");
                        if (ch == 2) return null;
                    } else {
                        return "Cash";
                    }
                }
                case 8 -> {
                    System.out.println("\u001B[33mPayment cancelled.\u001B[0m");
                    return null;
                }
                default -> System.out.println("\u001B[31mInvalid! Choose 1-8.\u001B[0m");
            }
        }
    }

    private String processCardPayment(String cardType) {
        ConsoleMenu.clearScreen();
        ConsoleMenu.drawHeader(cardType + " PAYMENT");
        System.out.printf("Amount: $%.2f%n%n", cartService.getTotalAmount());
        System.out.print("Card Number (16 digits): "); String cardNum = sc.nextLine().trim();
        if (cardNum.length() < 12) { System.out.println("\u001B[31mInvalid card!\u001B[0m"); pause(); return null; }
        System.out.print("Cardholder Name: "); String name = sc.nextLine().trim();
        if (name.isEmpty()) { System.out.println("\u001B[31mName required!\u001B[0m"); pause(); return null; }
        System.out.print("Expiry (MM/YY): "); String expiry = sc.nextLine().trim();
        System.out.print("CVV (3 digits): "); String cvv = sc.nextLine().trim();
        if (cvv.length() != 3) { System.out.println("\u001B[31mInvalid CVV!\u001B[0m"); pause(); return null; }

        String masked = "****-****-****-" + cardNum.substring(cardNum.length() - 4);
        System.out.println("\n" + "-".repeat(40));
        System.out.printf("Card: %s (%s)%nName: %s%nExpiry: %s%nAmount: $%.2f%n",
            masked, cardType, name, expiry, cartService.getTotalAmount());
        System.out.println("-".repeat(40));
        System.out.println("\n1. Pay\n2. Cancel");
        int ch = readInt("Choice: ");
        if (ch == 1) {
            System.out.println("\u001B[32mPayment successful!\u001B[0m");
            return cardType + " (" + masked + ")";
        }
        System.out.println("\u001B[33mPayment cancelled.\u001B[0m"); pause(); return null;
    }

    private String processMobilePayment(String provider) {
        ConsoleMenu.clearScreen();
        ConsoleMenu.drawHeader(provider + " PAYMENT");
        System.out.printf("Amount: $%.2f%n%n", cartService.getTotalAmount());
        System.out.print(provider + " Number: "); String num = sc.nextLine().trim();
        if (num.length() < 11) { System.out.println("\u001B[31mInvalid number!\u001B[0m"); pause(); return null; }
        System.out.print(provider + " PIN: "); String pin = sc.nextLine().trim();
        if (pin.length() < 4) { System.out.println("\u001B[31mInvalid PIN!\u001B[0m"); pause(); return null; }
        System.out.print("OTP (check " + provider + " app): "); String otp = sc.nextLine().trim();
        if (otp.length() < 4) { System.out.println("\u001B[31mInvalid OTP!\u001B[0m"); pause(); return null; }

        String masked = num.substring(0, 3) + "****" + num.substring(num.length() - 4);
        System.out.println("\n" + "-".repeat(40));
        System.out.printf("Provider: %s%nNumber: %s%nAmount: $%.2f%n", provider, masked, cartService.getTotalAmount());
        System.out.println("-".repeat(40));
        System.out.println("\n1. Pay\n2. Cancel");
        int ch = readInt("Choice: ");
        if (ch == 1) {
            System.out.println("\u001B[32mPayment successful!\u001B[0m");
            return provider + " (" + masked + ")";
        }
        System.out.println("\u001B[33mPayment cancelled.\u001B[0m"); pause(); return null;
    }
}
