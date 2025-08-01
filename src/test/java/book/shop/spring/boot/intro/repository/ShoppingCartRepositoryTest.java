package book.shop.spring.boot.intro.repository;

import static org.assertj.core.api.Assertions.assertThat;

import book.shop.spring.boot.intro.model.ShoppingCart;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.jdbc.Sql;
import org.testcontainers.junit.jupiter.Testcontainers;

@DataJpaTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Sql(scripts = "classpath:database/schemas/shopping-carts-schema.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = "classpath:database/shopping-carts/add-shopping-carts.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = "classpath:database/shopping-carts/clear-shopping-carts.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
class ShoppingCartRepositoryTest {

    @Autowired
    private ShoppingCartRepository cartRepository;

    @Test
    @DisplayName("findByUserId should return cart for user")
    void findByUserId_ReturnsCart() {
        Optional<ShoppingCart> result = cartRepository.findByUserId(1L);

        assertThat(result).isPresent();
        assertThat(result.get().getUser().getEmail()).isEqualTo("test@cart.com");
    }

    @Test
    @DisplayName("existsByUserId should return true if cart exists")
    void existsByUserId_ReturnsTrue() {
        boolean exists = cartRepository.existsByUserId(2L);

        assertThat(exists).isTrue();
    }
}
