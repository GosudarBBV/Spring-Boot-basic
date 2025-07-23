package book.shop.spring.boot.intro.repository;

import book.shop.spring.boot.intro.model.ShoppingCart;
import book.shop.spring.boot.intro.model.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Testcontainers
class ShoppingCartRepositoryTest {

    @Container
    @ServiceConnection
    static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0");

    @Autowired
    private ShoppingCartRepository cartRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    @DisplayName("findByUserId should return cart for user")
    void findByUserId_ReturnsCart() {
        User user = TestEntityFactory.createTestUser();
        userRepository.save(user);

        ShoppingCart cart = new ShoppingCart();
        cart.setUser(user);
        cartRepository.save(cart);

        Optional<ShoppingCart> result = cartRepository.findByUserId(user.getId());

        assertThat(result).isPresent();
        assertThat(result.get().getUser().getEmail()).isEqualTo("test@cart.com");
    }

    @Test
    @DisplayName("existsByUserId should return true if cart exists")
    void existsByUserId_ReturnsTrue() {
        User user = TestEntityFactory.createTestUser();
        userRepository.save(user);

        ShoppingCart cart = new ShoppingCart();
        cart.setUser(user);
        cartRepository.save(cart);

        boolean exists = cartRepository.existsByUserId(user.getId());

        assertThat(exists).isTrue();
    }
}
