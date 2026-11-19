# Order Management System — JDBC + MySQL

CS336B Object Oriented Programming with Java, MSE-2. A menu-driven console
application that stores orders in `ecommerce_db.Orders` using JDBC.

## Architecture

```
Main (menu + Scanner)
   -> InputValidator (rejects bad numbers and empty text)
   -> OrderService (business rules and user messages)
        -> OrderDAO (all JDBC / SQL code)
             -> DBConnection (MySQL connection settings)
             -> Order (model object for one table row)
```

Only `OrderDAO` writes SQL. Every statement is a `PreparedStatement` with `?`
placeholders, and every method uses try-with-resources so connections,
statements and result sets close automatically.

## Setup

1. Install JDK 17 (or 21), Maven and MySQL 8.x.
2. Create the database and table:

   ```bash
   mysql -u root -p < database/ecommerce_db.sql
   ```

3. Open
   `src/main/java/com/example/ordermanagement/database/DBConnection.java`
   and set `USER` and `PASSWORD` to your MySQL credentials. Change the `URL`
   only if MySQL uses a different host or port.
4. Build and run:

   ```bash
   mvn package
   mvn exec:java
   ```

   Without Maven, download MySQL Connector/J 8.x, then:

   ```bash
   javac -cp "mysql-connector-j-8.x.jar" -d out $(find src/main/java -name "*.java")
   java -cp "out:mysql-connector-j-8.x.jar" com.example.ordermanagement.Main
   ```

   On Windows replace `:` in the classpath with `;`.

## Menu

1. Insert sample orders
2. Search orders by status and quantity
3. Increase order quantity
4. Update order status
5. Display total order value
6. Delete a cancelled order
7. Display all orders
8. Exit

## SQL and JDBC methods, in simple language

| Query / method | Meaning |
|---|---|
| `INSERT INTO Orders (...) VALUES (?, ?, ...)` | Adds a row. `?` values are set with `setInt`/`setString`/`setDouble`, so user text can never change the SQL command. |
| `SQLIntegrityConstraintViolationException` | Thrown by MySQL when the primary key already exists; the DAO returns `false` and the service prints a duplicate message. |
| `SELECT ... WHERE status = ? AND quantity > ? ORDER BY unit_price DESC` | Finds matching orders, keeps only quantity above 2 and sorts highest price first. |
| `UPDATE Orders SET quantity = quantity + ? WHERE order_id = ?` | Adds the supplied amount to the stored quantity in one statement. `executeUpdate()` returns 0 when the ID is absent. |
| `SELECT ... WHERE order_id = ?` | Retrieves an order before status update and before deletion. |
| `UPDATE Orders SET status = ? WHERE order_id = ?` | Changes status only after the service has checked the whitelist. |
| `SELECT COALESCE(SUM(quantity * unit_price), 0) FROM Orders` | Adds `quantity * unit_price` for every row; `COALESCE` makes an empty table return 0. |
| `DELETE FROM Orders WHERE order_id = ?` | Called only after the service confirms the stored status is `Cancelled`. |
| `try (...)` | try-with-resources closes database objects even when an exception occurs. |

Sample order 104 has status `Cancelled`, so option 6 can be demonstrated
without manually changing data in MySQL.

## Illustrative console output

The following is an example of the expected shape of the output. Actual
values depend on the current contents of your database. For the four sample
rows the total is `2 x 55000 + 5 x 799 + 4 x 1999 + 3 x 12000 = 157991.00`;
after increasing order 102 by 3 it becomes `160388.00`, and after deleting
order 104 it becomes `124388.00`.

```text
===== ORDER MANAGEMENT SYSTEM =====
1. Insert sample orders
2. Search orders by status and quantity
3. Increase order quantity
4. Update order status
5. Display total order value
6. Delete a cancelled order
7. Display all orders
8. Exit
Enter your choice: 1
Inserted order 101.
Inserted order 102.
Inserted order 103.
Inserted order 104.

===== ORDER MANAGEMENT SYSTEM =====
...
Enter your choice: 1
Order 101 skipped: duplicate order ID.
...

Enter your choice: 2
Enter status: Shipped
ID       CUSTOMER             PRODUCT            QTY    PRICE        STATUS
102      Priya Verma          Mouse              5      799.00       Shipped

Enter your choice: 3
Enter order ID: 102
Additional quantity: 3
Quantity increased by 3. Updated order: Order [id=102, customer=Priya Verma,
product=Mouse, quantity=8, unitPrice=799.0, status=Shipped]

Enter your choice: 4
Enter order ID: 101
New status (Pending/Shipped/Delivered): Shipped
Status updated. Updated order: Order [id=101, customer=Aarav Sharma,
product=Laptop, quantity=2, unitPrice=55000.0, status=Shipped]

Enter your choice: 4
Enter order ID: 999
New status (Pending/Shipped/Delivered): Shipped
Order 999 does not exist.

Enter your choice: 4
Enter order ID: 101
New status (Pending/Shipped/Delivered): Unknown
Invalid status 'Unknown'. Allowed values: Pending, Shipped, Delivered.

Enter your choice: 5
Total order value: 160388.00

Enter your choice: 6
Enter order ID to delete: 102
Total order value: 160388.00
Order 102 cannot be deleted because its status is Shipped, not Cancelled.

Enter your choice: 6
Enter order ID to delete: 104
Total order value: 160388.00
Order 104 was deleted successfully.

Enter your choice: 7
ID       CUSTOMER             PRODUCT            QTY    PRICE        STATUS
101      Aarav Sharma         Laptop             2      55000.00     Shipped
102      Priya Verma          Mouse              8      799.00       Shipped
103      Rohan Gupta          Keyboard           4      1999.00      Delivered

Enter your choice: 8
Thank you. Exiting the application.
```

## Viva points

- `Order` is a model class; `OrderDAO` is the only class that knows SQL.
- `PreparedStatement` prevents SQL injection and handles quoting automatically.
- `ResultSet.next()` moves the cursor to the next row; `getInt`/`getString`
  read columns by name.
- `executeUpdate()` returns the number of affected rows; 0 means no match.
- try-with-resources replaces an explicit `finally` block for closing resources.
- `LinkedHashMap` is not used here; data is stored in MySQL, and `ArrayList`
  holds query results temporarily in the service.
