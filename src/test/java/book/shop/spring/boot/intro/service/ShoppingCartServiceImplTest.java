package book.shop.spring.boot.intro.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
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
import book.shop.spring.boot.intro.util.TestEntityFactory;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ShoppingCartServiceImplTest {

    @Mock
    private ShoppingCartRepository shoppingCartRepository;

    @Mock
    private CartItemRepository cartItemRepository;

    @Mock
    private ShoppingCartMapper shoppingCartMapper;

    @Mock
    private BookRepository bookRepository;

    @InjectMocks
    private ShoppingCartServiceImpl shoppingCartService;

    @Test
    @DisplayName("Add book to cart when cart and book exist")
    void addBookToCart_WhenCartAndBookExist_ReturnsShoppingCartDto() {
        Long userId = 1L;
        Long bookId = 10L;
        int quantityToAdd = 2;

        ShoppingCart existingCart = TestEntityFactory
                .createCart(TestEntityFactory.createTestUser(), new HashSet<>());
        Book bookToAdd = TestEntityFactory.createBook(bookId, "Some Book", null);

        ShoppingCartResponseDto expectedResponseDto = new ShoppingCartResponseDto(
                existingCart.getId(),
                existingCart.getUser().getId(),
                List.of()
        );

        when(shoppingCartRepository.findByUserId(userId)).thenReturn(Optional.of(existingCart));
        when(bookRepository.findById(bookId)).thenReturn(Optional.of(bookToAdd));
        when(shoppingCartMapper.toDto(existingCart)).thenReturn(expectedResponseDto);

        ShoppingCartResponseDto actualResponseDto = shoppingCartService.addBookToCart(bookId,
                quantityToAdd, userId);

        assertThat(actualResponseDto).isEqualTo(expectedResponseDto);

        verify(shoppingCartRepository).findByUserId(userId);
        verify(bookRepository).findById(bookId);
        verify(cartItemRepository).save(any(CartItem.class));
        verify(shoppingCartMapper).toDto(existingCart);
    }

    @Test
    @DisplayName("Add book to cart throws when cart not found")
    void addBookToCart_WhenCartNotFound_ThrowsEntityNotFoundException() {
        Long userId = 1L;
        Long bookId = 1L;

        when(shoppingCartRepository.findByUserId(userId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> shoppingCartService
                .addBookToCart(bookId, 1, userId))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Shopping cart not found");

        verify(shoppingCartRepository).findByUserId(userId);
    }

    @Test
    @DisplayName("Add book to cart throws when book not found")
    void addBookToCart_WhenBookNotFound_ThrowsEntityNotFoundException() {
        Long userId = 1L;
        Long bookId = 1L;

        ShoppingCart existingCart = TestEntityFactory.createCart(TestEntityFactory.createTestUser(), new HashSet<>());

        when(shoppingCartRepository.findByUserId(userId))
                .thenReturn(Optional.of(existingCart));
        when(bookRepository.findById(bookId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> shoppingCartService
                .addBookToCart(bookId, 1, userId))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Book not found");

        verify(shoppingCartRepository).findByUserId(userId);
        verify(bookRepository).findById(bookId);
    }

    @Test
    @DisplayName("Get cart by user returns ShoppingCartResponseDto")
    void getCartByUser_WhenCartExists_ReturnsShoppingCartDto() {
        Long userId = 1L;
        ShoppingCart existingCart = TestEntityFactory
                .createCart(TestEntityFactory
                        .createTestUser(), new HashSet<>());

        ShoppingCartResponseDto expectedResponseDto = new ShoppingCartResponseDto(
                existingCart.getId(),
                existingCart.getUser().getId(),
                List.of()
        );

        when(shoppingCartRepository.findByUserId(userId)).thenReturn(Optional.of(existingCart));
        when(shoppingCartMapper.toDto(existingCart)).thenReturn(expectedResponseDto);

        ShoppingCartResponseDto actualResponseDto = shoppingCartService.getCartByUser(userId);

        assertThat(actualResponseDto).isEqualTo(expectedResponseDto);

        verify(shoppingCartRepository).findByUserId(userId);
        verify(shoppingCartMapper).toDto(existingCart);
    }

    @Test
    @DisplayName("Update cart item successfully updates quantity and returns ShoppingCartResponseDto")
    void updateCartItem_WhenCartItemExists_UpdatesQuantityAndReturnsDto() {
        Long userId = 1L;
        Long cartItemId = 100L;
        int newQuantity = 5;
        UpdateCartItemRequestDto updateRequest = new UpdateCartItemRequestDto(newQuantity);

        ShoppingCart existingCart = TestEntityFactory
                .createCart(TestEntityFactory
                .createTestUser(), new HashSet<>());

        CartItem existingCartItem = new CartItem();
        existingCartItem.setId(cartItemId);
        existingCartItem.setQuantity(1);

        ShoppingCartResponseDto expectedResponseDto = new ShoppingCartResponseDto(
                existingCart.getId(),
                existingCart.getUser().getId(),
                List.of()
        );

        when(shoppingCartRepository.findByUserId(userId)).thenReturn(Optional.of(existingCart));
        when(cartItemRepository.findByIdAndShoppingCartId(cartItemId,
                existingCart.getId()))
                .thenReturn(Optional.of(existingCartItem));
        when(shoppingCartMapper.toDto(existingCart)).thenReturn(expectedResponseDto);

        ShoppingCartResponseDto actualResponseDto = shoppingCartService
                .updateCartItem(cartItemId, updateRequest, userId);

        assertThat(existingCartItem.getQuantity()).isEqualTo(newQuantity);
        assertThat(actualResponseDto).isEqualTo(expectedResponseDto);

        verify(shoppingCartRepository).findByUserId(userId);
        verify(cartItemRepository).findByIdAndShoppingCartId(cartItemId, existingCart.getId());
        verify(cartItemRepository).save(existingCartItem);
        verify(shoppingCartMapper).toDto(existingCart);
    }

    @Test
    @DisplayName("Remove cart item successfully deletes the cart item")
    void removeCartItem_WhenCartItemExists_DeletesCartItem() {
        Long userId = 1L;
        Long cartItemId = 101L;

        ShoppingCart existingCart = TestEntityFactory
                .createCart(TestEntityFactory.createTestUser(),
                        new HashSet<>());
        existingCart.setId(10L);

        CartItem existingCartItem = new CartItem();
        existingCartItem.setId(cartItemId);

        when(shoppingCartRepository.findByUserId(userId)).thenReturn(Optional.of(existingCart));
        when(cartItemRepository.findByIdAndShoppingCartId(cartItemId, existingCart.getId()))
                .thenReturn(Optional.of(existingCartItem));

        shoppingCartService.removeCartItem(cartItemId, userId);

        verify(shoppingCartRepository).findByUserId(userId);
        verify(cartItemRepository).findByIdAndShoppingCartId(cartItemId, existingCart.getId());
        verify(cartItemRepository).delete(existingCartItem);
    }

    @Test
    @DisplayName("Remove cart item throws exception when cart item not found")
    void removeCartItem_WhenCartItemNotFound_ThrowsEntityNotFoundException() {
        Long userId = 1L;
        Long cartItemId = 1L;

        ShoppingCart existingCart = TestEntityFactory.createCart(TestEntityFactory.createTestUser(), new HashSet<>());
        existingCart.setId(10L);

        when(shoppingCartRepository.findByUserId(userId)).thenReturn(Optional.of(existingCart));
        when(cartItemRepository.findByIdAndShoppingCartId(cartItemId, existingCart.getId()))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> shoppingCartService.removeCartItem(cartItemId, userId))
                .isInstanceOf(EntityNotFoundException.class);

        verify(shoppingCartRepository).findByUserId(userId);
        verify(cartItemRepository).findByIdAndShoppingCartId(cartItemId, existingCart.getId());
    }
}
