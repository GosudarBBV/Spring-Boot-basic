package book.shop.spring.boot.intro.service;

import book.shop.spring.boot.intro.dto.ShoppingCartResponseDto;
import book.shop.spring.boot.intro.dto.UpdateCartItemRequestDto;

public interface ShoppingCartService {
    ShoppingCartResponseDto createCart(Long userId);

    ShoppingCartResponseDto getCartByUser(Long userId);

    ShoppingCartResponseDto updateCartItem(Long cartItemId,
                                                  UpdateCartItemRequestDto dto,
                                                  Long userId);

    void removeCartItem(Long cartItemId, Long userId);
}
