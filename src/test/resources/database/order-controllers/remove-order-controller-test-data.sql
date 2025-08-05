-- Очищення замовлень і замовлених товарів
DELETE FROM order_items;
DELETE FROM orders;

-- Очищення корзини і елементів
DELETE FROM cart_items;
DELETE FROM shopping_carts;

-- Очищення ролей користувачів
DELETE FROM users_roles;

-- Очищення користувачів і ролей
DELETE FROM users;
DELETE FROM roles;

-- Очищення книг і категорій
DELETE FROM books_categories;
DELETE FROM books;
DELETE FROM categories;