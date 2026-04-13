
package com.smartioms.service;

import com.smartioms.model.Product;
import java.util.Scanner;

public class SupplierService {
    private final ProductService productService;

    public SupplierService(ProductService ps) {
        this.productService = ps;
    }

    public void handleStockRequest(Scanner sc) {
        System.out.print("Enter Product ID: ");
        int id = sc.nextInt(); sc.nextLine();

        Product p = productService.getProductById(id);
        if (p == null) { System.out.println("Product not found!"); return; }

        System.out.printf("'%s' current stock: W1=%d, W2=%d, W3=%d (Total=%d)%n",
            p.getName(), p.getW1Stock(), p.getW2Stock(), p.getW3Stock(), p.getStock());

        System.out.print("Enter Quantity to add: ");
        int qty = sc.nextInt(); sc.nextLine();

        System.out.print("Store in which Warehouse (1/2/3): ");
        int wNum = sc.nextInt(); sc.nextLine();
        if (wNum < 1 || wNum > 3) {
            System.out.println("\u001B[31mInvalid warehouse! Use 1, 2, or 3.\u001B[0m");
            return;
        }

        System.out.print("Payment Type (C for Cash / L for Loan): ");
        String type = sc.nextLine().toUpperCase();

        double restockCost = p.getCostPrice() * qty;

        if (type.startsWith("L")) {
            p.setPaymentType("LOAN");
            p.setLoanAmount(p.getLoanAmount() + restockCost);
        } else {
            if (p.getLoanAmount() <= 0) p.setPaymentType("CASH");
        }

        // Add stock to the liked warehouse
        p.setWarehouseStock(wNum, p.getWarehouseStock(wNum) + qty);
        productService.saveProducts();
        productService.rebuildLowStockAlerts();

        System.out.printf("\u001B[32mAdded %d units to Warehouse %d (%s)\u001B[0m%n", qty, wNum,
            type.startsWith("L") ? "LOAN - $" + String.format("%.2f", restockCost) + " added to debt" : "CASH");
        System.out.printf("New stock: W1=%d, W2=%d, W3=%d (Total=%d)%n",
            p.getW1Stock(), p.getW2Stock(), p.getW3Stock(), p.getStock());
    }
}
