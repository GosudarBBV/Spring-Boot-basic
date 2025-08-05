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
import book.shop.spring.boot.intro.dto.CreateUserRequestDto;
import book.shop.spring.boot.intro.dto.CreateBookRequestDto;
import book.shop.spring.boot.intro.dto.AddCartItemRequestDto;
import book.shop.spring.boot.intro.model.Role;
import book.shop.spring.boot.intro.model.RoleName;
import book.shop.spring.boot.intro.model.ShoppingCart;
import book.shop.spring.boot.intro.model.User;
import book.shop.spring.boot.intro.repository.RoleRepository;
import book.shop.spring.boot.intro.repository.ShoppingCartRepository;
import book.shop.spring.boot.intro.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
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

    private static final String EMAIL_USER = "test_user@example.com";
    private static final String EMAIL_ADMIN = "test_admin@example.com";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ShoppingCartRepository shoppingCartRepository;

    @Autowired
    private RoleRepository roleRepository;

    @BeforeEach
    void setupFakeUser() {
        Role userRole = roleRepository.findByName(RoleName.USER)
                .orElseGet(() -> {
                    Role newRole = new Role();
                    newRole.setName(RoleName.USER);
                    return roleRepository.save(newRole);
                });

        // Перевіряємо, чи користувач уже є, щоб уникнути дублювання
        userRepository.findByEmail(EMAIL_USER).orElseGet(() -> {
            User user = new User();
            user.setEmail(EMAIL_USER);
            user.setPassword("{noop}password"); // або як у вас хешується пароль
            user.setFirstName("Fake");
            user.setLastName("User");
            user.getRoles().add(userRole);
            return userRepository.save(user);
        });

        createCartForUser(EMAIL_USER);
    }

    private Long createUser(String email, String password, String firstName, String lastName, String role) throws Exception {
        CreateUserRequestDto userDto = new CreateUserRequestDto(email, password, password, firstName, lastName, role);
        String response = mockMvc.perform(post("/auth/registration")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDto)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        // Створюємо кошик після створення користувача
        createCartForUser(email);

        return objectMapper.readTree(response).get("id").asLong();
    }

    private void createCartForUser(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found: " + email));

        // Переконаємось, що user — managed entity (підключений до сесії)
        User managedUser = userRepository.findById(user.getId())
                .orElseThrow(() -> new RuntimeException("User not found by id: " + user.getId()));

        boolean cartExists = shoppingCartRepository.existsByUserId(managedUser.getId());
        if (!cartExists) {
            ShoppingCart cart = new ShoppingCart();
            cart.setUser(managedUser);
            shoppingCartRepository.save(cart);
        }
    }

    private Long createBook(String title, String author, double price) throws Exception {
        CreateBookRequestDto bookDto = new CreateBookRequestDto(
                title,
                author,
                null,
                BigDecimal.valueOf(price),
                null,
                null,
                List.of()
        );
        String response = mockMvc.perform(post("/books")
                        .with(csrf())
                        .with(user("admin").roles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bookDto)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(response).get("id").asLong();
    }

    private void addBookToCart(String userEmail, Long bookId) throws Exception {
        AddCartItemRequestDto addDto = new AddCartItemRequestDto(bookId, 1);
        mockMvc.perform(post("/cart")
                        .with(csrf())
                        .with(user(userEmail).roles("USER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(addDto)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Place order - success for USER role")
    void placeOrder_asUser_shouldReturnCreatedOrder() throws Exception {
        String email = "user@example.com";
        createUser(email, "password", "Test", "User", "USER");
        Long bookId = createBook("Test Book", "Test Author", 100);

        addBookToCart(email, bookId);

        OrderRequestDto orderRequest = new OrderRequestDto("123 Test St, Kyiv");
        mockMvc.perform(post("/orders")
                        .with(csrf())
                        .with(user(email).roles("USER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(orderRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.shippingAddress").value("123 Test St, Kyiv"))
                .andExpect(jsonPath("$.id").isNumber());
    }

    @Test
    @DisplayName("Get order history - success for USER role")
    void getOrderHistory_asUser_shouldReturnPagedOrders() throws Exception {
        String email = "user@example.com";
        createUser(email, "password", "Test", "User", "USER");
        Long bookId = createBook("Test Book", "Test Author", 100);

        addBookToCart(email, bookId);

        OrderRequestDto orderRequest = new OrderRequestDto("123 Test St, Kyiv");
        mockMvc.perform(post("/orders")
                        .with(csrf())
                        .with(user(email).roles("USER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(orderRequest)))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/orders")
                        .with(user(email).roles("USER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    @DisplayName("Get order items - success for USER role")
    void getOrderItems_asUser_shouldReturnList() throws Exception {
        String email = "user@example.com";
        createUser(email, "password", "Test", "User", "USER");
        Long bookId = createBook("Test Book", "Test Author", 100);

        addBookToCart(email, bookId);

        OrderRequestDto orderRequest = new OrderRequestDto("123 Test St, Kyiv");
        String orderResponse = mockMvc.perform(post("/orders")
                        .with(csrf())
                        .with(user(email).roles("USER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(orderRequest)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        Long orderId = objectMapper.readTree(orderResponse).get("id").asLong();

        mockMvc.perform(get("/orders/" + orderId + "/items")
                        .with(user(email).roles("USER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @DisplayName("Get order item by ID - success for USER role")
    void getOrderItem_asUser_shouldReturnItem() throws Exception {
        String email = "user@example.com";
        createUser(email, "password", "Test", "User", "USER");
        Long bookId = createBook("Test Book", "Test Author", 100);

        addBookToCart(email, bookId);

        OrderRequestDto orderRequest = new OrderRequestDto("123 Test St, Kyiv");
        String orderResponse = mockMvc.perform(post("/orders")
                        .with(csrf())
                        .with(user(email).roles("USER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(orderRequest)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        Long orderId = objectMapper.readTree(orderResponse).get("id").asLong();

        String itemsResponse = mockMvc.perform(get("/orders/" + orderId + "/items")
                        .with(user(email).roles("USER")))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        Long itemId = objectMapper.readTree(itemsResponse).get(0).get("id").asLong();

        mockMvc.perform(get("/orders/" + orderId + "/items/" + itemId)
                        .with(user(email).roles("USER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(itemId));
    }

    @Test
    @DisplayName("Update order status - success for ADMIN role")
    void updateOrderStatus_asAdmin_shouldUpdateStatus() throws Exception {
        String adminEmail = "admin@example.com";
        createUser(adminEmail, "adminpassword", "Admin", "User", "ADMIN");

        String userEmail = "user@example.com";
        createUser(userEmail, "password", "Test", "User", "USER");
        Long bookId = createBook("Test Book", "Test Author", 100);
        addBookToCart(userEmail, bookId);
        OrderRequestDto orderRequest = new OrderRequestDto("123 Test St, Kyiv");
        String orderResponse = mockMvc.perform(post("/orders")
                        .with(csrf())
                        .with(user(userEmail).roles("USER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(orderRequest)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        Long orderId = objectMapper.readTree(orderResponse).get("id").asLong();

        UpdateOrderStatusRequestDto statusRequest = new UpdateOrderStatusRequestDto("COMPLETED");

        mockMvc.perform(patch("/orders/" + orderId)
                        .with(csrf())
                        .with(user(adminEmail).roles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(statusRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("COMPLETED"));
    }

    @Test
    @DisplayName("Update order status - forbidden for USER role")
    void updateOrderStatus_asUser_shouldBeForbidden() throws Exception {
        String email = "user@example.com";
        createUser(email, "password", "Test", "User", "USER");

        UpdateOrderStatusRequestDto statusRequest = new UpdateOrderStatusRequestDto("COMPLETED");

        mockMvc.perform(patch("/orders/1")
                        .with(csrf())
                        .with(user(email).roles("USER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(statusRequest)))
                .andExpect(status().isForbidden());
    }
}
