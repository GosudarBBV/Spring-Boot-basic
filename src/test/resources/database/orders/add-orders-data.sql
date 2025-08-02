INSERT INTO users (id, email, password, first_name, last_name, shipping_address)
VALUES (1, 'test@example.com', 'pass', 'John', 'Doe', 'Kyiv');

INSERT INTO users (id, email, password, first_name, last_name, shipping_address)
VALUES (2, 'john@example.com', 'pass', 'John', 'Smith', 'Kyiv');

INSERT INTO users (id, email, password, first_name, last_name, shipping_address)
VALUES (3, 'user2@example.com', 'pass', 'Anna', 'Black', 'Kyiv');

INSERT INTO orders (id, user_id, order_date, status, shipping_address, total)
VALUES
(1, 1, NOW(), 'PENDING', 'Kyiv', 0.00),
(2, 1, NOW(), 'DELIVERED', 'Kyiv', 0.00);

INSERT INTO orders (id, user_id, order_date, status, shipping_address, total)
VALUES (3, 2, NOW(), 'PENDING', 'Kyiv', 0.00);