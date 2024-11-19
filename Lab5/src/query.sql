CREATE DATABASE test_db;
\c test_db

CREATE TABLE users (
    user_id SERIAL PRIMARY KEY,
    username VARCHAR(50) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE products (
    product_id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    price DECIMAL(10, 2) NOT NULL CHECK (price > 0),
    stock_quantity INT NOT NULL CHECK (stock_quantity >= 0),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE orders (
    order_id SERIAL PRIMARY KEY,
    user_id INT REFERENCES users(user_id),
    total_price DECIMAL(10, 2) NOT NULL CHECK (total_price > 0),
    order_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE order_details (
    detail_id SERIAL PRIMARY KEY,
    order_id INT REFERENCES orders(order_id),
    product_id INT REFERENCES products(product_id),
    quantity INT NOT NULL CHECK (quantity > 0),
    subtotal DECIMAL(10, 2) NOT NULL CHECK (subtotal > 0)
);

CREATE TABLE event_log (
    log_id SERIAL PRIMARY KEY,
    event_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    description TEXT NOT NULL
);

INSERT INTO users (username, email) VALUES
    ('Alice', 'alice@example.com'),
    ('Bob', 'bob@example.com'),
    ('Charlie', 'charlie@example.com'),
    ('Diana', 'diana@example.com'),
    ('Eve', 'eve@example.com');

INSERT INTO products (name, price, stock_quantity) VALUES
    ('Laptop', 1200.00, 10),
    ('Smartphone', 800.00, 20),
    ('Tablet', 400.00, 15),
    ('Monitor', 250.00, 8),
    ('Keyboard', 50.00, 50);

INSERT INTO orders (user_id, total_price) VALUES
    (1, 1600.00),
    (2, 800.00),
    (3, 400.00),
    (4, 500.00),
    (5, 1200.00);

INSERT INTO order_details (order_id, product_id, quantity, subtotal) VALUES
    (1, 1, 1, 1200.00),
    (1, 5, 8, 400.00),
    (2, 2, 1, 800.00),
    (3, 3, 1, 400.00),
    (4, 4, 2, 500.00),
    (5, 1, 1, 1200.00);

SELECT * FROM users;
SELECT * FROM products;
SELECT * FROM orders;
SELECT * FROM order_details;
SELECT * FROM event_log;
