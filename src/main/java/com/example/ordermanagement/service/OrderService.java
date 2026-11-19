package com.example.ordermanagement.service;

import com.example.ordermanagement.dao.OrderDAO;
import com.example.ordermanagement.model.Order;
import com.example.ordermanagement.util.InputValidator;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/**
 * Business logic layer: it validates rules, calls the DAO and prepares the
 * message shown to the user. It never writes SQL of its own.
 */
public class OrderService {

    // Four sample orders: the fourth is Cancelled so option 6 can be demonstrated.
    private static final List<Order> SAMPLE_ORDERS = List.of(
            new Order(101, "Aarav Sharma", "Laptop", 2, 55000.0, "Pending"),
            new Order(102, "Priya Verma", "Mouse", 5, 799.0, "Shipped"),
            new Order(103, "Rohan Gupta", "Keyboard", 4, 1999.0, "Delivered"),
            new Order(104, "Neha Singh", "Monitor", 3, 12000.0, "Cancelled"));

    private final OrderDAO orderDAO = new OrderDAO();

    /** Operation A: insert the sample rows and report each result separately. */
    public String insertSampleOrders() {
        StringBuilder result = new StringBuilder();
        for (Order order : SAMPLE_ORDERS) {
            try {
                if (orderDAO.insert(order)) {
                    result.append("Inserted order ").append(order.getOrderId()).append(".\n");
                } else {
                    result.append("Order ").append(order.getOrderId())
                            .append(" skipped: duplicate order ID.\n");
                }
            } catch (SQLException ex) {
                result.append("Database error while inserting order ")
                        .append(order.getOrderId()).append(": ").append(ex.getMessage()).append("\n");
            }
        }
        return result.toString().trim();
    }

    /** Operation B: status filter is done in SQL together with quantity > 2. */
    public String searchOrders(String status) {
        try {
            List<Order> orders = orderDAO.findByStatusAndMinimumQuantity(status, 2);
            if (orders.isEmpty()) {
                return "No orders found with status '" + status + "' and quantity greater than 2.";
            }
            return formatOrders(orders);
        } catch (SQLException ex) {
            return "Database error: " + ex.getMessage();
        }
    }

    /** Operation C: reject bad input, then add the amount and re-read the row. */
    public String increaseQuantity(int orderId, int extraQuantity) {
        if (extraQuantity <= 0) {
            return "Invalid additional quantity. It must be greater than 0.";
        }
        try {
            if (!orderDAO.updateQuantity(orderId, extraQuantity)) {
                return "Order " + orderId + " does not exist.";
            }
            Order updated = orderDAO.findById(orderId).orElse(null);
            return "Quantity increased by " + extraQuantity + ". Updated order: " + updated;
        } catch (SQLException ex) {
            return "Database error: " + ex.getMessage();
        }
    }

    /** Operation D: retrieve first, check the status whitelist, then update. */
    public String updateOrderStatus(int orderId, String newStatus) {
        try {
            // The examination requires the order to be retrieved before any status change.
            Optional<Order> existing = orderDAO.findById(orderId);
            if (existing.isEmpty()) {
                return "Order " + orderId + " does not exist.";
            }
            if (!InputValidator.isValidStatus(newStatus)) {
                return "Invalid status '" + newStatus + "'. Allowed values: Pending, Shipped, Delivered.";
            }
            if (!orderDAO.updateStatus(orderId, newStatus)) {
                return "Order " + orderId + " could not be updated.";
            }
            Order updated = orderDAO.findById(orderId).orElse(existing.get());
            return "Status updated. Updated order: " + updated;
        } catch (SQLException ex) {
            return "Database error: " + ex.getMessage();
        }
    }

    /** Operation E (first part): SUM(quantity * unit_price) is calculated by MySQL. */
    public String getTotalOrderValue() {
        try {
            return String.format("Total order value: %.2f", orderDAO.getTotalValue());
        } catch (SQLException ex) {
            return "Database error: " + ex.getMessage();
        }
    }

    /** Operation E (second part): delete only when the stored status is Cancelled. */
    public String deleteCancelledOrder(int orderId) {
        try {
            double total = orderDAO.getTotalValue();
            String prefix = String.format("Total order value: %.2f%n", total);
            Optional<Order> found = orderDAO.findById(orderId);

            if (found.isEmpty()) {
                return prefix + "Order " + orderId + " does not exist; nothing was deleted.";
            }
            Order order = found.get();
            if (!"Cancelled".equalsIgnoreCase(order.getStatus())) {
                return prefix + "Order " + orderId + " cannot be deleted because its status is "
                        + order.getStatus() + ", not Cancelled.";
            }
            if (orderDAO.deleteById(orderId)) {
                return prefix + "Order " + orderId + " was deleted successfully.";
            }
            return prefix + "Order " + orderId + " could not be deleted.";
        } catch (SQLException ex) {
            return "Database error: " + ex.getMessage();
        }
    }

    /** Extra menu option: show every stored order. */
    public String getAllOrders() {
        try {
            List<Order> orders = orderDAO.findAll();
            return orders.isEmpty() ? "No orders in the database." : formatOrders(orders);
        } catch (SQLException ex) {
            return "Database error: " + ex.getMessage();
        }
    }

    private String formatOrders(List<Order> orders) {
        StringBuilder table = new StringBuilder();
        table.append(String.format("%-8s %-20s %-18s %-6s %-12s %s%n",
                "ID", "CUSTOMER", "PRODUCT", "QTY", "PRICE", "STATUS"));
        for (Order order : orders) {
            table.append(String.format("%-8d %-20s %-18s %-6d %-12.2f %s%n",
                    order.getOrderId(), order.getCustomerName(), order.getProductName(),
                    order.getQuantity(), order.getUnitPrice(), order.getStatus()));
        }
        return table.toString().trim();
    }
}
