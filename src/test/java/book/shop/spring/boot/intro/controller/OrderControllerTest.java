package book.shop.spring.boot.intro.controller;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import book.shop.spring.boot.intro.config.TestSecurityConfig;
import book.shop.spring.boot.intro.dto.OrderRequestDto;
import book.shop.spring.boot.intro.dto.UpdateOrderStatusRequestDto;
import book.shop.spring.boot.intro.model.Book;
import book.shop.spring.boot.intro.model.CartItem;
import book.shop.spring.boot.intro.model.Order;
import book.shop.spring.boot.intro.model.OrderStatus;
import book.shop.spring.boot.intro.model.Role;
import book.shop.spring.boot.intro.model.RoleName;
import book.shop.spring.boot.intro.model.ShoppingCart;
import book.shop.spring.boot.intro.model.User;
import book.shop.spring.boot.intro.repository.BookRepository;
import book.shop.spring.boot.intro.repository.CartItemRepository;
import book.shop.spring.boot.intro.repository.OrderRepository;
import book.shop.spring.boot.intro.repository.RoleRepository;
import book.shop.spring.boot.intro.repository.ShoppingCartRepository;
import book.shop.spring.boot.intro.repository.UserRepository;
import book.shop.spring.boot.intro.util.TestEntityFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.Set;
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

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private UserRepository userRepository;
    @Autowired private RoleRepository roleRepository;
    @Autowired private BookRepository bookRepository;
    @Autowired private ShoppingCartRepository shoppingCartRepository;
    @Autowired private CartItemRepository cartItemRepository;
    @Autowired private OrderRepository orderRepository;

    private User prepareUser() {
        Role role = new Role();
        role.setName(RoleName.USER);
        roleRepository.save(role);

        User user = TestEntityFactory.createTestUser("user@example.com");
        user.setRoles(Set.of(role));
        return userRepository.save(user);
    }

    private Book prepareBook() {
        return bookRepository.save(
                TestEntityFactory.createBook(null, "Book", new BigDecimal("29.99")));
    }

    @Test
    @DisplayName("Create order - success")
    void placeOrder_ValidRequest_ReturnsCreatedOrder() throws Exception {
        User user = prepareUser();
        Book book = prepareBook();

        CartItem cartItem = new CartItem();
        cartItem.setBook(book);
        cartItem.setQuantity(1);

        ShoppingCart cart = new ShoppingCart();
        cart.setUser(user);
        cart.setCartItems(Set.of(cartItem));
        cart = shoppingCartRepository.save(cart);

        cartItem.setShoppingCart(cart);
        cartItemRepository.save(cartItem);

        OrderRequestDto requestDto = new OrderRequestDto("Kyiv");

        mockMvc.perform(post("/orders")
                        .with(csrf())
                        .with(user(user.getEmail()).roles("USER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.shippingAddress").value("Kyiv"))
                .andExpect(jsonPath("$.userId").value(user.getId()));
    }

    @Test
    @DisplayName("Update order status - success")
    void updateStatus_ShouldUpdateStatus() throws Exception {
        Role adminRole = new Role();
        adminRole.setName(RoleName.ADMIN);
        roleRepository.save(adminRole);

        User admin = TestEntityFactory.createTestUser("admin@example.com");
        admin.setRoles(Set.of(adminRole));
        admin = userRepository.save(admin);

        Order order = new Order();
        order.setUser(admin);
        order.setStatus(OrderStatus.PENDING);
        order.setOrderDate(java.time.LocalDateTime.now());
        order.setTotal(new BigDecimal("49.99"));
        order.setOrderItems(Collections.emptySet());
        order = orderRepository.save(order);

        UpdateOrderStatusRequestDto request = new UpdateOrderStatusRequestDto("COMPLETED");

        mockMvc.perform(patch("/orders/{id}", order.getId())
                        .with(csrf())
                        .with(user(admin.getEmail()).roles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("COMPLETED"));
    }
}
