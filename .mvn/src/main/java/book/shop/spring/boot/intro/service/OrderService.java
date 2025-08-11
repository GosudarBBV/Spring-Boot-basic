package book.shop.spring.boot.intro.service;

import book.shop.spring.boot.intro.dto.OrderItemDto;
import book.shop.spring.boot.intro.dto.OrderResponseDto;
import book.shop.spring.boot.intro.model.OrderStatus;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface OrderService {
    OrderResponseDto placeOrder(Long userId, String shippingAddress);

    Page<OrderResponseDto> getOrderHistory(Long userId, Pageable pageable);

    OrderResponseDto updateStatus(Long orderId, OrderStatus status);

    List<OrderItemDto> getOrderItems(Long orderId, Long userId);

    OrderItemDto getOrderItem(Long orderId, Long itemId, Long userId);
}
