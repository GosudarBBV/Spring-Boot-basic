INSERT INTO users (id, email, password, first_name, last_name, shipping_address)
VALUES (1, 'test@example.com', 'password', 'Test', 'User', '123 Main St');

INSERT INTO books (id, title, author, price, description, isbn, cover_image, is_deleted)
VALUES (1, 'Test Book', 'Author Name', 10.00, 'A sample book for testing', '1234567890', 'cover.jpg', false);

INSERT INTO shopping_carts (id, user_id)
VALUES (1, 1);

INSERT INTO cart_items (id, shopping_cart_id, book_id, quantity)
VALUES (1, 1, 1, 2);