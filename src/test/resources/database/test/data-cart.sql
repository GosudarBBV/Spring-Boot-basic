INSERT INTO users (id, email, password, first_name, last_name, shipping_address, is_deleted) VALUES
(1, 'testuser@example.com', 'password', 'Test', 'User', 'Kyiv, Street 1', FALSE);

INSERT INTO books (id, title, author, isbn, price, description, cover_image, is_deleted) VALUES
(1, 'Test Book', 'Author A', '1234567890', 20.00, 'Interesting book about...', 'cover1.jpg', FALSE);

INSERT INTO shopping_cart (id, user_id)
VALUES (1, 1);

INSERT INTO cart_item (id, cart_id, book_id, quantity)
VALUES (1, 1, 1, 2);

