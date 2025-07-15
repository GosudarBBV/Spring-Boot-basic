package book.shop.spring.boot.intro.controller;

import book.shop.spring.boot.intro.dto.OrderResponseDto;
import book.shop.spring.boot.intro.model.OrderStatus;
import book.shop.spring.boot.intro.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin Orders", description = "Endpoints for admin to manage order statuses")
public class AdminOrderController {
    private final OrderService orderService;

    @PatchMapping("/{id}")
    @Operation(summary = "Update status of an order")
    public OrderResponseDto updateStatus(
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {
        OrderStatus status = OrderStatus.valueOf(body.get("status"));
        return orderService.updateStatus(id, status);
    }
}
