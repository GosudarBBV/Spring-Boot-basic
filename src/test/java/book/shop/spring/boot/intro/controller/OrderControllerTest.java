package book.shop.spring.boot.intro.controller;

import static org.junit.jupiter.api.Assertions.assertTrue;
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
import book.shop.spring.boot.intro.model.Order;
import book.shop.spring.boot.intro.model.OrderStatus;
import book.shop.spring.boot.intro.model.Role;
import book.shop.spring.boot.intro.model.RoleName;
import book.shop.spring.boot.intro.model.ShoppingCart;
import book.shop.spring.boot.intro.model.User;
import book.shop.spring.boot.intro.repository.OrderRepository;
import book.shop.spring.boot.intro.repository.RoleRepository;
import book.shop.spring.boot.intro.repository.ShoppingCartRepository;
import book.shop.spring.boot.intro.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Collections;
import java.util.HashSet;
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
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private ShoppingCartRepository shoppingCartRepository;

    @Autowired
    private OrderRepository orderRepository;

    private User createUser(String email, RoleName roleName) {
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
        user.setShippingAddress("Test Address");
        user.setRoles(new HashSet<>(Collections.singleton(role)));

        // Save user and return the managed entity
        return userRepository.save(user);
    }

    private void createShoppingCartForUser(User user) {
        // Завантажуємо керований (managed) entity користувача
        User managedUser = userRepository.findById(user.getId())
                .orElseThrow(() -> new IllegalStateException("User not found"));

        ShoppingCart cart = new ShoppingCart();
        cart.setUser(managedUser);
        shoppingCartRepository.save(cart);
    }

    private Long createOrderForUser(User user, OrderRequestDto orderRequestDto) throws Exception {
        createShoppingCartForUser(user);

        // Виконуємо запит на створення замовлення
        String response = mockMvc.perform(post("/orders")
                        .with(csrf())
                        .with(user(user.getEmail()).roles(getRoleName(user)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(orderRequestDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andReturn()
                .getResponse()
                .getContentAsString();

        return objectMapper.readTree(response).get("id").asLong();
    }

    private String getRoleName(User user) {
        return user.getRoles().stream()
                .findFirst()
                .map(role -> role.getName().name())
                .orElse("USER");
    }

    @Test
    @DisplayName("Create order - success")
    void createOrder_ReturnsCreatedOrder() throws Exception {
        User user = createUser("user1@example.com", RoleName.USER);
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
        User user = createUser("user2@example.com", RoleName.USER);
        createOrderForUser(user, new OrderRequestDto("Kyiv"));

        mockMvc.perform(get("/orders")
                        .with(user(user.getEmail()).roles("USER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @DisplayName("Get order items - success")
    void getOrderItems_ReturnsItems() throws Exception {
        User user = createUser("user3@example.com", RoleName.USER);
        Long orderId = createOrderForUser(user, new OrderRequestDto("Kyiv"));

        mockMvc.perform(get("/orders/{orderId}/items", orderId)
                        .with(user(user.getEmail()).roles("USER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @DisplayName("Get specific order item - success")
    void getOrderItem_ReturnsSpecificItem() throws Exception {
        User user = createUser("user4@example.com", RoleName.USER);
        Long orderId = createOrderForUser(user, new OrderRequestDto("Kyiv"));

        mockMvc.perform(get("/orders/{orderId}/items/{itemId}", orderId, 1)
                        .with(user(user.getEmail()).roles("USER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    @DisplayName("Update order status - success")
    void updateOrderStatus_ReturnsOk() throws Exception {
        User admin = createUser("admin@example.com", RoleName.ADMIN);
        User user = createUser("user5@example.com", RoleName.USER);

        Long orderId = createOrderForUser(user, new OrderRequestDto("Kyiv"));
        UpdateOrderStatusRequestDto updateRequest = new UpdateOrderStatusRequestDto(OrderStatus.PENDING.name());

        mockMvc.perform(patch("/orders/{id}", orderId)
                        .with(csrf())
                        .with(user(admin.getEmail()).roles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PENDING"));

        Order updatedOrder = orderRepository.findById(orderId).orElseThrow();
        assertTrue(updatedOrder.getStatus() == OrderStatus.PENDING);
    }
}
