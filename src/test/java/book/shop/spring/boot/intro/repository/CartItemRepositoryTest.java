package book.shop.spring.boot.intro.repository;

import static org.assertj.core.api.Assertions.assertThat;

import book.shop.spring.boot.intro.model.CartItem;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.jdbc.Sql;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@DisplayName("CartItemRepository integration tests")
@Sql(scripts = "classpath:database/schemas/cart-items-schema.sql",
        executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = {
        "classpath:database/cart-items/clear-shopping-cart-and-items.sql"
}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = {
        "classpath:database/cart-items/add-shopping-cart-and-items.sql"
}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = {
        "classpath:database/cart-items/clear-shopping-cart-and-items.sql"
}, executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
public class CartItemRepositoryTest {

    @Autowired
    private CartItemRepository cartItemRepository;

    @Test
    @DisplayName("Find cart item by ID and shopping cart ID")
    void findByIdAndShoppingCartId_ValidData_ReturnsItem() {
        Long cartId = 1L;
        Long cartItemId = 1L;

        Optional<CartItem> result = cartItemRepository.findByIdAndShoppingCartId(cartItemId, cartId);

        assertThat(result).isPresent();
        assertThat(result.get().getBook().getTitle()).isEqualTo("Test Book");
    }
}
