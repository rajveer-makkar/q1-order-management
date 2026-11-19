# Order Management System Using Java JDBC and MySQL

**Project Report for CS336B — Object Oriented Programming with Java**

## 1. Project Title and Submission Details

| Detail | Value |
|---|---|
| Examination | Mid Semester Examination-II (MSE-2), Blended, Odd Semester 2026-27 |
| Subject | Object Oriented Programming with Java (CS336B) |
| Project title | Order Management System Using Java JDBC and MySQL |
| Student name | `[Enter your name]` |
| Registration number | `[Enter your registration number]` |
| Branch | `[Enter your branch]` |
| Semester | III |
| Institution | `[Enter your institution name]` |

> **Evidence note:** This report describes code that exists in `q1-order-management/`. Compilation was executed successfully. A MySQL server was not available in the development environment, so no console session, query result or screenshot was captured. Those places are marked as placeholders and no runtime result is claimed as observed.

---

## 2. Abstract

This project implements a menu-driven Order Management System using Java JDBC and MySQL. The application connects to the `ecommerce_db` database and manages records in the `Orders` table, whose columns are `order_id`, `customer_name`, `product_name`, `quantity`, `unit_price` and `status`. It supports five examination operations: inserting sample orders with duplicate-ID handling, filtering by status and quantity with descending price sorting, increasing an order's quantity, validating and updating an order's status, and calculating total order value before conditionally deleting only cancelled orders.

The program follows a layered design: `Main` presents the menu, `InputValidator` checks console input, `OrderService` applies business rules, `OrderDAO` contains all JDBC code, `DBConnection` supplies the connection, and `Order` represents a table row. Every SQL statement uses `PreparedStatement`, and all database resources are closed with try-with-resources. The project compiles with Maven using Connector/J 8.4.0.

---

## 3. Introduction

### 3.1 Introduction to order management

An order management system records what a customer bought, how many units, the unit price and the current fulfilment status. Even a small system must answer practical questions: which orders are shipped, how many copies remain, what is the total value of all orders, and which cancelled orders can be removed.

### 3.2 Problem statement

The examination requires a console application that performs five database operations on `ecommerce_db.Orders` using JDBC, with safe parameterised SQL, proper exception handling, and a menu that repeats until the user exits. The application must reject duplicate order IDs, invalid quantities and invalid statuses, and must not delete an order that is not cancelled.

### 3.3 Objectives

1. Establish a JDBC connection to MySQL 8.x.
2. Insert at least three sample orders using `PreparedStatement`.
3. Filter orders by status and quantity greater than 2, sorted by price descending.
4. Increase an order's quantity and display the updated row.
5. Validate and update status using only Pending, Shipped and Delivered.
6. Compute `SUM(quantity * unit_price)` and delete only cancelled orders.
7. Use a layered structure with reusable classes and safe input handling.
8. Replace plain SQL string concatenation with parameterised queries.

---

## 4. Software and Hardware Requirements

| Category | Requirement |
|---|---|
| Language | Java 17 (JDK 17 or newer) |
| Runtime | Java Virtual Machine 17+ |
| Database | MySQL Server 8.x |
| JDBC | Java JDBC API (`java.sql` package, included in the JDK) |
| Driver | MySQL Connector/J 8.4.0 (declared in `pom.xml`) |
| Build tool | Apache Maven |
| IDE | IntelliJ IDEA, Eclipse or any text editor |
| Operating system | Any OS with Java and MySQL support |
| Memory / disk | 4 GB RAM and 200 MB free disk space recommended |

---

## 5. Technologies and Concepts

### 5.1 Java

Java provides the language features used throughout: classes, objects, private fields, constructors, methods, exceptions, `List`, `Optional`, `Set` and try-with-resources.

### 5.2 JDBC architecture

JDBC is a standard Java API for talking to relational databases. The driver manager loads a database-specific driver (Connector/J), obtains a `Connection`, creates a `PreparedStatement`, executes SQL and returns a `ResultSet`. The application code depends on the standard `java.sql` interfaces rather than on vendor-specific classes.

### 5.3 MySQL

MySQL stores the data permanently in the `ecommerce_db` database. It enforces the primary key and evaluates aggregate functions such as `SUM`.

### 5.4 PreparedStatement

A `PreparedStatement` is precompiled SQL with `?` placeholders. Values are supplied with `setInt`, `setString` and `setDouble`. This prevents SQL injection because user input is always treated as data, never as SQL code.

### 5.5 Statement versus PreparedStatement

| Feature | `Statement` | `PreparedStatement` |
|---|---|---|
| SQL text | Built by string concatenation | Precompiled with `?` placeholders |
| Injection risk | High if user input is concatenated | Safe, values are bound separately |
| Repeated execution | Re-parsed each time | Parsed once, reused with new values |
| Type handling | Manual quoting | `setInt`, `setString`, `setDouble` |
| Used here? | No | Yes, in every DAO method |

### 5.6 ResultSet

`ResultSet` is the cursor returned by `executeQuery()`. `next()` moves to the next row; `getInt`, `getString` and `getDouble` read columns by name. `OrderDAO.mapRow` converts one row into an `Order` object.

### 5.7 SQL queries used

| Query | Purpose |
|---|---|
| `INSERT INTO Orders (...) VALUES (?, ?, ?, ?, ?, ?)` | Add a row |
| `SELECT ... WHERE status = ? AND quantity > ? ORDER BY unit_price DESC` | Filter and sort |
| `SELECT ... WHERE order_id = ?` | Retrieve one row |
| `UPDATE Orders SET quantity = quantity + ? WHERE order_id = ?` | Increase quantity |
| `UPDATE Orders SET status = ? WHERE order_id = ?` | Change status |
| `SELECT COALESCE(SUM(quantity * unit_price), 0) FROM Orders` | Total value |
| `DELETE FROM Orders WHERE order_id = ?` | Remove a row |

### 5.8 Primary keys

`order_id` is the primary key. MySQL rejects a duplicate value automatically by throwing a constraint violation, which the DAO catches and reports as a friendly message.

### 5.9 Exception handling

DAO methods declare `throws SQLException`. `OrderService` catches it and returns a readable message instead of letting the console crash. `InputValidator` catches `NumberFormatException` so non-numeric menu input is handled.

### 5.10 Java OOP concepts

- **Encapsulation:** fields in `Order` are private and exposed through getters/setters.
- **Abstraction:** the service says "insert an order"; only the DAO knows the SQL.
- **Single responsibility:** each class has one job.
- **Reuse:** one `DBConnection` method serves every DAO operation.
- **Objects:** each table row is represented by an `Order` object.

---

## 6. System Architecture

```
  +----------------------------------------------------------+
  |                         USER                             |
  +---------------------------+------------------------------+
                              |  keyboard input
                              v
  +----------------------------------------------------------+
  | Main.java          menu loop, switch, prints results     |
  +-------+-----------------------------+--------------------+
          | uses                         | uses
          v                              v
  +---------------------+   +--------------------------------+
  | InputValidator.java |   | OrderService.java              |
  | readInt             |   | business rules + messages      |
  | readPositiveInt     |   | sample orders                  |
  | readNonEmptyString  |   | status whitelist               |
  | isValidStatus       |   | delete condition               |
  +---------------------+   +---------------+----------------+
                                            | calls
                                            v
                            +--------------------------------+
                            | OrderDAO.java                  |
                            | PreparedStatement, ResultSet   |
                            | try-with-resources             |
                            +---------------+----------------+
                                            | uses
                    +-----------------------+-----------------------+
                    |                                               |
                    v                                               v
      +--------------------------+                  +--------------------------+
      | DBConnection.java        |                  | Order.java               |
      | getConnection()          |                  | fields, constructors,     |
      | URL / USER / PASSWORD    |                  | getters, setters, toString|
      +------------+-------------+                  +--------------------------+
                   |
                   v
      +--------------------------+
      | MySQL Server 8.x         |
      | database: ecommerce_db   |
      | table: Orders            |
      +--------------------------+
```

### 6.1 Database connection flow

1. `Main` calls a service method.
2. `OrderService` calls an `OrderDAO` method.
3. `OrderDAO` calls `DBConnection.getConnection()`.
4. `DriverManager` uses Connector/J to open a TCP connection to `localhost:3306/ecommerce_db`.
5. The DAO creates a `PreparedStatement`, binds values and executes SQL.
6. Results are mapped to `Order` objects or to a boolean/double.
7. try-with-resources closes `ResultSet`, `PreparedStatement` and `Connection`.
8. The service formats a message; `Main` prints it and shows the menu again.

---

## 7. Database Design

### 7.1 Database

`ecommerce_db` is created with `CREATE DATABASE IF NOT EXISTS`. The application URL in `DBConnection.java` points to this database.

### 7.2 Orders table

| Attribute | Data type | Constraint | Meaning |
|---|---|---|---|
| `order_id` | INT | PRIMARY KEY | Unique identifier; no duplicates allowed |
| `customer_name` | VARCHAR(100) | none | Name of the customer |
| `product_name` | VARCHAR(100) | none | Name of the ordered product |
| `quantity` | INT | none | Number of units ordered |
| `unit_price` | DOUBLE | none | Price of one unit |
| `status` | VARCHAR(30) | none | Pending, Shipped, Delivered or Cancelled |

### 7.3 SQL script (`database/ecommerce_db.sql`)

```sql
CREATE DATABASE IF NOT EXISTS ecommerce_db;
USE ecommerce_db;

CREATE TABLE IF NOT EXISTS Orders (
    order_id      INT PRIMARY KEY,
    customer_name VARCHAR(100),
    product_name  VARCHAR(100),
    quantity      INT,
    unit_price    DOUBLE,
    status        VARCHAR(30)
);
```

### 7.4 Sample records

The application inserts these rows when menu option 1 is selected:

| order_id | customer_name | product_name | quantity | unit_price | status |
|---|---|---|---|---|---|
| 101 | Aarav Sharma | Laptop | 2 | 55000.0 | Pending |
| 102 | Priya Verma | Mouse | 5 | 799.0 | Shipped |
| 103 | Rohan Gupta | Keyboard | 4 | 1999.0 | Delivered |
| 104 | Neha Singh | Monitor | 3 | 12000.0 | Cancelled |

Order 104 exists so that option 6 (delete cancelled order) can be demonstrated. The SQL script keeps its own sample `INSERT` commented out to avoid duplicate messages when option 1 is used.

Manual total for the four sample rows:
`2 × 55000 + 5 × 799 + 4 × 1999 + 3 × 12000 = 157991.00`.

---

## 8. Project Directory Structure

```
q1-order-management/
├── pom.xml
├── README.md
├── database/
│   └── ecommerce_db.sql
└── src/main/java/com/example/ordermanagement/
    ├── Main.java
    ├── database/
    │   └── DBConnection.java
    ├── model/
    │   └── Order.java
    ├── dao/
    │   └── OrderDAO.java
    ├── service/
    │   └── OrderService.java
    └── util/
        └── InputValidator.java
```

| Package / file | Purpose |
|---|---|
| `com.example.ordermanagement.Main` | Starts the application and runs the menu loop |
| `com.example.ordermanagement.database.DBConnection` | Holds JDBC URL, username, password and `getConnection()` |
| `com.example.ordermanagement.model.Order` | Model for one row of `Orders` |
| `com.example.ordermanagement.dao.OrderDAO` | All SQL and JDBC operations |
| `com.example.ordermanagement.service.OrderService` | Business rules, validation of rules, result messages |
| `com.example.ordermanagement.util.InputValidator` | Safe console reading and status whitelist |
| `database/ecommerce_db.sql` | Creates the database and table |
| `pom.xml` | Java 17, Connector/J 8.4.0, exec plugin |

---

## 9. Detailed Source Code Explanation

### 9.1 Main.java

`Main` creates one `OrderService`, wraps `Scanner` in try-with-resources and loops until the user chooses 8. Its `switch` maps options 1–8 to service methods. Invalid menu choices fall into `default`. The `Scanner` is closed automatically on exit.

### 9.2 DBConnection.java

Holds the JDBC URL `jdbc:mysql://localhost:3306/ecommerce_db?...`, `USER` and `PASSWORD` as constants. `getConnection()` calls `DriverManager.getConnection`. Connector/J 8 registers itself, so `Class.forName` is unnecessary.

### 9.3 Order.java

Declares six members matching the table columns. It has a no-argument constructor, an all-argument constructor, getters and setters for every field, and `toString()` for printing an updated order.

### 9.4 OrderDAO.java

Contains eight `private static final` SQL strings. Each public method opens a connection and a `PreparedStatement` in try-with-resources. `insert` catches `SQLIntegrityConstraintViolationException` and returns `false`. Query methods build `ArrayList<Order>` using `mapRow`. Update and delete methods return `executeUpdate() > 0`.

### 9.5 OrderService.java

Holds the four sample orders and all business rules. It catches `SQLException` from the DAO, checks quantity > 0, checks the status whitelist, retrieves an order before status update, and permits deletion only for `Cancelled`. It formats tables with `String.format`.

### 9.6 InputValidator.java

`readInt` retries until `Integer.parseInt` succeeds. `readPositiveInt` additionally rejects 0 and negatives. `readNonEmptyString` rejects blank text. `isValidStatus` checks membership in `Set.of("Pending", "Shipped", "Delivered")`.

---

## 10. Implementation of the Five Examination Requirements

### A. Database connection and insertion

| Item | Detail |
|---|---|
| Objective | Connect to MySQL and insert at least three sample orders with duplicate handling |
| Java method | `Main` option 1 → `OrderService.insertSampleOrders()` → `OrderDAO.insert(Order)` |
| SQL | `INSERT INTO Orders (order_id, customer_name, product_name, quantity, unit_price, status) VALUES (?, ?, ?, ?, ?, ?)` |
| JDBC methods | `DriverManager.getConnection`, `prepareStatement`, `setInt`, `setString`, `setDouble`, `executeUpdate` |
| Business logic | Four sample orders are attempted one by one; a duplicate primary key is caught and reported without stopping the loop |
| Expected output | "Inserted order 101." etc.; on repeat, "Order 101 skipped: duplicate order ID." |

### B. Filtering and sorting

| Item | Detail |
|---|---|
| Objective | Accept a status, show only quantity > 2, sorted by price descending |
| Java method | `OrderService.searchOrders(String)` → `OrderDAO.findByStatusAndMinimumQuantity(status, 2)` |
| SQL | `SELECT ... FROM Orders WHERE status = ? AND quantity > ? ORDER BY unit_price DESC` |
| JDBC methods | `prepareStatement`, `setString`, `setInt`, `executeQuery`, `ResultSet.next`, `getInt`, `getString`, `getDouble` |
| Business logic | Both conditions run in the database; Java only displays rows. Empty result produces a clear message |
| Expected output | Table of matching rows, highest `unit_price` first |

### C. Increasing quantity

| Item | Detail |
|---|---|
| Objective | Add a supplied amount to an existing quantity and display the updated order |
| Java method | `OrderService.increaseQuantity(int, int)` → `OrderDAO.updateQuantity` + `findById` |
| SQL | `UPDATE Orders SET quantity = quantity + ? WHERE order_id = ?`; then `SELECT ... WHERE order_id = ?` |
| JDBC methods | `prepareStatement`, `setInt`, `executeUpdate`, `executeQuery` |
| Business logic | Zero/negative extra quantity is rejected; `executeUpdate() == 0` means the ID does not exist; the row is re-read for display |
| Expected output | "Quantity increased by 3. Updated order: Order [id=102, ... quantity=8, ...]" |

### D. Validating and updating status

| Item | Detail |
|---|---|
| Objective | Retrieve the order, accept only Pending/Shipped/Delivered, update and display |
| Java method | `OrderService.updateOrderStatus(int, String)` → `findById` → `InputValidator.isValidStatus` → `OrderDAO.updateStatus` |
| SQL | `SELECT ... WHERE order_id = ?`; `UPDATE Orders SET status = ? WHERE order_id = ?`; `SELECT` again |
| JDBC methods | `prepareStatement`, `setInt`, `setString`, `executeQuery`, `executeUpdate` |
| Business logic | Retrieval happens first; invalid status and missing ID produce distinct messages; updated row is displayed |
| Expected output | "Status updated. Updated order: Order [...]" |

### E. Total value and conditional deletion

| Item | Detail |
|---|---|
| Objective | Display `SUM(quantity * unit_price)`, then delete only if status is Cancelled |
| Java method | `OrderService.getTotalOrderValue()` and `OrderService.deleteCancelledOrder(int)` |
| SQL | `SELECT COALESCE(SUM(quantity * unit_price), 0) FROM Orders`; `SELECT ... WHERE order_id = ?`; `DELETE FROM Orders WHERE order_id = ?` |
| JDBC methods | `executeQuery`, `getDouble`, `executeUpdate` |
| Business logic | Total is shown first; the row is retrieved; deletion is attempted only when status equals `Cancelled` (case-insensitive) |
| Expected output | Total value followed by success, non-cancelled, or nonexistent message |

---

## 11. Testing and Results

### 11.1 Evidence available

Executed command in `q1-order-management/`:

```
mvn -q -DskipTests package
```

Observed output:

```
COMPILE: BUILD SUCCESS
```

A source review also confirmed: eight `prepareStatement` calls, no plain `Statement`, try-with-resources in every DAO method, and `catch (SQLException)` in every service method.

**No MySQL runtime test was executed.** There was no `mysql`/`mysqld` executable and the Docker daemon was unavailable, so the application could not be started against a database. Consequently the table below gives expected outputs and static verification only; there are no observed console outputs or MySQL result sets to report.

### 11.2 Test cases

| ID | Scenario | Input | Expected output | Executed? |
|---|---|---|---|---|
| T1 | Connect to database | start app | menu appears | No |
| T2 | Insert samples | option 1 | four inserted messages | No |
| T3 | Duplicate ID | option 1 twice | duplicate message, no crash | No |
| T4 | Filter | status `Shipped` | matching rows only | No |
| T5 | Quantity rule | status `Pending` | rows with quantity > 2 only | No |
| T6 | Sorting | any status | highest unit price first | No |
| T7 | Empty filter | status `Unknown` | no orders found message | No |
| T8 | Increase quantity | id 102, extra 3 | updated row with quantity 8 | No |
| T9 | Missing order on increase | id 999 | does not exist message | No |
| T10 | Invalid quantity | extra 0 or -2 | rejected before SQL | No |
| T11 | Status Pending | id 101 | updated row | No |
| T12 | Status Shipped | id 102 | updated row | No |
| T13 | Status Delivered | id 103 | updated row | No |
| T14 | Invalid status | `Unknown` | invalid status message | No |
| T15 | Missing order on status | id 999 | does not exist message | No |
| T16 | Total value | option 5 | `157991.00` for initial samples | No |
| T17 | Delete Cancelled | id 104 | deleted successfully | No |
| T18 | Delete non-Cancelled | id 102 | cannot be deleted message | No |
| T19 | Delete nonexistent | id 999 | does not exist message | No |
| T20 | SQL injection attempt | status `' OR '1'='1` | no rows, no SQL change | No |
| T21 | Invalid menu input | `abc` | validation message, menu repeats | No |
| T22 | Repeated execution | options 1–7 then 8 | menu repeats until 8 | No |

### 11.3 Screenshot placeholders

Paste real screenshots here after running the application against MySQL:

```
[PASTE SCREENSHOT: menu after startup]

[PASTE SCREENSHOT: option 1 — four inserted orders]

[PASTE SCREENSHOT: option 1 repeated — duplicate messages]

[PASTE SCREENSHOT: option 2 — filtered and sorted rows]

[PASTE SCREENSHOT: option 3 — updated quantity]

[PASTE SCREENSHOT: option 4 — updated status]

[PASTE SCREENSHOT: option 5 — SUM result]

[PASTE SCREENSHOT: option 6 — cancelled deletion]

[PASTE SCREENSHOT: MySQL table after operations]
```

---

## 12. AI Prompt Documentation

### Prompt 1 — Initial application request

**Prompt used:** "I'm a B.Tech CSE student. Write a menu-driven Java JDBC console application for an Order Management System using MySQL (database: ecommerce_db, table: Orders with order_id INT PK, customer_name VARCHAR(100), product_name VARCHAR(100), quantity INT, unit_price DOUBLE, status VARCHAR(30)). Menu options: a) Connect to the DB and insert at least 3 orders using PreparedStatement; handle duplicate order IDs (catch SQLIntegrityConstraintViolationException) b) Accept a status and show orders with that status and quantity > 2, sorted by unit_price DESC c) Accept order ID and extra quantity; add it to the existing quantity; show a message if the order doesn't exist d) Accept order ID and new status; update only if the order exists and the status is Pending/Shipped/Delivered; then display the updated order e) Show total order value using SUM(quantity * unit_price); accept an order ID and delete it only if its status is 'Cancelled', otherwise show a message. Use try-with-resources, PreparedStatement everywhere, and comments explaining each part. Also give me the CREATE TABLE SQL and the MySQL connector dependency."

**Reflection:** I used this prompt to get a first working version that covered all five examination operations and the required MySQL dependency. It gave me a single-file application with the correct SQL and duplicate handling. From this I learned the basic JDBC lifecycle. I independently checked that each menu option matched the question paper and corrected the folder layout later.

### Prompt 2 — Layered project rebuild

**Prompt used:** "Act as a senior Java developer and JDBC database architect. I am a second-year B.Tech CSE student developing an Order Management System for my CS336B MSE-2 examination. Build a complete, professional, beginner-friendly Java JDBC and MySQL application based strictly on the following examination requirements... [full database details, five mandatory operations, technical requirements, suggested project structure with Main.java, database/DBConnection.java, model/Order.java, dao/OrderDAO.java, service/OrderService.java, util/InputValidator.java, database/ecommerce_db.sql and README.md, menu options 1–8, and instructions to explain architecture and SQL queries]."

**Reflection:** I used this prompt because the first version kept most logic in one class, while the exam expected organised code. The result was the layered project now in `q1-order-management/`. It helped me separate input handling, business rules and JDBC. I learned why a DAO is useful: SQL appears in one place only. I independently fixed three issues: an incorrect total in the README, sample rows in the SQL script that conflicted with menu option 1, and status validation that ran before retrieving the order.

### Prompt 3 — Source and requirement review

**Prompt used:** "Act as an experienced Java JDBC developer and database testing engineer. I have developed a Java JDBC and MySQL Order Management System for my CS336B MSE-2 examination. Review the complete project source code and verify that every requirement of the examination question has been implemented correctly... Test the following five requirements... For each test case provide: test case ID, objective, input, SQL operation, expected output, actual output (only if the test was executed), pass/fail status (only when verified)... Do not invent successful test results or claim execution without evidence."

**Reflection:** I used this prompt to audit the implementation against the examination wording. It confirmed the presence of every required query and highlighted that validation order mattered in requirement D. I learned that static inspection can prove code structure but not runtime behaviour. I verified compilation myself and deliberately left runtime results unclaimed because no MySQL server was reachable.

---

## 13. Limitations and Future Scope

### Limitations

- Credentials are constants in `DBConnection.java` rather than an external configuration file.
- Each DAO method opens its own connection, so a multi-step operation is not wrapped in one database transaction.
- The application is console-based and has no graphical interface or authentication.
- The status whitelist does not allow setting `Cancelled` from the menu; sample order 104 supplies the cancelled case.
- SQL exception messages are returned as text rather than structured error objects.
- No automated integration tests were executed against MySQL.

### Future enhancements

- Move credentials to a properties file or environment variables.
- Use one connection with an explicit transaction for update-plus-display operations.
- Add a Swing/JavaFX or web interface.
- Add user authentication and role-based access.
- Add date columns, stock checks, pagination and search by customer or product.

---

## 14. Conclusion

The project implements all five examination operations for an `ecommerce_db.Orders` table using Java 17, JDBC and MySQL Connector/J. It uses parameterised SQL throughout, try-with-resources for all database objects, a layered class structure, input validation and clear user messages. The code compiles successfully with Maven. Runtime verification against MySQL remains to be performed on a machine with MySQL 8 installed; the report deliberately does not claim results that were not observed.

---

## 15. Viva Questions and Answers

**Q1. What is JDBC?**
A. A Java API in `java.sql` that connects a Java program to a relational database through a vendor driver.

**Q2. What does `DriverManager.getConnection` do?**
A. It selects a suitable registered driver and opens a physical connection to the database URL.

**Q3. Why is `Class.forName` not used?**
A. JDBC 4 drivers register themselves automatically through service loading; Connector/J 8 does this.

**Q4. Why use `PreparedStatement` instead of `Statement`?**
A. It is precompiled, prevents SQL injection through `?` placeholders and handles type conversion.

**Q5. What does `executeUpdate()` return?**
A. The number of affected rows. Zero means no row matched the `WHERE` clause.

**Q6. What does `executeQuery()` return?**
A. A `ResultSet` cursor over the matching rows.

**Q7. How do you read a row from `ResultSet`?**
A. Call `next()` to move the cursor, then `getInt("column")` or `getString("column")`.

**Q8. What is a primary key?**
A. A column that uniquely identifies each row. MySQL rejects duplicate `order_id` values.

**Q9. How are duplicate IDs handled here?**
A. `OrderDAO.insert` catches `SQLIntegrityConstraintViolationException` and returns `false`; the service prints a duplicate message.

**Q10. How is quantity increased safely?**
A. `UPDATE Orders SET quantity = quantity + ? WHERE order_id = ?` performs the addition inside the database.

**Q11. How is SQL injection prevented in the status search?**
A. The user's text is bound with `setString`; it is never concatenated into the SQL string.

**Q12. What does `COALESCE(SUM(...), 0)` do?**
A. `SUM` returns NULL for an empty table; `COALESCE` converts it to 0.

**Q13. Why retrieve an order before changing its status?**
A. To confirm it exists and to display the original details before modification, as required by the question.

**Q14. Why can only cancelled orders be deleted?**
A. That is the examination rule; `deleteCancelledOrder` checks the retrieved status before calling the DAO delete.

**Q15. What is try-with-resources?**
A. A `try (...)` statement that automatically closes `Connection`, `PreparedStatement`, `ResultSet` and `Scanner`.

**Q16. Where are SQL exceptions handled?**
A. DAO methods declare `throws SQLException`; service methods catch it and return a readable message.

**Q17. What OOP principles are demonstrated?**
A. Encapsulation in `Order`, abstraction through the service/DAO split, single responsibility per class and reuse of `DBConnection`.

**Q18. What was actually verified in this environment?**
A. Compilation (`mvn package` → BUILD SUCCESS) and static source review. No MySQL runtime test was executed because no database server was available.

---

## 16. Source Code Appendix

### 16.1 `pom.xml`

```xml
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <groupId>com.example</groupId>
    <artifactId>order-management</artifactId>
    <version>1.0-SNAPSHOT</version>

    <properties>
        <maven.compiler.release>17</maven.compiler.release>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
    </properties>

    <dependencies>
        <dependency>
            <groupId>com.mysql</groupId>
            <artifactId>mysql-connector-j</artifactId>
            <version>8.4.0</version>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>org.codehaus.mojo</groupId>
                <artifactId>exec-maven-plugin</artifactId>
                <version>3.3.0</version>
                <configuration>
                    <mainClass>com.example.ordermanagement.Main</mainClass>
                </configuration>
            </plugin>
        </plugins>
    </build>
</project>
```

### 16.2 `database/ecommerce_db.sql`

```sql
-- Database and table used by the Order Management System.
-- Run this script first:  mysql -u root -p < database/ecommerce_db.sql

CREATE DATABASE IF NOT EXISTS ecommerce_db;
USE ecommerce_db;

-- order_id is the PRIMARY KEY, so duplicate IDs are rejected by MySQL itself.
CREATE TABLE IF NOT EXISTS Orders (
    order_id      INT PRIMARY KEY,
    customer_name VARCHAR(100),
    product_name  VARCHAR(100),
    quantity      INT,
    unit_price    DOUBLE,
    status        VARCHAR(30)
);

-- The application itself inserts four sample orders when menu option 1 is chosen.
-- Keep the INSERT below commented out unless you want to preload rows manually;
-- otherwise option 1 will correctly report those IDs as duplicates.
-- INSERT INTO Orders (order_id, customer_name, product_name, quantity, unit_price, status) VALUES
-- (101, 'Aarav Sharma', 'Laptop',   2, 55000.0, 'Pending'),
-- (102, 'Priya Verma',  'Mouse',    5,   799.0, 'Shipped'),
-- (103, 'Rohan Gupta',  'Keyboard', 4,  1999.0, 'Delivered'),
-- (104, 'Neha Singh',   'Monitor',  3, 12000.0, 'Cancelled');
```

### 16.3 `src/main/java/com/example/ordermanagement/Main.java`

```java
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
```

### 16.4 `src/main/java/com/example/ordermanagement/database/DBConnection.java`

```java
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
    private static final String USER = "root";
    private static final String PASSWORD = "password";

    private DBConnection() {
    }

    public static Connection getConnection() throws SQLException {
        // Connector/J 8 registers itself automatically, so Class.forName is not needed.
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}
```

### 16.5 `src/main/java/com/example/ordermanagement/model/Order.java`

```java
package com.example.ordermanagement.model;

/**
 * Represents one row of the Orders table.
 * The class has only data and accessors; JDBC work belongs in the DAO.
 */
public class Order {
    private int orderId;
    private String customerName;
    private String productName;
    private int quantity;
    private double unitPrice;
    private String status;

    // Needed by frameworks and by code that creates an empty order first.
    public Order() {
    }

    public Order(int orderId, String customerName, String productName,
                 int quantity, double unitPrice, String status) {
        this.orderId = orderId;
        this.customerName = customerName;
        this.productName = productName;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.status = status;
    }

    public int getOrderId() {
        return orderId;
    }

    public void setOrderId(int orderId) {
        this.orderId = orderId;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public double getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(double unitPrice) {
        this.unitPrice = unitPrice;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "Order [id=" + orderId + ", customer=" + customerName
                + ", product=" + productName + ", quantity=" + quantity
                + ", unitPrice=" + unitPrice + ", status=" + status + "]";
    }
}
```

### 16.6 `src/main/java/com/example/ordermanagement/dao/OrderDAO.java`

```java
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
```

### 16.7 `src/main/java/com/example/ordermanagement/service/OrderService.java`

```java
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
```

### 16.8 `src/main/java/com/example/ordermanagement/util/InputValidator.java`

```java
package com.example.ordermanagement.util;

import java.util.Scanner;
import java.util.Set;

/** Reads console input safely so that bad input cannot crash the menu. */
public final class InputValidator {

    private static final Set<String> VALID_STATUSES = Set.of("Pending", "Shipped", "Delivered");

    private InputValidator() {
    }

    /** Reads a whole line and retries until it is a valid integer. */
    public static int readInt(Scanner scanner, String prompt) {
        while (true) {
            System.out.print(prompt);
            try {
                return Integer.parseInt(scanner.nextLine().trim());
            } catch (NumberFormatException ex) {
                System.out.println("Please enter a valid number.");
            }
        }
    }

    /** Rejects zero and negative values, used for the extra quantity. */
    public static int readPositiveInt(Scanner scanner, String prompt) {
        while (true) {
            int value = readInt(scanner, prompt);
            if (value > 0) {
                return value;
            }
            System.out.println("The quantity must be greater than 0.");
        }
    }

    public static String readNonEmptyString(Scanner scanner, String prompt) {
        while (true) {
            System.out.print(prompt);
            String value = scanner.nextLine().trim();
            if (!value.isEmpty()) {
                return value;
            }
            System.out.println("This field cannot be empty.");
        }
    }

    public static boolean isValidStatus(String status) {
        return VALID_STATUSES.contains(status);
    }
}
```

---

**End of report.**
