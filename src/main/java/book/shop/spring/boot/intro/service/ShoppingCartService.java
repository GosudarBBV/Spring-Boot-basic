package book.shop.spring.boot.intro.service;

import book.shop.spring.boot.intro.dto.ShoppingCartResponseDto;
import book.shop.spring.boot.intro.dto.UpdateCartItemRequestDto;
import book.shop.spring.boot.intro.model.User;

public interface ShoppingCartService {
    ShoppingCartResponseDto createCart(String email);

    public ShoppingCartResponseDto getCartByUser(User user);

    public ShoppingCartResponseDto updateCartItem(Long cartItemId,
                                                  UpdateCartItemRequestDto dto,
                                                  User user);

    void removeCartItem(Long cartItemId, User user);
}
