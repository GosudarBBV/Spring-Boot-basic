INSERT INTO books (title, author, price, description, isbn, cover_image, is_deleted)
VALUES ('Test Book', 'Author', 19.99, 'A test description', '1234567890123', 'cover.jpg', false);

INSERT INTO books_categories (book_id, category_id)
VALUES (
    (SELECT id FROM books WHERE title = 'Test Book'),
    (SELECT id FROM categories WHERE name = 'Fiction')
);