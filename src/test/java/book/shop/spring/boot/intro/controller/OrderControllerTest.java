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
import book.shop.spring.boot.intro.dto.AddCartItemRequestDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestSecurityConfig.class)
@Sql(scripts = "classpath:database/schemas/create-order-controller-schema.sql",
        executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = {
        "classpath:database/order-controllers/insert-order-controller-test-data.sql",
}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = {
        "classpath:database/order-controllers/remove-order-controller-test-data.sql",
}, executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
public class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

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
        addBookToCart(email, 1L);

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
        addBookToCart(email, 1L);

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
        addBookToCart(email, 1L);

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
        addBookToCart(email, 1L);

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
        String userEmail = "user@example.com";
        addBookToCart(userEmail, 1L);

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
                        .with(user("admin@example.com").roles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(statusRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("COMPLETED"));
    }

    @Test
    @DisplayName("Update order status - forbidden for USER role")
    void updateOrderStatus_asUser_shouldBeForbidden() throws Exception {
        UpdateOrderStatusRequestDto statusRequest = new UpdateOrderStatusRequestDto("COMPLETED");

        mockMvc.perform(patch("/orders/1")
                        .with(csrf())
                        .with(user("user@example.com").roles("USER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(statusRequest)))
                .andExpect(status().isForbidden());
    }
}
