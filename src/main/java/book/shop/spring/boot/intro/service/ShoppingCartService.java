package book.shop.spring.boot.intro.service;

import book.shop.spring.boot.intro.dto.ShoppingCartResponseDto;
import book.shop.spring.boot.intro.dto.UpdateCartItemRequestDto;

public interface ShoppingCartService {
    void updateCartItemQuantity(Long cartItemId, int quantity);

    ShoppingCartResponseDto getCurrentUserCart();

    ShoppingCartResponseDto updateCartItem(Long cartItemId, UpdateCartItemRequestDto dto);

    void removeCartItem(Long cartItemId);
}
