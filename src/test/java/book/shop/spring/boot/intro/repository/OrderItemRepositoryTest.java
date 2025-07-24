package book.shop.spring.boot.intro.repository;

import static org.assertj.core.api.Assertions.assertThat;

import book.shop.spring.boot.intro.config.TestRepositoryConfig;
import book.shop.spring.boot.intro.model.Book;
import book.shop.spring.boot.intro.model.Order;
import book.shop.spring.boot.intro.model.OrderItem;
import book.shop.spring.boot.intro.model.OrderStatus;
import book.shop.spring.boot.intro.model.User;
import book.shop.spring.boot.intro.util.TestEntityFactory;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Import;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@DataJpaTest
@Testcontainers
@Import(TestRepositoryConfig.class)
public class OrderItemRepositoryTest {

    @Container
    @ServiceConnection
    static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0");

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    @DisplayName("findAllByOrderId should return correct items")
    void findAllByOrderId_ReturnsItems() {
        User user = TestEntityFactory.createTestUser("user@example.com");
        userRepository.save(user);

        Order order = new Order();
        order.setUser(user);
        order.setStatus(OrderStatus.PENDING);
        order.setOrderDate(LocalDateTime.now());
        order.setShippingAddress("Kyiv");
        order.setTotal(BigDecimal.ZERO);
        orderRepository.save(order);

        Book book = new Book();
        book.setTitle("Book 1");
        book.setAuthor("Author");
        book.setPrice(BigDecimal.valueOf(50));
        book.setIsbn("1234567890123");
        book.setDescription("Test description");
        book.setCoverImage("cover.jpg");
        book.setDeleted(false);
        bookRepository.save(book);

        OrderItem item = new OrderItem();
        item.setBook(book);
        item.setOrder(order);
        item.setQuantity(2);
        item.setPrice(BigDecimal.valueOf(100));
        orderItemRepository.save(item);

        List<OrderItem> items = orderItemRepository.findAllByOrderId(order.getId());

        assertThat(items).hasSize(1);
        assertThat(items.get(0).getQuantity()).isEqualTo(2);
        assertThat(items.get(0).getOrder().getId()).isEqualTo(order.getId());
    }

    @Test
    @DisplayName("findByIdAndOrderIdAndOrderUserId should return correct item")
    void findByIdAndOrderIdAndUserId_ReturnsItem() {
        User user = TestEntityFactory.createTestUser("user2@example.com");
        userRepository.save(user);

        Order order = new Order();
        order.setUser(user);
        order.setStatus(OrderStatus.PENDING);
        order.setOrderDate(LocalDateTime.now());
        order.setShippingAddress("Kyiv");
        order.setTotal(BigDecimal.ZERO);
        orderRepository.save(order);

        Book book = new Book();
        book.setTitle("Book 1");
        book.setAuthor("Author");
        book.setPrice(BigDecimal.valueOf(50));
        book.setIsbn("1234567890123");
        book.setDescription("Test description");
        book.setCoverImage("cover.jpg");
        book.setDeleted(false);
        bookRepository.save(book);

        OrderItem item = new OrderItem();
        item.setBook(book);
        item.setOrder(order);
        item.setQuantity(2);
        item.setPrice(BigDecimal.valueOf(100));
        orderItemRepository.save(item);

        Optional<OrderItem> result = orderItemRepository
                .findByIdAndOrderIdAndOrderUserId(item.getId(),
                        order.getId(), user.getId());

        assertThat(result).isPresent();
        assertThat(result.get().getQuantity()).isEqualTo(2);
    }
}
