package com.smartioms;

import com.smartioms.model.User;
import com.smartioms.service.*;
import com.smartioms.ui.*;
import com.smartioms.util.ConsoleUtil;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        ProductService pService = new ProductService();
        UserService uService = new UserService();
        uService.addDefaultAdmin();

        OrderService oService = new OrderService(pService);
        InvoiceManager iManager = new InvoiceManager();

        Scanner sc = new Scanner(System.in);
        int choice = 0;

        do {
            // Clear screen on every return to main menu
            ConsoleMenu.clearScreen();
            ConsoleMenu.drawHeader("\u001B[1;32mSMART-IOMS SYSTEM\u001B[0m");
System.out.println("\u001B[32m1. Login\u001B[0m");          // Green
System.out.println("\u001B[34m2. Signup (New Customers)\u001B[0m"); // Blue
System.out.println("\u001B[31m3. Exit\u001B[0m");           // Red
            System.out.print("\nSelect Option: ");

            if (sc.hasNextInt()) {
                choice = sc.nextInt();
                sc.nextLine();
            } else {
                sc.nextLine();
                continue;
            }

            if (choice == 1) {
                System.out.print("Username: "); String u = sc.nextLine();
                String p = ConsoleUtil.readPassword("Password: ", sc);
                User user = uService.login(u, p);

                if (user != null) {
                    String role = user.getRole().toUpperCase();
                    if (role.equals("ADMIN")) {
                        new AdminPanel(user, uService, pService, sc).showMenu();
                    } else if (role.equals("STAFF")) {
                        new StaffPanel(pService, uService, sc).showMenu();
                    } else if (role.equals("CUSTOMER")) {
                        user.clearSessionPurchases();
                        new CustomerPanel(pService, oService, iManager, uService, user, sc).showMenu();
                    }
                    // Immediately clear screen after ANY panel logout
                    ConsoleMenu.clearScreen();
                } else {
                    System.out.println("\n\u001B[31mInvalid Username or Password!\u001B[0m");
                    System.out.println("Press Enter to try again...");
                    sc.nextLine();
                }

            } else if (choice == 2) {
                ConsoleMenu.clearScreen();
                ConsoleMenu.drawHeader("CUSTOMER REGISTRATION");
                System.out.print("Enter New Username: ");
                String newName = sc.nextLine();
                String newPass = ConsoleUtil.readAndValidatePassword("Enter New Password: ", sc);

                boolean ok = uService.signup(newName, "CUSTOMER", newPass);
                if (ok) {
                    System.out.println("\n\u001B[32mSignup Successful! You can now login.\u001B[0m");
                } else {
                    System.out.println("\n\u001B[31mSignup failed!\u001B[0m");
                }
                System.out.println("Press Enter to return to Main Menu...");
                sc.nextLine();
            }

        } while (choice != 3);

        System.out.println("\nExiting System... Thank you for using SMART-IOMS.");
        sc.close();
    }
}
