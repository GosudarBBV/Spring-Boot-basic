package book.shop.spring.boot.intro.repository;

import static org.assertj.core.api.Assertions.assertThat;

import book.shop.spring.boot.intro.model.OrderItem;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.jdbc.Sql;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Sql(scripts = "classpath:database/schemas/order-items-schema.sql",
        executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = "classpath:database/order-items/clear-order-items-data.sql",
        executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = "classpath:database/order-items/add-order-items-data.sql",
        executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = "classpath:database/order-items/clear-order-items-data.sql",
        executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
class OrderItemRepositoryTest {

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Test
    @DisplayName("findAllByOrderId should return correct items")
    void findAllByOrderId_ReturnsItems() {
        List<OrderItem> items = orderItemRepository.findAllByOrderId(1L);

        assertThat(items).hasSize(1);
        assertThat(items.get(0).getQuantity()).isEqualTo(2);
        assertThat(items.get(0).getOrder().getId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("findByIdAndOrderIdAndOrderUserId should return correct item")
    void findByIdAndOrderIdAndUserId_ReturnsItem() {
        Optional<OrderItem> result = orderItemRepository.findByIdAndOrderIdAndOrderUserId(1L, 1L, 1L);

        assertThat(result).isPresent();
        assertThat(result.get().getQuantity()).isEqualTo(2);
    }
}
