-- user for test findByUserId
INSERT INTO users (id, email, password, first_name, last_name, shipping_address)
VALUES (1, 'test@cart.com', 'pass', 'Test', 'Cart', 'Kyiv');

-- user for test existsByUserId
INSERT INTO users (id, email, password, first_name, last_name, shipping_address)
VALUES (2, 'test2@cart.com', 'pass', 'Test2', 'Cart', 'Kyiv');

-- shopping carts
INSERT INTO shopping_carts (id, user_id) VALUES (1, 1), (2, 2);