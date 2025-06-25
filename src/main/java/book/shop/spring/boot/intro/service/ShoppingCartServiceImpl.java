package book.shop.spring.boot.intro.service;

import book.shop.spring.boot.intro.dto.ShoppingCartResponseDto;
import book.shop.spring.boot.intro.dto.UpdateCartItemRequestDto;
import book.shop.spring.boot.intro.exception.EntityNotFoundException;
import book.shop.spring.boot.intro.mapper.ShoppingCartMapper;
import book.shop.spring.boot.intro.model.CartItem;
import book.shop.spring.boot.intro.model.ShoppingCart;
import book.shop.spring.boot.intro.model.User;
import book.shop.spring.boot.intro.repository.CartItemRepository;
import book.shop.spring.boot.intro.repository.ShoppingCartRepository;
import book.shop.spring.boot.intro.repository.UserRepository;
import jakarta.transaction.Transactional;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@Transactional
@RequiredArgsConstructor
public class ShoppingCartServiceImpl implements ShoppingCartService {
    private final ShoppingCartRepository shoppingCartRepository;
    private final CartItemRepository cartItemRepository;
    private final ShoppingCartMapper shoppingCartMapper;
    private final UserRepository userRepository;

    @Override
    public ShoppingCartResponseDto createCart(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new EntityNotFoundException("User not found with id: " + userId));

        Optional<ShoppingCart> existingCart = shoppingCartRepository.findByUserId(userId);
        if (existingCart.isPresent()) {
            throw new IllegalStateException("Shopping cart already exists for userId: " + userId);
        }

        ShoppingCart shoppingCart = new ShoppingCart();
        shoppingCart.setUser(user);
        shoppingCartRepository.save(shoppingCart);

        return shoppingCartMapper.toDto(shoppingCart);
    }

    @Override
    public ShoppingCartResponseDto getCartByUser(Long userId) {
        ShoppingCart cart = shoppingCartRepository.findByUserId(userId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Shopping cart not found for userId: " + userId));
        return shoppingCartMapper.toDto(cart);
    }

    @Override
    public ShoppingCartResponseDto updateCartItem(Long cartItemId,
                                                  UpdateCartItemRequestDto dto,
                                                  Long userId) {
        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() ->
                        new EntityNotFoundException("Cart item not found: id = " + cartItemId));

        cartItem.setQuantity(dto.quantity());
        cartItemRepository.save(cartItem);
        return shoppingCartMapper.toDto(cartItem.getShoppingCart());
    }

    @Override
    public void removeCartItem(Long cartItemId, Long userId) {
        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() ->
                        new EntityNotFoundException("Cart item not found: id = " + cartItemId));
        cartItemRepository.delete(cartItem);
    }
}
