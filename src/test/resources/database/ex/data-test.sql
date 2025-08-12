INSERT INTO users (id, email, password, first_name, last_name) VALUES
(1, 'testuser@example.com', 'password', 'Test', 'User'),
(2, 'historyUser@example.com', 'password', 'History', 'User'),
(3, 'itemsUser@example.com', 'password', 'Items', 'User'),
(4, 'specificItemUser@example.com', 'password', 'Specific', 'User'),
(5, 'adminUser@example.com', 'password', 'Admin', 'User');

INSERT INTO books (id, title, author, isbn, price) VALUES
(1, 'Test Book', 'Author', '1234567890', 20.00),
(2, 'Specific Book', 'Author', '1234567891', 25.00);

INSERT INTO orders (id, user_id, order_date, status, shipping_address, total) VALUES
(1, 2, NOW(), 'PENDING', 'Lviv', 0.00),
(2, 3, NOW(), 'PENDING', 'Odessa', 40.00),  -- 2 * 20.00
(3, 4, NOW(), 'PENDING', 'Dnipro', 25.00),  -- 1 * 25.00
(4, 5, NOW(), 'PENDING', 'Kharkiv', 0.00);

INSERT INTO order_items (id, order_id, book_id, quantity, price, is_deleted) VALUES
(1, 2, 1, 2, 20.00, FALSE),
(2, 3, 2, 1, 25.00, FALSE);
