package book.shop.spring.boot.intro.controller;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import book.shop.spring.boot.intro.config.TestSecurityConfig;
import book.shop.spring.boot.intro.dto.OrderRequestDto;
import book.shop.spring.boot.intro.dto.UpdateOrderStatusRequestDto;
import book.shop.spring.boot.intro.model.Book;
import book.shop.spring.boot.intro.model.Order;
import book.shop.spring.boot.intro.model.OrderItem;
import book.shop.spring.boot.intro.model.OrderStatus;
import book.shop.spring.boot.intro.model.User;
import book.shop.spring.boot.intro.repository.BookRepository;
import book.shop.spring.boot.intro.repository.OrderItemRepository;
import book.shop.spring.boot.intro.repository.OrderRepository;
import book.shop.spring.boot.intro.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestSecurityConfig.class)
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    private User createUser(String username) {
        User user = new User();
        user.setEmail(username + "@example.com");
        user.setPassword("password");
        return userRepository.save(user);
    }

    private Book createBook(String title) {
        Book book = new Book();
        book.setTitle(title);
        book.setAuthor("Author");
        book.setIsbn("1234567890");
        book.setPrice(BigDecimal.valueOf(20.00));
        return bookRepository.save(book);
    }

    private Order createOrder(User user, String address) {
        Order order = new Order();
        order.setUser(user);
        order.setShippingAddress(address);
        order.setStatus(OrderStatus.PENDING);
        order.setOrderDate(LocalDateTime.now());
        order.setTotal(BigDecimal.ZERO);
        return orderRepository.save(order);
    }

    @Test
    @DisplayName("Create order - success")
    void placeOrder_ValidRequest_ReturnsCreatedOrder() throws Exception {
        User user = createUser("testuser");
        OrderRequestDto requestDto = new OrderRequestDto("Kyiv");

        mockMvc.perform(post("/orders")
                        .with(csrf())
                        .with(user(user.getEmail()).roles("USER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.shippingAddress").value("Kyiv"))
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    @DisplayName("Get order history - success")
    void getOrderHistory_ShouldReturnPage() throws Exception {
        User user = createUser("historyUser");
        createOrder(user, "Lviv");

        mockMvc.perform(get("/orders")
                        .with(user(user.getEmail()).roles("USER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    @DisplayName("Get order items - success")
    void getItems_ShouldReturnItems() throws Exception {
        User user = createUser("itemsUser");
        Order order = createOrder(user, "Odessa");
        Book book = createBook("Test Book");

        OrderItem orderItem = new OrderItem();
        orderItem.setOrder(order);
        orderItem.setBook(book);
        orderItem.setQuantity(2);
        orderItemRepository.save(orderItem);

        mockMvc.perform(get("/orders/" + order.getId() + "/items")
                        .with(user(user.getEmail()).roles("USER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].bookId").value(book.getId()))
                .andExpect(jsonPath("$[0].quantity").value(2));
    }

    @Test
    @DisplayName("Get specific order item - success")
    void getItem_ShouldReturnSpecificItem() throws Exception {
        User user = createUser("specificItemUser");
        Order order = createOrder(user, "Dnipro");
        Book book = createBook("Specific Book");

        OrderItem orderItem = new OrderItem();
        orderItem.setOrder(order);
        orderItem.setBook(book);
        orderItem.setQuantity(1);
        orderItemRepository.save(orderItem);

        mockMvc.perform(get("/orders/" + order.getId() + "/items/" + orderItem.getId())
                        .with(user(user.getEmail()).roles("USER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(orderItem.getId()))
                .andExpect(jsonPath("$.bookId").value(book.getId()))
                .andExpect(jsonPath("$.quantity").value(1));
    }

    @Test
    @DisplayName("Update order status - success")
    void updateStatus_ShouldUpdateStatus() throws Exception {
        User admin = createUser("adminUser");
        Order order = createOrder(admin, "Kharkiv");

        UpdateOrderStatusRequestDto request = new UpdateOrderStatusRequestDto("PENDING");

        mockMvc.perform(patch("/orders/" + order.getId())
                        .with(csrf())
                        .with(user(admin.getEmail()).roles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PENDING"));
    }
}
