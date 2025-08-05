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
import book.shop.spring.boot.intro.repository.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.util.Set;

import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
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
@Transactional
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

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private OrderRepository orderRepository;


    @BeforeEach
    void cleanDatabase() {
        cartItemRepository.deleteAll();
        shoppingCartRepository.deleteAll();
        orderRepository.deleteAll();
        userRepository.deleteAll();
        roleRepository.deleteAll();
        bookRepository.deleteAll();

        entityManager.flush();
        entityManager.clear();
    }

    private User createUserIfNotExist(String email, RoleName roleName, String password, String firstName, String lastName) {
        Role role = roleRepository.findByName(roleName)
                .orElseGet(() -> {
                    Role r = new Role();
                    r.setName(roleName);
                    return roleRepository.save(r);
                });
        return userRepository.findByEmail(email)
                .orElseGet(() -> {
                    User u = new User();
                    u.setEmail(email);
                    u.setPassword("{noop}" + password);
                    u.setFirstName(firstName);
                    u.setLastName(lastName);
                    u.setRoles(Set.of(role));
                    return userRepository.save(u);
                });
    }

    private void prepareTestDataForUser(User user) {
        User managedUser = userRepository.findById(user.getId()).orElseThrow();

        ShoppingCart cart = shoppingCartRepository.findByUserId(managedUser.getId())
                .orElseGet(() -> {
                    ShoppingCart c = new ShoppingCart();
                    c.setUser(managedUser);
                    return shoppingCartRepository.save(c);
                });

        Book book = bookRepository.findAll().stream().findFirst().orElseGet(() -> {
            Book b = new Book();
            b.setTitle("Test Book");
            b.setAuthor("Test Author");
            b.setPrice(BigDecimal.valueOf(100));
            return bookRepository.save(b);
        });

        boolean cartItemExists = cartItemRepository.findAll().stream()
                .anyMatch(ci -> ci.getShoppingCart().getId().equals(cart.getId())
                        && ci.getBook().getId().equals(book.getId()));

        if (!cartItemExists) {
            CartItem cartItem = new CartItem();
            cartItem.setShoppingCart(cart);
            cartItem.setBook(book);
            cartItem.setQuantity(1);
            cartItemRepository.save(cartItem);
        }
    }

    @Test
    @DisplayName("Place order - success for USER role")
    void placeOrder_asUser_shouldReturnCreatedOrder() throws Exception {
        User testUser = createUserIfNotExist(TEST_USER_EMAIL, RoleName.USER, "password", "Test", "User");
        prepareTestDataForUser(testUser);

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
        User testUser = createUserIfNotExist(TEST_USER_EMAIL, RoleName.USER, "password", "Test", "User");
        prepareTestDataForUser(testUser);

        mockMvc.perform(get("/orders")
                        .with(user(TEST_USER_EMAIL).roles("USER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    @DisplayName("Get order items - success for USER role")
    void getOrderItems_asUser_shouldReturnList() throws Exception {
        User testUser = createUserIfNotExist(TEST_USER_EMAIL, RoleName.USER, "password", "Test", "User");
        prepareTestDataForUser(testUser);

        mockMvc.perform(get("/orders/1/items")
                        .with(user(TEST_USER_EMAIL).roles("USER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @DisplayName("Get order item by ID - success for USER role")
    void getOrderItem_asUser_shouldReturnItem() throws Exception {
        User testUser = createUserIfNotExist(TEST_USER_EMAIL, RoleName.USER, "password", "Test", "User");
        prepareTestDataForUser(testUser);

        mockMvc.perform(get("/orders/1/items/1")
                        .with(user(TEST_USER_EMAIL).roles("USER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    @DisplayName("Update order status - success for ADMIN role")
    void updateOrderStatus_asAdmin_shouldUpdateStatus() throws Exception {
        User testAdmin = createUserIfNotExist(TEST_ADMIN_EMAIL, RoleName.ADMIN, "adminpassword", "Admin", "User");
        // Не обов’язково створювати кошик/книгу для цього тесту

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
        User testUser = createUserIfNotExist(TEST_USER_EMAIL, RoleName.USER, "password", "Test", "User");
        // Не обов’язково створювати кошик/книгу для цього тесту

        UpdateOrderStatusRequestDto requestDto = new UpdateOrderStatusRequestDto(OrderStatus.COMPLETED.name());

        mockMvc.perform(patch("/orders/1")
                        .with(csrf())
                        .with(user(TEST_USER_EMAIL).roles("USER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isForbidden());
    }
}
