package book.shop.spring.boot.intro.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import book.shop.spring.boot.intro.config.TestSecurityConfig;
import book.shop.spring.boot.intro.dto.OrderItemDto;
import book.shop.spring.boot.intro.dto.OrderRequestDto;
import book.shop.spring.boot.intro.dto.UpdateOrderStatusRequestDto;
import book.shop.spring.boot.intro.model.Order;
import book.shop.spring.boot.intro.model.OrderItem;
import book.shop.spring.boot.intro.model.OrderStatus;
import book.shop.spring.boot.intro.model.Role;
import book.shop.spring.boot.intro.model.RoleName;
import book.shop.spring.boot.intro.model.ShoppingCart;
import book.shop.spring.boot.intro.model.User;
import book.shop.spring.boot.intro.repository.OrderItemRepository;
import book.shop.spring.boot.intro.repository.OrderRepository;
import book.shop.spring.boot.intro.repository.RoleRepository;
import book.shop.spring.boot.intro.repository.ShoppingCartRepository;
import book.shop.spring.boot.intro.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
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
public class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private ShoppingCartRepository shoppingCartRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    private User createUserWithRole(String email, RoleName roleName) {
        Role role = roleRepository.findByName(roleName)
                .orElseGet(() -> {
                    Role newRole = new Role();
                    newRole.setName(roleName);
                    return roleRepository.save(newRole);
                });

        User user = new User();
        user.setEmail(email);
        user.setPassword("password");
        user.setFirstName("Test");
        user.setLastName("User");
        user.setShippingAddress("Test address");
        user.setRoles(new HashSet<>(Collections.singleton(role)));
        return userRepository.save(user);
    }

    private void createShoppingCartForUser(User user) {
        ShoppingCart cart = new ShoppingCart();
        cart.setUser(user);
        shoppingCartRepository.save(cart);
    }

    private Long createOrderForUser(User user, String shippingAddress) {
        Order order = new Order();
        order.setUser(user);
        order.setShippingAddress(shippingAddress);
        order.setOrderDate(LocalDateTime.now());
        order.setStatus(OrderStatus.PENDING);
        order.setTotal(new BigDecimal("100.00"));
        order = orderRepository.save(order);
        return order.getId();
    }

    private void addOrderItem(Order order, Long bookId, int quantity) {
        OrderItem item = new OrderItem();
        item.setOrder(order);
        item.setQuantity(quantity);
        orderItemRepository.save(item);
    }

    @BeforeEach
    void cleanDb() {
        orderItemRepository.deleteAll();
        orderRepository.deleteAll();
        shoppingCartRepository.deleteAll();
        userRepository.deleteAll();
        roleRepository.deleteAll();
    }

    @Test
    @DisplayName("Create order - success")
    void createOrder_ReturnsCreatedOrder() throws Exception {
        User user = createUserWithRole("user1@example.com", RoleName.USER);
        createShoppingCartForUser(user);

        OrderRequestDto request = new OrderRequestDto("Kyiv");

        mockMvc.perform(post("/orders")
                        .with(csrf())
                        .with(user(user.getEmail()).roles("USER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.shippingAddress").value("Kyiv"))
                .andExpect(jsonPath("$.id").isNumber());
    }

    @Test
    @DisplayName("Get order history - success")
    void getOrderHistory_ReturnsOrderList() throws Exception {
        User user = createUserWithRole("user2@example.com", RoleName.USER);
        createShoppingCartForUser(user);
        Long orderId = createOrderForUser(user, "Kyiv");

        mockMvc.perform(get("/orders")
                        .with(user(user.getEmail()).roles("USER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(orderId))
                .andExpect(jsonPath("$[0].shippingAddress").value("Kyiv"));
    }

    @Test
    @DisplayName("Get order items - success")
    void getOrderItems_ReturnsItems() throws Exception {
        User user = createUserWithRole("user3@example.com", RoleName.USER);
        createShoppingCartForUser(user);
        Long orderId = createOrderForUser(user, "Kyiv");

        Order order = orderRepository.findById(orderId).orElseThrow();
        addOrderItem(order, 5L, 2);
        addOrderItem(order, 6L, 1);

        mockMvc.perform(get("/orders/" + orderId + "/items")
                        .with(user(user.getEmail()).roles("USER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].bookId").value(5))
                .andExpect(jsonPath("$[0].quantity").value(2))
                .andExpect(jsonPath("$[1].bookId").value(6))
                .andExpect(jsonPath("$[1].quantity").value(1));
    }

    @Test
    @DisplayName("Get specific order item - success")
    void getSpecificOrderItem_ReturnsItem() throws Exception {
        User user = createUserWithRole("user4@example.com", RoleName.USER);
        createShoppingCartForUser(user);
        Long orderId = createOrderForUser(user, "Kyiv");

        Order order = orderRepository.findById(orderId).orElseThrow();
        addOrderItem(order, 7L, 1);
        OrderItem item = orderItemRepository.findAll().get(0);

        mockMvc.perform(get("/orders/" + orderId + "/items/" + item.getId())
                        .with(user(user.getEmail()).roles("USER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.bookId").value(7))
                .andExpect(jsonPath("$.quantity").value(1));
    }

    @Test
    @DisplayName("Update order status - success")
    void updateOrderStatus_ReturnsOk() throws Exception {
        User admin = createUserWithRole("admin@example.com", RoleName.ADMIN);
        User user = createUserWithRole("user5@example.com", RoleName.USER);
        createShoppingCartForUser(user);

        Long orderId = createOrderForUser(user, "Kyiv");

        UpdateOrderStatusRequestDto updateRequest = new UpdateOrderStatusRequestDto(OrderStatus.PENDING.name());

        mockMvc.perform(patch("/orders/" + orderId)
                        .with(csrf())
                        .with(user(admin.getEmail()).roles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PENDING"));

        Order updatedOrder = orderRepository.findById(orderId).orElseThrow();
        assertThat(updatedOrder.getStatus()).isEqualTo(OrderStatus.PENDING);
    }
}
