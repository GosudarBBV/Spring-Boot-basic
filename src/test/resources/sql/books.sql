-- Категорії (опціонально, якщо ще не створено)
INSERT INTO categories (id, name, description, is_deleted)
VALUES
    (1, 'Fiction', 'Fiction books', false);

-- Книга
INSERT INTO books (id, title, author, isbn, price, description, cover_image, is_deleted)
VALUES
    (100, 'SQL Book', 'Tester', 'ISBN12345', 19.99, 'SQL test book', 'img.jpg', false);

-- Зв’язок
INSERT INTO books_categories (book_id, category_id)
VALUES
    (100, 1);