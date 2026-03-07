SET FOREIGN_KEY_CHECKS = 0;

INSERT INTO books (id, title, author, isbn, price, description, cover_image, is_deleted) VALUES
(1, 'Test Book 1', 'Test Author 1', '9783161484100', 19.99, 'Description for Test Book 1', 'http://example.com/cover1.jpg', false),
(2, 'Test Book 2', 'Test Author 2', '9783161484101', 29.99, 'Description for Test Book 2', 'http://example.com/cover2.jpg', false);

SET FOREIGN_KEY_CHECKS = 1;
