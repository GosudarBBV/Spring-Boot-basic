package book.shop.spring.boot.intro.repository;

import static org.assertj.core.api.Assertions.assertThat;

import book.shop.spring.boot.intro.model.Book;
import book.shop.spring.boot.intro.model.CartItem;
import book.shop.spring.boot.intro.model.ShoppingCart;
import book.shop.spring.boot.intro.model.User;
import book.shop.spring.boot.intro.util.TestEntityFactory;
import java.math.BigDecimal;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.jdbc.Sql;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@DataJpaTest
@Testcontainers
@DisplayName("CartItemRepository integration tests")
@Sql(
        scripts = {
                "classpath:database/schemas/cart-items-schema.sql",
                "classpath:database/shopping-carts/add-shopping-cart-and-items.sql"
        },
        executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD
)
@Sql(
        scripts = "classpath:database/shopping-carts/clear-shopping-cart-and-items.sql",
        executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD
)
class CartItemRepositoryTest {

    @Container
    @ServiceConnection
    static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0.26");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", mysql::getJdbcUrl);
        registry.add("spring.datasource.username", mysql::getUsername);
        registry.add("spring.datasource.password", mysql::getPassword);
    }

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private ShoppingCartRepository shoppingCartRepository;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    @DisplayName("Should find cart item by ID and shopping cart ID")
    void findByIdAndShoppingCartId_ShouldReturnItem() {
        User user = userRepository.save(TestEntityFactory.createTestUser("test@example.com"));

        ShoppingCart cart = new ShoppingCart();
        cart.setUser(user);
        shoppingCartRepository.save(cart);

        Book book = new Book();
        book.setTitle("Test Book");
        book.setAuthor("Author");
        book.setPrice(BigDecimal.valueOf(10));
        bookRepository.save(book);

        CartItem item = new CartItem();
        item.setBook(book);
        item.setQuantity(2);
        item.setShoppingCart(cart);
        cartItemRepository.save(item);

        Optional<CartItem> result = cartItemRepository.findByIdAndShoppingCartId(item.getId(), cart.getId());

        assertThat(result).isPresent();
        assertThat(result.get().getBook().getTitle()).isEqualTo("Test Book");
    }
}
