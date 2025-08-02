package book.shop.spring.boot.intro.controller;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.any;

import book.shop.spring.boot.intro.config.TestSecurityConfig;
import book.shop.spring.boot.intro.dto.OrderItemDto;
import book.shop.spring.boot.intro.dto.OrderRequestDto;
import book.shop.spring.boot.intro.dto.OrderResponseDto;
import book.shop.spring.boot.intro.dto.UpdateOrderStatusRequestDto;
import book.shop.spring.boot.intro.model.OrderStatus;
import book.shop.spring.boot.intro.service.OrderService;
import book.shop.spring.boot.intro.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;
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

    @MockBean
    private OrderService orderService;

    @MockBean
    private UserService userService;

    @Test
    @DisplayName("Create order - success")
    void placeOrder_ValidRequest_ReturnsCreatedOrder() throws Exception {
        OrderRequestDto requestDto = new OrderRequestDto("Kyiv");
        OrderResponseDto responseDto = new OrderResponseDto(
                1L,
                1L,
                Collections.emptyList(),
                LocalDateTime.now(),
                new BigDecimal("99.99"),
                OrderStatus.PENDING
        );

        when(userService.getAuthenticatedUserId()).thenReturn(1L);
        when(orderService.placeOrder(1L, requestDto.shippingAddress())).thenReturn(responseDto);

        mockMvc.perform(post("/orders")
                        .with(csrf())
                        .with(user("user").roles("USER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isCreated())
                .andExpect(content().json(objectMapper.writeValueAsString(responseDto)));
    }

    @Test
    @DisplayName("Get order history - success")
    void getOrderHistory_ShouldReturnPage() throws Exception {
        OrderResponseDto order = new OrderResponseDto(
                1L,
                1L,
                Collections.emptyList(),
                LocalDateTime.now(),
                new BigDecimal("29.99"),
                OrderStatus.PENDING
        );

        when(userService.getAuthenticatedUserId()).thenReturn(1L);
        when(orderService.getOrderHistory(any(Long.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(order), PageRequest.of(0, 10), 1));

        mockMvc.perform(get("/orders")
                        .with(user("user").roles("USER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.content[0].userId").value(1))
                .andExpect(jsonPath("$.content[0].total").value(29.99));
    }

    @Test
    @DisplayName("Get order items - success")
    void getItems_ShouldReturnItems() throws Exception {
        OrderItemDto item = new OrderItemDto(1L, 5L, 2);

        when(userService.getAuthenticatedUserId()).thenReturn(1L);
        when(orderService.getOrderItems(1L, 1L)).thenReturn(List.of(item));

        mockMvc.perform(get("/orders/1/items")
                        .with(user("user").roles("USER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].bookId").value(5))
                .andExpect(jsonPath("$[0].quantity").value(2));
    }

    @Test
    @DisplayName("Get specific order item - success")
    void getItem_ShouldReturnSpecificItem() throws Exception {
        OrderItemDto item = new OrderItemDto(2L, 7L, 1);

        when(userService.getAuthenticatedUserId()).thenReturn(1L);
        when(orderService.getOrderItem(1L, 2L, 1L)).thenReturn(item);

        mockMvc.perform(get("/orders/1/items/2")
                        .with(user("user").roles("USER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(2))
                .andExpect(jsonPath("$.bookId").value(7))
                .andExpect(jsonPath("$.quantity").value(1));
    }

    @Test
    @DisplayName("Update order status - success")
    void updateStatus_ShouldUpdateStatus() throws Exception {
        UpdateOrderStatusRequestDto request = new UpdateOrderStatusRequestDto("PENDING");
        OrderResponseDto response = new OrderResponseDto(
                1L,
                1L,
                Collections.emptyList(),
                LocalDateTime.now(),
                new BigDecimal("49.99"),
                OrderStatus.PENDING
        );

        when(orderService.updateStatus(1L, OrderStatus.PENDING)).thenReturn(response);

        mockMvc.perform(patch("/orders/1")
                        .with(csrf())
                        .with(user("admin").roles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PENDING"));
    }
}
