-- Вставка ролей
INSERT INTO roles (id, name) VALUES (1, 'USER'), (2, 'ADMIN');

-- Вставка користувача USER
INSERT INTO users (id, email, password, first_name, last_name, shipping_address)
VALUES (1, 'user@example.com', 'encrypted-password', 'User', 'Test', '123 Test St, Kyiv');

-- Вставка користувача ADMIN
INSERT INTO users (id, email, password, first_name, last_name, shipping_address)
VALUES (2, 'admin@example.com', 'encrypted-password', 'Admin', 'Test', '456 Admin St, Kyiv');

-- Присвоєння ролей користувачам
INSERT INTO users_roles (user_id, role_id) VALUES (1, 1), (2, 2);

-- Вставка книги з id = 1 (як у тестах)
INSERT INTO books (id, title, author, isbn, price, description, cover_image, is_deleted)
VALUES (1, 'Test Book', 'Author Test', 'ISBN123456', 9.99, 'Test description', NULL, FALSE);

-- Створення корзини для користувача USER
INSERT INTO shopping_carts (id, user_id) VALUES (1, 1);