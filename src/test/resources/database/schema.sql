-- 1. Таблиця books
CREATE TABLE books (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(255),
    author VARCHAR(255),
    isbn VARCHAR(255),
    price DECIMAL(10, 2),
    description VARCHAR(1000),
    cover_image VARCHAR(255),
    is_deleted BIT NOT NULL DEFAULT FALSE
);

-- 2. Таблиця users
CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    first_name VARCHAR(255) NOT NULL,
    last_name VARCHAR(255) NOT NULL,
    shipping_address VARCHAR(255),
    is_deleted BIT NOT NULL DEFAULT FALSE
);

-- 3. Таблиця roles
CREATE TABLE roles (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE
);

-- 4. Таблиця users_roles (зв’язок many-to-many)
CREATE TABLE users_roles (
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    CONSTRAINT fk_users_roles_user FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT fk_users_roles_role FOREIGN KEY (role_id) REFERENCES roles(id)
);

-- 5. Таблиця categories
CREATE TABLE categories (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description VARCHAR(255),
    is_deleted BIT DEFAULT FALSE
);

-- 6. Таблиця books_categories (зв’язок many-to-many)
CREATE TABLE books_categories (
    book_id BIGINT NOT NULL,
    category_id BIGINT NOT NULL,
    FOREIGN KEY (book_id) REFERENCES books(id),
    FOREIGN KEY (category_id) REFERENCES categories(id)
);

-- 7. Таблиця shopping_carts
CREATE TABLE shopping_carts (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE,
    FOREIGN KEY (user_id) REFERENCES users(id)
);

-- 8. Таблиця cart_items
CREATE TABLE cart_items (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    shopping_cart_id BIGINT NOT NULL,
    book_id BIGINT NOT NULL,
    quantity INT NOT NULL,
    FOREIGN KEY (shopping_cart_id) REFERENCES shopping_carts(id),
    FOREIGN KEY (book_id) REFERENCES books(id)
);

-- 9. Таблиця orders
CREATE TABLE orders (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    order_date TIMESTAMP NOT NULL,
    status VARCHAR(50) NOT NULL,
    total DECIMAL(10, 2) NOT NULL,
    shipping_address VARCHAR(255) NOT NULL,
    is_deleted BIT DEFAULT FALSE,
    CONSTRAINT fk_orders_users FOREIGN KEY (user_id) REFERENCES users(id)
);

-- 10. Таблиця order_items
CREATE TABLE order_items (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id BIGINT NOT NULL,
    book_id BIGINT NOT NULL,
    quantity INT NOT NULL,
    price DECIMAL(10, 2) NOT NULL,
    is_deleted BIT DEFAULT FALSE,
    CONSTRAINT fk_order_items_orders FOREIGN KEY (order_id) REFERENCES orders(id),
    CONSTRAINT fk_order_items_books FOREIGN KEY (book_id) REFERENCES books(id)
);