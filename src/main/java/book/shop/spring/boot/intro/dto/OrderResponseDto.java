package book.shop.spring.boot.intro.dto;

import book.shop.spring.boot.intro.model.OrderStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record OrderResponseDto(
        Long id,
        Long userId,
        List<OrderItemDto> orderItems,
        LocalDateTime orderDate,
        BigDecimal total,
        OrderStatus status
) {
}
