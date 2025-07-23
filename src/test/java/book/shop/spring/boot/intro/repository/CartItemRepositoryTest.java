package book.shop.spring.boot.intro.repository;

import book.shop.spring.boot.intro.config.TestRepositoryConfig;
import book.shop.spring.boot.intro.model.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Testcontainers
@Import(TestRepositoryConfig.class)
public class CartItemRepositoryTest {

    @Container
    @ServiceConnection
    static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0.26");

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private ShoppingCartRepository shoppingCartRepository;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private UserRepository userRepository;

    @DynamicPropertySource
    static void overrideProps(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", mysql::getJdbcUrl);
        registry.add("spring.datasource.username", mysql::getUsername);
        registry.add("spring.datasource.password", mysql::getPassword);
    }

    @Test
    @DisplayName("Find cart item by id and shopping cart id")
    void findByIdAndShoppingCartId_ReturnsItem() {
        User user = TestEntityFactory.createTestUser();
        userRepository.save(user);

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

        Optional<CartItem> result = cartItemRepository.findByIdAndShoppingCartId(
                item.getId(), cart.getId()
        );

        assertThat(result).isPresent();
        assertThat(result.get().getBook().getTitle()).isEqualTo("Test Book");
    }
}
