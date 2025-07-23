package book.shop.spring.boot.intro.repository;

import book.shop.spring.boot.intro.model.User;

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
}