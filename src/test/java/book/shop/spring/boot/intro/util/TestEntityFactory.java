package book.shop.spring.boot.intro.util;

import book.shop.spring.boot.intro.model.Book;
import book.shop.spring.boot.intro.model.User;
import java.math.BigDecimal;

public class TestEntityFactory {
    public static User createTestUser() {
        return createTestUser("test@example.com");
    }

    public static User createTestUser(String email) {
        User user = new User();
        user.setEmail(email);
        user.setPassword("password");
        user.setFirstName("John");
        user.setLastName("Doe");
        user.setShippingAddress("123 Main St, Kyiv");
        return user;
    }

    public static Book createBook(Long id, String title, BigDecimal price) {
        Book book = new Book();
        book.setId(id);
        book.setTitle(title);
        book.setAuthor("Author Name");
        book.setPrice(price);
        book.setDescription("Description of the book");
        book.setCoverImage("image.jpg");
        return book;
    }
}
