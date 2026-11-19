package com.example.ordermanagement.dao;

import com.example.ordermanagement.database.DBConnection;
import com.example.ordermanagement.model.Order;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * All JDBC code lives here. Every method opens and closes its own resources
 * using try-with-resources, and every SQL statement uses ? placeholders.
 */
public class OrderDAO {

    private static final String INSERT_ORDER =
            "INSERT INTO Orders (order_id, customer_name, product_name, quantity, unit_price, status) "
                    + "VALUES (?, ?, ?, ?, ?, ?)";

    // ? for status and ? for the minimum quantity; ORDER BY puts the highest price first.
    private static final String SELECT_BY_STATUS =
            "SELECT order_id, customer_name, product_name, quantity, unit_price, status "
                    + "FROM Orders WHERE status = ? AND quantity > ? ORDER BY unit_price DESC";

    private static final String SELECT_BY_ID =
            "SELECT order_id, customer_name, product_name, quantity, unit_price, status "
                    + "FROM Orders WHERE order_id = ?";

    // quantity = quantity + ? adds the supplied amount to the stored value in the database.
    private static final String UPDATE_QUANTITY =
            "UPDATE Orders SET quantity = quantity + ? WHERE order_id = ?";

    private static final String UPDATE_STATUS =
            "UPDATE Orders SET status = ? WHERE order_id = ?";

    // COALESCE returns 0 when the table is empty, so getDouble never sees NULL.
    private static final String TOTAL_VALUE =
            "SELECT COALESCE(SUM(quantity * unit_price), 0) FROM Orders";

    private static final String DELETE_BY_ID =
            "DELETE FROM Orders WHERE order_id = ?";

    private static final String SELECT_ALL =
            "SELECT order_id, customer_name, product_name, quantity, unit_price, status "
                    + "FROM Orders ORDER BY order_id";

    /** Returns false when the primary key already exists. */
    public boolean insert(Order order) throws SQLException {
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(INSERT_ORDER)) {
            statement.setInt(1, order.getOrderId());
            statement.setString(2, order.getCustomerName());
            statement.setString(3, order.getProductName());
            statement.setInt(4, order.getQuantity());
            statement.setDouble(5, order.getUnitPrice());
            statement.setString(6, order.getStatus());

            try {
                statement.executeUpdate();
                return true;
            } catch (SQLIntegrityConstraintViolationException ex) {
                // A duplicate order_id violates the PRIMARY KEY constraint.
                return false;
            }
        }
    }

    /** Parameterized query: user text is data, never part of the SQL string. */
    public List<Order> findByStatusAndMinimumQuantity(String status, int minimumQuantity)
            throws SQLException {
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(SELECT_BY_STATUS)) {
            statement.setString(1, status);
            statement.setInt(2, minimumQuantity);

            List<Order> orders = new ArrayList<>();
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    orders.add(mapRow(resultSet));
                }
            }
            return orders;
        }
    }

    public Optional<Order> findById(int orderId) throws SQLException {
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(SELECT_BY_ID)) {
            statement.setInt(1, orderId);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? Optional.of(mapRow(resultSet)) : Optional.empty();
            }
        }
    }

    /** Zero affected rows means no order had that ID. */
    public boolean updateQuantity(int orderId, int extraQuantity) throws SQLException {
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(UPDATE_QUANTITY)) {
            statement.setInt(1, extraQuantity);
            statement.setInt(2, orderId);
            return statement.executeUpdate() > 0;
        }
    }

    public boolean updateStatus(int orderId, String newStatus) throws SQLException {
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(UPDATE_STATUS)) {
            statement.setString(1, newStatus);
            statement.setInt(2, orderId);
            return statement.executeUpdate() > 0;
        }
    }

    public double getTotalValue() throws SQLException {
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(TOTAL_VALUE);
             ResultSet resultSet = statement.executeQuery()) {
            resultSet.next();
            return resultSet.getDouble(1);
        }
    }

    public boolean deleteById(int orderId) throws SQLException {
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(DELETE_BY_ID)) {
            statement.setInt(1, orderId);
            return statement.executeUpdate() > 0;
        }
    }

    public List<Order> findAll() throws SQLException {
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(SELECT_ALL);
             ResultSet resultSet = statement.executeQuery()) {
            List<Order> orders = new ArrayList<>();
            while (resultSet.next()) {
                orders.add(mapRow(resultSet));
            }
            return orders;
        }
    }

    // Converts the current ResultSet row into a Java object using column names.
    private Order mapRow(ResultSet resultSet) throws SQLException {
        return new Order(
                resultSet.getInt("order_id"),
                resultSet.getString("customer_name"),
                resultSet.getString("product_name"),
                resultSet.getInt("quantity"),
                resultSet.getDouble("unit_price"),
                resultSet.getString("status"));
    }
}
