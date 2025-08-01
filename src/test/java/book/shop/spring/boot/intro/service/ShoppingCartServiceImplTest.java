package book.shop.spring.boot.intro.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import book.shop.spring.boot.intro.dto.ShoppingCartResponseDto;
import book.shop.spring.boot.intro.dto.UpdateCartItemRequestDto;
import book.shop.spring.boot.intro.exception.EntityNotFoundException;
import book.shop.spring.boot.intro.mapper.ShoppingCartMapper;
import book.shop.spring.boot.intro.model.Book;
import book.shop.spring.boot.intro.model.CartItem;
import book.shop.spring.boot.intro.model.ShoppingCart;
import book.shop.spring.boot.intro.repository.BookRepository;
import book.shop.spring.boot.intro.repository.CartItemRepository;
import book.shop.spring.boot.intro.repository.ShoppingCartRepository;
import java.util.HashSet;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ShoppingCartServiceImplTest {

    private ShoppingCartRepository shoppingCartRepository;
    private CartItemRepository cartItemRepository;
    private ShoppingCartMapper shoppingCartMapper;
    private BookRepository bookRepository;

    private ShoppingCartServiceImpl shoppingCartService;

    @BeforeEach
    void setUp() {
        shoppingCartRepository = mock(ShoppingCartRepository.class);
        cartItemRepository = mock(CartItemRepository.class);
        shoppingCartMapper = mock(ShoppingCartMapper.class);
        bookRepository = mock(BookRepository.class);
        shoppingCartService = new ShoppingCartServiceImpl(
                shoppingCartRepository,
                cartItemRepository,
                shoppingCartMapper,
                bookRepository
        );
    }

    @Test
    @DisplayName("Add book to cart when cart and book exist")
    void addBookToCart_ValidData_Success() {
        Long userId = 1L;
        Long bookId = 10L;
        int quantity = 2;

        ShoppingCart cart = new ShoppingCart();
        cart.setId(1L);
        cart.setCartItems(new HashSet<>());

        Book book = new Book();
        book.setId(bookId);

        ShoppingCartResponseDto expectedDto = mock(ShoppingCartResponseDto.class);

        when(shoppingCartRepository.findByUserId(userId)).thenReturn(Optional.of(cart));
        when(bookRepository.findById(bookId)).thenReturn(Optional.of(book));
        when(shoppingCartMapper.toDto(cart)).thenReturn(expectedDto);

        ShoppingCartResponseDto actual = shoppingCartService.addBookToCart(bookId, quantity, userId);

        assertEquals(expectedDto, actual);
        verify(cartItemRepository).save(any(CartItem.class));
    }

    @Test
    @DisplayName("Throw if cart not found")
    void addBookToCart_CartNotFound_ThrowsException() {
        when(shoppingCartRepository.findByUserId(1L)).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                () -> shoppingCartService.addBookToCart(1L, 1, 1L));
        assertTrue(exception.getMessage().contains("Shopping cart not found"));
    }

    @Test
    @DisplayName("Throw if book not found")
    void addBookToCart_BookNotFound_ThrowsException() {
        ShoppingCart cart = new ShoppingCart();
        cart.setCartItems(new HashSet<>());

        when(shoppingCartRepository.findByUserId(1L)).thenReturn(Optional.of(cart));
        when(bookRepository.findById(1L)).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                () -> shoppingCartService.addBookToCart(1L, 1, 1L));
        assertTrue(exception.getMessage().contains("Book not found"));
    }

    @Test
    @DisplayName("Get cart by user - success")
    void getCartByUser_Success() {
        ShoppingCart cart = new ShoppingCart();
        ShoppingCartResponseDto dto = mock(ShoppingCartResponseDto.class);

        when(shoppingCartRepository.findByUserId(1L)).thenReturn(Optional.of(cart));
        when(shoppingCartMapper.toDto(cart)).thenReturn(dto);

        ShoppingCartResponseDto actual = shoppingCartService.getCartByUser(1L);

        assertEquals(dto, actual);
    }

    @Test
    @DisplayName("Update cart item - success")
    void updateCartItem_Success() {
        Long userId = 1L;
        Long cartItemId = 100L;
        UpdateCartItemRequestDto dto = new UpdateCartItemRequestDto(5);

        ShoppingCart cart = new ShoppingCart();
        cart.setId(1L);
        cart.setCartItems(new HashSet<>());

        CartItem cartItem = new CartItem();
        cartItem.setId(cartItemId);
        cartItem.setQuantity(1);

        ShoppingCartResponseDto expectedDto = mock(ShoppingCartResponseDto.class);

        when(shoppingCartRepository.findByUserId(userId)).thenReturn(Optional.of(cart));
        when(cartItemRepository.findByIdAndShoppingCartId(cartItemId, cart.getId()))
                .thenReturn(Optional.of(cartItem));
        when(shoppingCartMapper.toDto(cart)).thenReturn(expectedDto);

        ShoppingCartResponseDto actual = shoppingCartService.updateCartItem(cartItemId, dto, userId);

        assertEquals(5, cartItem.getQuantity());
        assertEquals(expectedDto, actual);
        verify(cartItemRepository).save(cartItem);
    }

    @Test
    @DisplayName("Remove cart item - success")
    void removeCartItem_Success() {
        Long userId = 1L;
        Long cartItemId = 101L;

        ShoppingCart cart = new ShoppingCart();
        cart.setId(10L);

        CartItem cartItem = new CartItem();
        cartItem.setId(cartItemId);

        when(shoppingCartRepository.findByUserId(userId)).thenReturn(Optional.of(cart));
        when(cartItemRepository.findByIdAndShoppingCartId(cartItemId, cart.getId()))
                .thenReturn(Optional.of(cartItem));

        shoppingCartService.removeCartItem(cartItemId, userId);

        verify(cartItemRepository).delete(cartItem);
    }

    @Test
    @DisplayName("Remove cart item - not found")
    void removeCartItem_NotFound_ThrowsException() {
        ShoppingCart cart = new ShoppingCart();
        cart.setId(10L);

        when(shoppingCartRepository.findByUserId(1L)).thenReturn(Optional.of(cart));
        when(cartItemRepository.findByIdAndShoppingCartId(1L, 10L))
                .thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class,
                () -> shoppingCartService.removeCartItem(1L, 1L));
    }
}
