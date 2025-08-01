package book.shop.spring.boot.intro.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import book.shop.spring.boot.intro.dto.OrderItemDto;
import book.shop.spring.boot.intro.dto.OrderResponseDto;
import book.shop.spring.boot.intro.exception.EntityNotFoundException;
import book.shop.spring.boot.intro.exception.OrderProcessingException;
import book.shop.spring.boot.intro.mapper.OrderMapper;
import book.shop.spring.boot.intro.model.Book;
import book.shop.spring.boot.intro.model.CartItem;
import book.shop.spring.boot.intro.model.Order;
import book.shop.spring.boot.intro.model.OrderItem;
import book.shop.spring.boot.intro.model.OrderStatus;
import book.shop.spring.boot.intro.model.ShoppingCart;
import book.shop.spring.boot.intro.model.User;
import book.shop.spring.boot.intro.repository.OrderItemRepository;
import book.shop.spring.boot.intro.repository.OrderRepository;
import book.shop.spring.boot.intro.repository.ShoppingCartRepository;
import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class OrderServiceImplTest {

    @Mock
    private ShoppingCartRepository cartRepository;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderItemRepository orderItemRepository;

    @Mock
    private OrderMapper orderMapper;

    @InjectMocks
    private OrderServiceImpl orderService;

    @Test
    @DisplayName("Place order successfully")
    void placeOrder_Success() {
        Long userId = 1L;
        String address = "Kyiv, Ukraine";

        Book book = new Book();
        book.setPrice(BigDecimal.valueOf(20));

        CartItem cartItem = new CartItem();
        cartItem.setBook(book);
        cartItem.setQuantity(2);

        ShoppingCart cart = new ShoppingCart();
        User user = new User();
        user.setId(userId);
        cart.setUser(user);

        Set<CartItem> cartItems = new HashSet<>();
        cartItems.add(cartItem);
        cart.setCartItems(cartItems);

        OrderResponseDto expectedDto = new OrderResponseDto(1L, userId, List.of(),
                null, BigDecimal.valueOf(40), OrderStatus.PENDING);

        when(cartRepository.findByUserId(userId)).thenReturn(Optional.of(cart));
        when(orderMapper.toDto(any(Order.class))).thenReturn(expectedDto);

        OrderResponseDto result = orderService.placeOrder(userId, address);

        ArgumentCaptor<Order> orderCaptor = ArgumentCaptor.forClass(Order.class);
        verify(orderRepository).save(orderCaptor.capture());
        verify(cartRepository).save(cart);
        verify(orderMapper).toDto(orderCaptor.getValue());

        Order savedOrder = orderCaptor.getValue();
        assertThat(savedOrder.getShippingAddress()).isEqualTo(address);
        assertThat(savedOrder.getStatus()).isEqualTo(OrderStatus.PENDING);
        assertThat(savedOrder.getUser()).isEqualTo(user);
        assertThat(savedOrder.getTotal()).isEqualByComparingTo(BigDecimal.valueOf(40));
        assertThat(result).isEqualTo(expectedDto);
    }

    @Test
    @DisplayName("Throw when shopping cart is empty")
    void placeOrder_EmptyCart_Throws() {
        Long userId = 1L;

        User user = new User();
        user.setId(userId);

        ShoppingCart cart = new ShoppingCart();
        cart.setUser(user);
        cart.setCartItems(Set.of());

        when(cartRepository.findByUserId(userId)).thenReturn(Optional.of(cart));

        assertThatThrownBy(() -> orderService.placeOrder(userId, "Kyiv"))
                .isInstanceOf(OrderProcessingException.class)
                .hasMessage("Shopping cart is empty for userId = " + userId);

        verify(cartRepository).findByUserId(userId);
    }

    @Test
    @DisplayName("Throw when shopping cart not found")
    void placeOrder_NoCart_Throws() {
        Long userId = 1L;

        when(cartRepository.findByUserId(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.placeOrder(userId, "Kyiv"))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Shopping cart not found for userId: " + userId);

        verify(cartRepository).findByUserId(userId);
    }

    @Test
    @DisplayName("Get order history by user ID")
    void getOrderHistory_Success() {
        Long userId = 1L;
        Order order = new Order();
        Page<Order> ordersPage = new PageImpl<>(List.of(order));
        OrderResponseDto orderDto = new OrderResponseDto(1L, userId, List.of(),
                null, BigDecimal.ZERO, OrderStatus.PENDING);

        when(orderRepository.findAllByUserId(eq(userId), any(Pageable.class))).thenReturn(ordersPage);
        when(orderMapper.toDto(order)).thenReturn(orderDto);

        Page<OrderResponseDto> result = orderService.getOrderHistory(userId, Pageable.unpaged());

        assertThat(result.getContent()).containsExactly(orderDto);
        verify(orderRepository).findAllByUserId(eq(userId), any(Pageable.class));
        verify(orderMapper).toDto(order);
    }

    @Test
    @DisplayName("Get order items by order ID and user ID")
    void getOrderItems_Success() {
        Long userId = 1L;
        Long orderId = 10L;

        Order order = new Order();
        order.setUser(new User());
        order.setId(orderId);

        OrderItem orderItem = new OrderItem();
        OrderItemDto itemDto = new OrderItemDto(2L, 4L, 1);

        when(orderRepository.findByIdAndUserId(orderId, userId)).thenReturn(Optional.of(order));
        when(orderItemRepository.findAllByOrderId(orderId)).thenReturn(List.of(orderItem));
        when(orderMapper.toOrderItemDto(orderItem)).thenReturn(itemDto);

        List<OrderItemDto> result = orderService.getOrderItems(orderId, userId);

        assertThat(result).containsExactly(itemDto);
        verify(orderRepository).findByIdAndUserId(orderId, userId);
        verify(orderItemRepository).findAllByOrderId(orderId);
        verify(orderMapper).toOrderItemDto(orderItem);
    }

    @Test
    @DisplayName("Get order item by IDs")
    void getOrderItem_Success() {
        Long orderId = 1L;
        Long itemId = 2L;
        Long userId = 3L;

        OrderItem orderItem = new OrderItem();
        OrderItemDto expectedDto = new OrderItemDto(itemId, 4L, 1);

        when(orderItemRepository.findByIdAndOrderIdAndOrderUserId(itemId, orderId, userId))
                .thenReturn(Optional.of(orderItem));
        when(orderMapper.toOrderItemDto(orderItem)).thenReturn(expectedDto);

        OrderItemDto result = orderService.getOrderItem(orderId, itemId, userId);

        assertThat(result).isEqualTo(expectedDto);
        verify(orderItemRepository).findByIdAndOrderIdAndOrderUserId(itemId, orderId, userId);
        verify(orderMapper).toOrderItemDto(orderItem);
    }

    @Test
    @DisplayName("Update order status by ID")
    void updateStatus_Success() {
        Long orderId = 1L;
        OrderStatus status = OrderStatus.COMPLETED;

        Order order = new Order();
        order.setId(orderId);

        OrderResponseDto updatedDto = new OrderResponseDto(orderId, 1L, List.of(), null, BigDecimal.ZERO, status);

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(orderMapper.toDto(order)).thenReturn(updatedDto);

        OrderResponseDto result = orderService.updateStatus(orderId, status);

        assertThat(result).isEqualTo(updatedDto);
        assertThat(order.getStatus()).isEqualTo(status);
        verify(orderRepository).findById(orderId);
        verify(orderMapper).toDto(order);
    }
}
