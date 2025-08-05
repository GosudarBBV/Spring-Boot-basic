package book.shop.spring.boot.intro.service;

import book.shop.spring.boot.intro.dto.OrderItemDto;
import book.shop.spring.boot.intro.dto.OrderResponseDto;
import book.shop.spring.boot.intro.exception.EntityNotFoundException;
import book.shop.spring.boot.intro.exception.OrderProcessingException;
import book.shop.spring.boot.intro.mapper.OrderMapper;
import book.shop.spring.boot.intro.model.CartItem;
import book.shop.spring.boot.intro.model.Order;
import book.shop.spring.boot.intro.model.OrderItem;
import book.shop.spring.boot.intro.model.OrderStatus;
import book.shop.spring.boot.intro.model.ShoppingCart;
import book.shop.spring.boot.intro.repository.OrderItemRepository;
import book.shop.spring.boot.intro.repository.OrderRepository;
import book.shop.spring.boot.intro.repository.ShoppingCartRepository;
import jakarta.transaction.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Transactional
public class OrderServiceImpl implements OrderService {
    private final ShoppingCartRepository shoppingCartRepository;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final OrderMapper orderMapper;

    @Override
    public OrderResponseDto placeOrder(Long userId, String shippingAddress) {
        ShoppingCart cart = getShoppingCart(userId);
        validateCartNotEmpty(cart);

        Order order = prepareOrder(cart, shippingAddress);
        Set<OrderItem> orderItems = mapCartItemsToOrderItems(cart.getCartItems(), order);
        BigDecimal total = calculateTotal(orderItems);

        order.setTotal(total);

        orderRepository.save(order);
        cart.clearCart();
        shoppingCartRepository.save(cart);

        order.setOrderItems(orderItems);
        orderRepository.save(order);

        return orderMapper.toDto(order);
    }

    @Override
    public Page<OrderResponseDto> getOrderHistory(Long userId, Pageable pageable) {
        return orderRepository.findAllByUserId(userId, pageable)
                .map(orderMapper::toDto);
    }

    @Override
    public List<OrderItemDto> getOrderItems(Long orderId, Long userId) {
        orderRepository.findByIdAndUserId(orderId, userId)
                .orElseThrow(()
                        -> new EntityNotFoundException("Order not found or does "
                        + "not belong to user"));

        List<OrderItem> items = orderItemRepository.findAllByOrderId(orderId);
        return items.stream()
                .map(orderMapper::toOrderItemDto)
                .toList();
    }

    @Override
    public OrderItemDto getOrderItem(Long orderId, Long itemId, Long userId) {
        OrderItem item = orderItemRepository
                .findByIdAndOrderIdAndOrderUserId(itemId, orderId, userId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Order item not found: itemId = " + itemId
                                + ", orderId = " + orderId
                                + ", userId = " + userId));
        return orderMapper.toOrderItemDto(item);
    }

    @Override
    public OrderResponseDto updateStatus(Long orderId, OrderStatus status) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException("Order not found: id = " + orderId));
        order.setStatus(status);
        return orderMapper.toDto(order);
    }

    private ShoppingCart getShoppingCart(Long userId) {
        return shoppingCartRepository.findByUserId(userId)
                .orElseThrow(()
                        -> new EntityNotFoundException("Shopping cart not found for userId: "
                        + userId));
    }

    private void validateCartNotEmpty(ShoppingCart cart) {
        if (cart.getCartItems().isEmpty()) {
            throw new OrderProcessingException("Shopping cart is empty for userId = "
                    + cart.getUser().getId());
        }
    }

    private Order prepareOrder(ShoppingCart cart, String shippingAddress) {
        Order order = new Order();
        order.setUser(cart.getUser());
        order.setOrderDate(LocalDateTime.now());
        order.setStatus(OrderStatus.PENDING);
        order.setShippingAddress(shippingAddress);
        return order;
    }

    private Set<OrderItem> mapCartItemsToOrderItems(Set<CartItem> cartItems, Order order) {
        Set<OrderItem> orderItems = new HashSet<>();
        for (CartItem cartItem : cartItems) {
            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setBook(cartItem.getBook());
            orderItem.setQuantity(cartItem.getQuantity());
            BigDecimal price = cartItem.getBook().getPrice()
                    .multiply(BigDecimal.valueOf(cartItem.getQuantity()));
            orderItem.setPrice(price);
            orderItems.add(orderItem);
        }
        return orderItems;
    }

    private BigDecimal calculateTotal(Set<OrderItem> orderItems) {
        return orderItems.stream()
                .map(OrderItem::getPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
