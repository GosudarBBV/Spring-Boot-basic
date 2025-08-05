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
import book.shop.spring.boot.intro.model.*;
import book.shop.spring.boot.intro.repository.BookRepository;
import book.shop.spring.boot.intro.repository.CartItemRepository;
import book.shop.spring.boot.intro.repository.RoleRepository;
import book.shop.spring.boot.intro.repository.ShoppingCartRepository;
import book.shop.spring.boot.intro.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

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

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestSecurityConfig.class)
public class OrderControllerTest {

    private static final String TEST_USER_EMAIL = "user@example.com";
    private static final String TEST_ADMIN_EMAIL = "admin@example.com";

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
    private BookRepository bookRepository;

    @Autowired
    private CartItemRepository cartItemRepository;

    private User testUser;
    private User testAdmin;

    @BeforeEach
    void setUp() {
        // 1. Створити ролі, якщо їх немає
        Role roleUser = roleRepository.findByName(RoleName.USER)
                .orElseGet(() -> {
                    Role r = new Role();
                    r.setName(RoleName.USER);
                    return roleRepository.save(r);
                });

        Role roleAdmin = roleRepository.findByName(RoleName.ADMIN)
                .orElseGet(() -> {
                    Role r = new Role();
                    r.setName(RoleName.ADMIN);
                    return roleRepository.save(r);
                });

        // 2. Створити тестового користувача
        testUser = userRepository.findByEmail(TEST_USER_EMAIL)
                .orElseGet(() -> {
                    User u = new User();
                    u.setEmail(TEST_USER_EMAIL);
                    u.setPassword("{noop}password");
                    u.setFirstName("Test");
                    u.setLastName("User");
                    u.setRoles(Set.of(roleUser));
                    return userRepository.save(u); // SAVE — зробить user managed
                });

        testAdmin = userRepository.findByEmail(TEST_ADMIN_EMAIL)
                .orElseGet(() -> {
                    User u = new User();
                    u.setEmail(TEST_ADMIN_EMAIL);
                    u.setPassword("{noop}adminpassword");
                    u.setFirstName("Admin");
                    u.setLastName("User");
                    u.setRoles(Set.of(roleAdmin));
                    return userRepository.save(u);
                });

        // Оновити testUser з бази, щоб бути впевненим, що це managed entity
        testUser = userRepository.findById(testUser.getId()).orElseThrow();

        // 3. Створити кошик
        ShoppingCart cart = shoppingCartRepository.findByUserId(testUser.getId())
                .orElseGet(() -> {
                    ShoppingCart c = new ShoppingCart();
                    c.setUser(testUser); // Важливо: managed entity
                    return shoppingCartRepository.save(c);
                });

        // 4. Створити книгу
        Book book = bookRepository.findAll().stream().findFirst().orElseGet(() -> {
            Book b = new Book();
            b.setTitle("Test Book");
            b.setAuthor("Test Author");
            b.setPrice(BigDecimal.valueOf(100));
            return bookRepository.save(b);
        });

        // 5. Додати CartItem, якщо потрібно
        boolean cartItemExists = cartItemRepository.findAll().stream()
                .anyMatch(ci -> ci.getShoppingCart().getId().equals(cart.getId())
                        && ci.getBook().getId().equals(book.getId()));

        if (!cartItemExists) {
            CartItem cartItem = new CartItem();
            cartItem.setShoppingCart(cart); // також managed entity
            cartItem.setBook(book);
            cartItem.setQuantity(1);
            cartItemRepository.save(cartItem);
        }
    }

    @Test
    @DisplayName("Place order - success for USER role")
    void placeOrder_asUser_shouldReturnCreatedOrder() throws Exception {
        OrderRequestDto requestDto = new OrderRequestDto("123 Test St, Kyiv");

        mockMvc.perform(post("/orders")
                        .with(csrf())
                        .with(user(TEST_USER_EMAIL).roles("USER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.shippingAddress").value("123 Test St, Kyiv"))
                .andExpect(jsonPath("$.id").isNumber());
    }

    @Test
    @DisplayName("Get order history - success for USER role")
    void getOrderHistory_asUser_shouldReturnPagedOrders() throws Exception {
        mockMvc.perform(get("/orders")
                        .with(user(TEST_USER_EMAIL).roles("USER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    @DisplayName("Get order items - success for USER role")
    void getOrderItems_asUser_shouldReturnList() throws Exception {
        mockMvc.perform(get("/orders/1/items")
                        .with(user(TEST_USER_EMAIL).roles("USER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @DisplayName("Get order item by ID - success for USER role")
    void getOrderItem_asUser_shouldReturnItem() throws Exception {
        mockMvc.perform(get("/orders/1/items/1")
                        .with(user(TEST_USER_EMAIL).roles("USER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    @DisplayName("Update order status - success for ADMIN role")
    void updateOrderStatus_asAdmin_shouldUpdateStatus() throws Exception {
        UpdateOrderStatusRequestDto requestDto = new UpdateOrderStatusRequestDto(OrderStatus.COMPLETED.name());

        mockMvc.perform(patch("/orders/1")
                        .with(csrf())
                        .with(user(TEST_ADMIN_EMAIL).roles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(OrderStatus.COMPLETED.name()));
    }

    @Test
    @DisplayName("Update order status - forbidden for USER role")
    void updateOrderStatus_asUser_shouldBeForbidden() throws Exception {
        UpdateOrderStatusRequestDto requestDto = new UpdateOrderStatusRequestDto(OrderStatus.COMPLETED.name());

        mockMvc.perform(patch("/orders/1")
                        .with(csrf())
                        .with(user(TEST_USER_EMAIL).roles("USER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isForbidden());
    }
}
