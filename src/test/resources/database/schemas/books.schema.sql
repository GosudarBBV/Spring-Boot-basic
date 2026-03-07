DROP TABLE IF EXISTS order_items;
DROP TABLE IF EXISTS books_categories;
DROP TABLE IF EXISTS books;
DROP TABLE IF EXISTS categories;

CREATE TABLE categories (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    is_deleted BOOLEAN DEFAULT FALSE
);

CREATE TABLE books (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    title VARCHAR(255),
    author VARCHAR(255),
    isbn VARCHAR(255),
    price DECIMAL(10,2),
    description TEXT,
    cover_image TEXT,
    is_deleted BOOLEAN
);

CREATE TABLE books_categories (
    book_id BIGINT NOT NULL,
    category_id BIGINT NOT NULL,
    PRIMARY KEY (book_id, category_id),
    FOREIGN KEY (book_id) REFERENCES books(id),
    FOREIGN KEY (category_id) REFERENCES categories(id)
);
