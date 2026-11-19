package com.example.ordermanagement.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Single place that knows how to connect to the MySQL database.
 * Change the three constants below if your MySQL username or password differs.
 */
public final class DBConnection {

    // localhost:3306 is the default MySQL port; ecommerce_db is our database.
    private static final String URL =
            "jdbc:mysql://localhost:3306/ecommerce_db?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true";
    private static final String USER = "javauser";
    private static final String PASSWORD = "javauser123";

    private DBConnection() {
    }

    public static Connection getConnection() throws SQLException {
        // Connector/J 8 registers itself automatically, so Class.forName is not needed.
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}
