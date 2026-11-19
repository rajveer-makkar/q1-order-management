package com.example.ordermanagement;

import com.example.ordermanagement.service.OrderService;
import com.example.ordermanagement.util.InputValidator;

import java.util.Scanner;

/** Console entry point: shows the menu, reads input and delegates to OrderService. */
public class Main {

    public static void main(String[] args) {
        OrderService orderService = new OrderService();

        try (Scanner scanner = new Scanner(System.in)) {
            boolean running = true;
            while (running) {
                printMenu();
                int choice = InputValidator.readInt(scanner, "Enter your choice: ");

                switch (choice) {
                    case 1:
                        System.out.println(orderService.insertSampleOrders());
                        break;
                    case 2:
                        String status = InputValidator.readNonEmptyString(scanner, "Enter status: ");
                        System.out.println(orderService.searchOrders(status));
                        break;
                    case 3:
                        int orderId = InputValidator.readInt(scanner, "Enter order ID: ");
                        int extra = InputValidator.readPositiveInt(scanner, "Additional quantity: ");
                        System.out.println(orderService.increaseQuantity(orderId, extra));
                        break;
                    case 4:
                        int idForStatus = InputValidator.readInt(scanner, "Enter order ID: ");
                        String newStatus = InputValidator.readNonEmptyString(
                                scanner, "New status (Pending/Shipped/Delivered): ");
                        System.out.println(orderService.updateOrderStatus(idForStatus, newStatus));
                        break;
                    case 5:
                        System.out.println(orderService.getTotalOrderValue());
                        break;
                    case 6:
                        int idForDelete = InputValidator.readInt(scanner, "Enter order ID to delete: ");
                        System.out.println(orderService.deleteCancelledOrder(idForDelete));
                        break;
                    case 7:
                        System.out.println(orderService.getAllOrders());
                        break;
                    case 8:
                        running = false;
                        System.out.println("Thank you. Exiting the application.");
                        break;
                    default:
                        System.out.println("Invalid choice. Please select 1 to 8.");
                }
                System.out.println();
            }
        }
    }

    private static void printMenu() {
        System.out.println("===== ORDER MANAGEMENT SYSTEM =====");
        System.out.println("1. Insert sample orders");
        System.out.println("2. Search orders by status and quantity");
        System.out.println("3. Increase order quantity");
        System.out.println("4. Update order status");
        System.out.println("5. Display total order value");
        System.out.println("6. Delete a cancelled order");
        System.out.println("7. Display all orders");
        System.out.println("8. Exit");
    }
}
