package book.shop.spring.boot.intro.repository;

import book.shop.spring.boot.intro.config.TestRepositoryConfig;
import book.shop.spring.boot.intro.model.Order;
import book.shop.spring.boot.intro.model.OrderStatus;
import book.shop.spring.boot.intro.model.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Testcontainers
@Import(TestRepositoryConfig.class)
class OrderRepositoryTest {

    @Container
    @ServiceConnection
    static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0");

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    @DisplayName("findAllByUserId should return all orders for user")
    void findAllByUserId_ShouldReturnOrders() {
        // given
        User user = TestEntityFactory.createTestUser();
        userRepository.save(user);

        Order order1 = new Order();
        order1.setUser(user);
        order1.setStatus(OrderStatus.PENDING);
        order1.setOrderDate(LocalDateTime.now());
        order1.setShippingAddress("Kyiv");
        order1.setTotal(BigDecimal.ZERO);
        orderRepository.save(order1);

        Order order2 = new Order();
        order2.setUser(user);
        order2.setStatus(OrderStatus.DELIVERED);
        order2.setOrderDate(LocalDateTime.now());
        order2.setShippingAddress("Kyiv");
        order2.setTotal(BigDecimal.ZERO);
        orderRepository.save(order2);

        // when
        Page<Order> orders = orderRepository.findAllByUserId(user.getId(), PageRequest.of(0, 10));

        // then
        assertThat(orders).hasSize(2);
        assertThat(orders.getContent()).extracting("user.id").containsOnly(user.getId());
    }

    @Test
    @DisplayName("findByIdAndUserId should return correct order")
    void findByIdAndUserId_ShouldReturnOrder() {
        // given
        User user = new User();
        user.setEmail("john@example.com");
        user.setPassword("secret");
        userRepository.save(user);

        Order order = new Order();
        order.setUser(user);
        order.setStatus(OrderStatus.PENDING);
        orderRepository.save(order);

        // when
        Optional<Order> result = orderRepository.findByIdAndUserId(order.getId(), user.getId());

        // then
        assertThat(result).isPresent();
        assertThat(result.get().getUser().getId()).isEqualTo(user.getId());
    }

    @Test
    @DisplayName("findByIdAndUserId should return empty if user doesn't match")
    void findByIdAndUserId_ShouldReturnEmpty_WhenUserMismatch() {
        // given
        User user1 = new User();
        user1.setEmail("user1@example.com");
        user1.setPassword("1");
        userRepository.save(user1);

        User user2 = new User();
        user2.setEmail("user2@example.com");
        user2.setPassword("2");
        userRepository.save(user2);

        Order order = new Order();
        order.setUser(user1);
        order.setStatus(OrderStatus.PENDING);
        orderRepository.save(order);

        // when
        Optional<Order> result = orderRepository.findByIdAndUserId(order.getId(), user2.getId());

        // then
        assertThat(result).isEmpty();
    }
}
