INSERT INTO users (id, email, password, first_name, last_name, shipping_address)
VALUES (1, 'user@example.com', 'pass', 'John', 'Doe', 'Kyiv');

INSERT INTO books (id, title, author, price, description, isbn, cover_image, is_deleted)
VALUES (1, 'Book 1', 'Author', 50.00, 'Test description', '1234567890123', 'cover.jpg', FALSE);

INSERT INTO orders (id, user_id, order_date, status, shipping_address, total)
VALUES (1, 1, NOW(), 'PENDING', 'Kyiv', 100.00);

INSERT INTO order_items (id, order_id, book_id, quantity, price)
VALUES (1, 1, 1, 2, 100.00);