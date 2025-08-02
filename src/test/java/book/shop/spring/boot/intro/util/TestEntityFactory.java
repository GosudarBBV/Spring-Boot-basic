package book.shop.spring.boot.intro.util;

import book.shop.spring.boot.intro.model.Book;
import book.shop.spring.boot.intro.model.CartItem;
import book.shop.spring.boot.intro.model.Category;
import book.shop.spring.boot.intro.model.Order;
import book.shop.spring.boot.intro.model.OrderItem;
import book.shop.spring.boot.intro.model.OrderStatus;
import book.shop.spring.boot.intro.model.ShoppingCart;
import book.shop.spring.boot.intro.model.User;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

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

    public static Category createCategory(Long id, String name, String description) {
        Category category = new Category();
        category.setId(id);
        category.setName(name);
        category.setDescription(description);
        category.setDeleted(false); // soft-delete default
        return category;
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

    public static ShoppingCart createCart(User user, Set<CartItem> items) {
        ShoppingCart cart = new ShoppingCart();
        cart.setUser(user);
        cart.setCartItems(items != null ? items : new HashSet<>());
        return cart;
    }
}
