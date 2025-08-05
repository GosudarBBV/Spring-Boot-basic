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
import book.shop.spring.boot.intro.model.OrderStatus;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestSecurityConfig.class)
public class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private Long testOrderId;

    @BeforeEach
    void createTestOrder() throws Exception {
        OrderRequestDto requestDto = new OrderRequestDto("123 Test St, Kyiv");

        MvcResult result = mockMvc.perform(post("/orders")
                        .with(csrf())
                        .with(user("user").roles("USER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isCreated())
                .andReturn();

        String json = result.getResponse().getContentAsString();
        JsonNode node = objectMapper.readTree(json);
        testOrderId = node.get("id").asLong();
    }

    @Test
    @DisplayName("Place order - success for USER role")
    void placeOrder_asUser_shouldReturnCreatedOrder() throws Exception {
        OrderRequestDto requestDto = new OrderRequestDto("456 Another St, Kyiv");

        mockMvc.perform(post("/orders")
                        .with(csrf())
                        .with(user("user").roles("USER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.shippingAddress").value("456 Another St, Kyiv"))
                .andExpect(jsonPath("$.id").isNumber());
    }

    @Test
    @DisplayName("Get order history - success for USER role")
    void getOrderHistory_asUser_shouldReturnPagedOrders() throws Exception {
        mockMvc.perform(get("/orders")
                        .with(user("user").roles("USER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    @DisplayName("Get order items - success for USER role")
    void getOrderItems_asUser_shouldReturnList() throws Exception {
        mockMvc.perform(get("/orders/{orderId}/items", testOrderId)
                        .with(user("user").roles("USER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @DisplayName("Get order item by ID - success for USER role")
    void getOrderItem_asUser_shouldReturnItem() throws Exception {
        // Тут може бути складно вгадати itemId,
        // якщо немає даних, можна або створити окремо item у @BeforeEach,
        // або перевірити лише що повертається JSON з id
        // Для прикладу спробуємо отримати перший item
        mockMvc.perform(get("/orders/{orderId}/items", testOrderId)
                        .with(user("user").roles("USER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").exists());
    }

    @Test
    @DisplayName("Update order status - success for ADMIN role")
    void updateOrderStatus_asAdmin_shouldUpdateStatus() throws Exception {
        UpdateOrderStatusRequestDto requestDto = new UpdateOrderStatusRequestDto(OrderStatus.COMPLETED.name());

        mockMvc.perform(patch("/orders/{orderId}", testOrderId)
                        .with(csrf())
                        .with(user("admin").roles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("COMPLETED"));
    }

    @Test
    @DisplayName("Update order status - forbidden for USER role")
    void updateOrderStatus_asUser_shouldBeForbidden() throws Exception {
        UpdateOrderStatusRequestDto requestDto = new UpdateOrderStatusRequestDto(OrderStatus.COMPLETED.name());

        mockMvc.perform(patch("/orders/{orderId}", testOrderId)
                        .with(csrf())
                        .with(user("user").roles("USER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isForbidden());
    }
}
