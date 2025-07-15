package book.shop.spring.boot.intro.controller;

import book.shop.spring.boot.intro.dto.OrderItemDto;
import book.shop.spring.boot.intro.dto.OrderRequestDto;
import book.shop.spring.boot.intro.dto.OrderResponseDto;
import book.shop.spring.boot.intro.service.OrderService;
import book.shop.spring.boot.intro.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
@PreAuthorize("hasRole('USER')")
@Tag(name = "User Orders", description = "Endpoints for regular users to manage their orders")
public class OrderController {
    private final OrderService orderService;
    private final UserService userService;

    @PostMapping
    @Operation(summary = "Place an order from shopping cart")
    public ResponseEntity<OrderResponseDto> placeOrder(
            @RequestBody @Valid OrderRequestDto dto) {
        Long userId = userService.getAuthenticatedUserId();
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(orderService.placeOrder(userId, dto.shippingAddress()));
    }

    @GetMapping
    @Operation(summary = "Get user's order history with pagination and sorting")
    public Page<OrderResponseDto> getOrderHistory(
            @Parameter(hidden = true) Pageable pageable) {
        Long userId = userService.getAuthenticatedUserId();
        return orderService.getOrderHistory(userId, pageable);
    }

    @GetMapping("/{orderId}/items")
    @Operation(summary = "Get all items from a specific order")
    public List<OrderItemDto> getItems(@PathVariable Long orderId) {
        Long userId = userService.getAuthenticatedUserId();
        return orderService.getOrderItems(orderId, userId);
    }

    @GetMapping("/{orderId}/items/{itemId}")
    @Operation(summary = "Get a specific item from a specific order")
    public OrderItemDto getItem(
            @PathVariable Long orderId,
            @PathVariable Long itemId) {
        Long userId = userService.getAuthenticatedUserId();
        return orderService.getOrderItem(orderId, itemId, userId);
    }
}
