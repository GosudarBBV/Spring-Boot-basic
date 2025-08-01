package book.shop.spring.boot.intro.repository;

import static org.assertj.core.api.Assertions.assertThat;

import book.shop.spring.boot.intro.model.Order;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.jdbc.Sql;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Sql(scripts = "classpath:database/schemas/orders-schema.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = "classpath:database/orders/add-orders-data.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = "classpath:database/orders/clear-orders-data.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
class OrderRepositoryTest {

    @Autowired
    private OrderRepository orderRepository;

    @Test
    @DisplayName("findAllByUserId should return all orders for user")
    void findAllByUserId_ShouldReturnOrders() {
        Page<Order> orders = orderRepository.findAllByUserId(1L, PageRequest.of(0, 10));

        assertThat(orders).hasSize(2);
        assertThat(orders.getContent())
                .extracting(order -> order.getUser().getId())
                .containsOnly(1L);
    }

    @Test
    @DisplayName("findByIdAndUserId should return correct order")
    void findByIdAndUserId_ShouldReturnOrder() {
        Optional<Order> result = orderRepository.findByIdAndUserId(3L, 2L);

        assertThat(result).isPresent();
        assertThat(result.get().getUser().getId()).isEqualTo(2L);
    }

    @Test
    @DisplayName("findByIdAndUserId should return empty if user doesn't match")
    void findByIdAndUserId_ShouldReturnEmpty_WhenUserMismatch() {
        Optional<Order> result = orderRepository.findByIdAndUserId(3L, 3L);

        assertThat(result).isEmpty();
    }
}
