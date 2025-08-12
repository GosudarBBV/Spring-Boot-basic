INSERT INTO users (id, email, password, first_name, last_name, shipping_address, is_deleted) VALUES
(1, 'testuser@example.com', 'password', 'Test', 'User', 'Kyiv, Street 1', FALSE),
(2, 'historyUser@example.com', 'password', 'History', 'User', 'Lviv, Street 2', FALSE),
(3, 'itemsUser@example.com', 'password', 'Items', 'User', 'Odessa, Street 3', FALSE),
(4, 'specificItemUser@example.com', 'password', 'Specific', 'User', 'Dnipro, Street 4', FALSE),
(5, 'adminUser@example.com', 'password', 'Admin', 'User', 'Kharkiv, Street 5', FALSE);

-- roles
INSERT INTO roles (id, name) VALUES
(1, 'ROLE_USER'),
(2, 'ROLE_ADMIN');

-- users_roles
INSERT INTO users_roles (user_id, role_id) VALUES
(1, 1),
(2, 1),
(3, 1),
(4, 1),
(5, 1),
(5, 2);  -- adminUser має роль ADMIN також

-- categories
INSERT INTO categories (id, name) VALUES
(1, 'Fiction'),
(2, 'Science'),
(3, 'History');

-- books
INSERT INTO books (id, title, author, isbn, price, description, cover_image, is_deleted) VALUES
(1, 'Test Book', 'Author A', '1234567890', 20.00, 'Interesting book about...', 'cover1.jpg', FALSE),
(2, 'Specific Book', 'Author B', '1234567891', 25.00, 'Specific topics explained...', 'cover2.jpg', FALSE);

-- books_categories (зв’язок many-to-many)
INSERT INTO books_categories (book_id, category_id) VALUES
(1, 1),  -- Test Book в категорії Fiction
(2, 2);  -- Specific Book в категорії Science

-- orders
INSERT INTO orders (id, user_id, status, total, order_date, shipping_address, is_deleted) VALUES
(1, 2, 'PENDING', 0.00, NOW(), 'Lviv', FALSE),
(2, 3, 'PENDING', 40.00, NOW(), 'Odessa', FALSE),  -- 2 * 20.00
(3, 4, 'PENDING', 25.00, NOW(), 'Dnipro', FALSE),  -- 1 * 25.00
(4, 5, 'PENDING', 0.00, NOW(), 'Kharkiv', FALSE);

-- order_items
INSERT INTO order_items (id, order_id, book_id, quantity, price, is_deleted) VALUES
(1, 2, 1, 2, 20.00, FALSE),
(2, 3, 2, 1, 25.00, FALSE);
