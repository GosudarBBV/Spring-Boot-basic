package book.shop.spring.boot.intro.controller;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
import book.shop.spring.boot.intro.model.User;
import book.shop.spring.boot.intro.repository.OrderRepository;
import book.shop.spring.boot.intro.repository.RoleRepository;
import book.shop.spring.boot.intro.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Collections;
import java.util.HashSet;
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

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

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
        user.setShippingAddress("Test address");
        user.setRoles(Collections.singleton(role));
        return userRepository.save(user);
    }

    private Long createOrderAndGetId(String userEmail, String role, OrderRequestDto request) throws Exception {
        String response = mockMvc.perform(post("/orders")
                        .with(csrf())
                        .with(user(userEmail).roles(role))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andReturn()
                .getResponse()
                .getContentAsString();

        return objectMapper.readTree(response).get("id").asLong();
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
        createOrderAndGetId(user.getEmail(), "USER", new OrderRequestDto("Kyiv"));

        mockMvc.perform(get("/orders")
                        .with(user(user.getEmail()).roles("USER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @DisplayName("Update order status - success")
    void updateOrderStatus_ReturnsOk() throws Exception {
        User admin = createUser("admin@example.com", RoleName.ADMIN);
        User user = createUser("user3@example.com", RoleName.USER);

        Long orderId = createOrderAndGetId(user.getEmail(), "USER", new OrderRequestDto("Kyiv"));
        UpdateOrderStatusRequestDto updateRequest = new UpdateOrderStatusRequestDto(OrderStatus.PENDING.name());

        mockMvc.perform(patch("/orders/{id}", orderId)
                        .with(csrf())
                        .with(user(admin.getEmail()).roles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk());

        Order updatedOrder = orderRepository.findById(orderId).orElseThrow();
        assertTrue(updatedOrder.getStatus() == OrderStatus.PENDING);
    }
}
