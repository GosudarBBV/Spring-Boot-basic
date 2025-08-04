package book.shop.spring.boot.intro.controller;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import book.shop.spring.boot.intro.config.TestSecurityConfig;
import book.shop.spring.boot.intro.dto.OrderItemDto;
import book.shop.spring.boot.intro.dto.OrderRequestDto;
import book.shop.spring.boot.intro.dto.OrderResponseDto;
import book.shop.spring.boot.intro.dto.UpdateOrderStatusRequestDto;
import book.shop.spring.boot.intro.model.*;
import book.shop.spring.boot.intro.repository.*;
import book.shop.spring.boot.intro.util.TestEntityFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
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
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

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
    private BookRepository bookRepository;

    @Autowired
    private ShoppingCartRepository shoppingCartRepository;

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    private User testUser;
    private Book testBook;

    @BeforeEach
    void setUp() {
        orderItemRepository.deleteAll();
        orderRepository.deleteAll();
        cartItemRepository.deleteAll();
        shoppingCartRepository.deleteAll();
        bookRepository.deleteAll();
        userRepository.deleteAll();
        roleRepository.deleteAll();

        Role userRole = new Role();
        userRole.setName(RoleName.USER);
        roleRepository.save(userRole);

        Role adminRole = new Role();
        adminRole.setName(RoleName.ADMIN);
        roleRepository.save(adminRole);

        testUser = TestEntityFactory.createTestUser("user@example.com");
        testUser.setRoles(Set.of(userRole));
        testUser = userRepository.save(testUser);

        testBook = TestEntityFactory.createBook(null, "Test Book", new BigDecimal("99.99"));
        testBook = bookRepository.save(testBook);
    }

    @Test
    @DisplayName("Create order - success")
    void placeOrder_ValidRequest_ReturnsCreatedOrder() throws Exception {
        OrderRequestDto requestDto = new OrderRequestDto("Kyiv");

        mockMvc.perform(post("/orders")
                        .with(csrf())
                        .with(user(testUser.getEmail()).roles("USER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("Get order history - success")
    void getOrderHistory_ShouldReturnPage() throws Exception {
        mockMvc.perform(get("/orders")
                        .with(user(testUser.getEmail()).roles("USER")))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Get order items - success")
    void getItems_ShouldReturnItems() throws Exception {
        mockMvc.perform(get("/orders/1/items")
                        .with(user(testUser.getEmail()).roles("USER")))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Get specific order item - success")
    void getItem_ShouldReturnSpecificItem() throws Exception {
        mockMvc.perform(get("/orders/1/items/2")
                        .with(user(testUser.getEmail()).roles("USER")))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Update order status - success")
    void updateStatus_ShouldUpdateStatus() throws Exception {
        UpdateOrderStatusRequestDto request = new UpdateOrderStatusRequestDto("PENDING");

        mockMvc.perform(patch("/orders/1")
                        .with(csrf())
                        .with(user("admin@example.com").roles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }
}
