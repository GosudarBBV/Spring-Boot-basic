package book.shop.spring.boot.intro.repository;

import book.shop.spring.boot.intro.config.TestRepositoryConfig;
import book.shop.spring.boot.intro.model.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Import;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Testcontainers
@Import(TestRepositoryConfig.class)
public class OrderItemRepositoryTest {

    @Container
    @ServiceConnection
    static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0")
            .withDatabaseName("test_db")
            .withUsername("test")
            .withPassword("test");

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    @DisplayName("findAllByOrderId should return correct items")
    void findAllByOrderId_ReturnsItems() {
        // Create test user
        User user = TestEntityFactory.createTestUser("user@example.com");
        userRepository.save(user);

        // Create order
        Order order = new Order();
        order.setUser(user);
        order.setStatus(OrderStatus.PENDING);
        orderRepository.save(order);

        // Create order item
        OrderItem item = new OrderItem();
        Book book = new Book();
        book.setTitle("Book 1");
        book.setPrice(BigDecimal.valueOf(50));
        item.setBook(book);
        item.setOrder(order);
        item.setQuantity(2);
        item.setPrice(BigDecimal.valueOf(100));
        orderItemRepository.save(item);

        // when
        List<OrderItem> items = orderItemRepository.findAllByOrderId(order.getId());

        // then
        assertThat(items).hasSize(1);
        assertThat(items.get(0).getQuantity()).isEqualTo(2);
        assertThat(items.get(0).getOrder().getId()).isEqualTo(order.getId());
    }

    @Test
    @DisplayName("findByIdAndOrderIdAndOrderUserId should return correct item")
    void findByIdAndOrderIdAndUserId_ReturnsItem() {
        // Create test user
        User user = TestEntityFactory.createTestUser("user2@example.com");
        userRepository.save(user);

        // Create order
        Order order = new Order();
        order.setUser(user);
        order.setStatus(OrderStatus.PENDING);
        orderRepository.save(order);

        // Create order item
        OrderItem item = new OrderItem();
        Book book = new Book();
        book.setTitle("Book 2");
        book.setPrice(BigDecimal.valueOf(30));
        item.setBook(book);
        item.setOrder(order);
        item.setQuantity(1);
        item.setPrice(BigDecimal.valueOf(30));
        orderItemRepository.save(item);

        Optional<OrderItem> result = orderItemRepository
                .findByIdAndOrderIdAndOrderUserId(item.getId(), order.getId(), user.getId());

        assertThat(result).isPresent();
        assertThat(result.get().getQuantity()).isEqualTo(1);
    }
}
