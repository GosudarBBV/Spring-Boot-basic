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
import book.shop.spring.boot.intro.service.OrderService;
import book.shop.spring.boot.intro.service.UserService;
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
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private OrderService orderService;

    @Autowired
    private UserService userService;

    // допоміжний метод, щоб створити замовлення і отримати його id
    private Long placeOrderAndGetId(OrderRequestDto request) throws Exception {
        String response = mockMvc.perform(post("/orders")
                        .with(csrf())
                        .with(user("user").roles("USER"))
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
    @DisplayName("Place order - success")
    void placeOrder_ValidRequest_ReturnsCreatedOrder() throws Exception {
        OrderRequestDto request = new OrderRequestDto("Some shipping address");

        mockMvc.perform(post("/orders")
                        .with(csrf())
                        .with(user("user").roles("USER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.status").value("NEW"));
    }

    @Test
    @DisplayName("Get order history - success")
    void getOrderHistory_ReturnsPagedOrders() throws Exception {
        // Створюємо замовлення, щоб щось було в історії
        placeOrderAndGetId(new OrderRequestDto("Address for history"));

        mockMvc.perform(get("/orders")
                        .with(user("user").roles("USER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].id").isNumber());
    }

    @Test
    @DisplayName("Get order items - success")
    void getOrderItems_ReturnsList() throws Exception {
        Long orderId = placeOrderAndGetId(new OrderRequestDto("Address for items"));

        mockMvc.perform(get("/orders/{orderId}/items", orderId)
                        .with(user("user").roles("USER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @DisplayName("Get single order item - success")
    void getOrderItem_ReturnsItem() throws Exception {
        Long orderId = placeOrderAndGetId(new OrderRequestDto("Address for single item"));

        // Тут треба створити замовлення з хоча б одним item,
        // або тестувати на реальному itemId якщо у тебе є стартові дані,
        // інакше цей тест потрібно адаптувати під твою логіку

        // Для прикладу, якщо є item з id=1 в цьому order:
        mockMvc.perform(get("/orders/{orderId}/items/{itemId}", orderId, 1L)
                        .with(user("user").roles("USER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    @DisplayName("Update order status - success")
    void updateStatus_ValidRequest_ReturnsUpdatedOrder() throws Exception {
        // Для оновлення статусу потрібен ADMIN, створимо замовлення від імені user
        Long orderId = placeOrderAndGetId(new OrderRequestDto("Address for status update"));

        UpdateOrderStatusRequestDto request = new UpdateOrderStatusRequestDto("COMPLETED");

        mockMvc.perform(patch("/orders/{id}", orderId)
                        .with(csrf())
                        .with(user("admin").roles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(orderId))
                .andExpect(jsonPath("$.status").value("COMPLETED"));
    }
}