package book.shop.spring.boot.intro.service;

import book.shop.spring.boot.intro.dto.CartItemResponseDto;
import book.shop.spring.boot.intro.dto.ShoppingCartResponseDto;
import book.shop.spring.boot.intro.dto.UpdateCartItemRequestDto;
import book.shop.spring.boot.intro.exception.EntityNotFoundException;
import book.shop.spring.boot.intro.mapper.ShoppingCartMapper;
import book.shop.spring.boot.intro.model.CartItem;
import book.shop.spring.boot.intro.model.ShoppingCart;
import book.shop.spring.boot.intro.model.User;
import book.shop.spring.boot.intro.repository.CartItemRepository;
import book.shop.spring.boot.intro.repository.ShoppingCartRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ShoppingCartServiceImpl implements ShoppingCartService {
    private final ShoppingCartRepository shoppingCartRepository;
    private final CartItemRepository cartItemRepository;
    private final UserService userService;
    private final ShoppingCartMapper shoppingCartMapper;

    @Override
    public void updateCartItemQuantity(Long cartItemId, int quantity) {
        CartItem cartItem = cartItemRepository.findById(cartItemId).orElseThrow(()
                    -> new EntityNotFoundException("Cart item not found with id: "
                    + cartItemId));

        cartItem.setQuantity(quantity);
        cartItemRepository.save(cartItem);
    }

    @Override
    public ShoppingCartResponseDto getCurrentUserCart() {
        User currentUser = userService.getAuthenticatedUser();

        ShoppingCart cart = shoppingCartRepository.findByUser(currentUser)
                .orElseThrow(() -> new EntityNotFoundException("Shopping cart not found"));

        List<CartItemResponseDto> items = cart.getCartItems().stream()
                .map(item -> new CartItemResponseDto(
                        item.getId(),
                        item.getBook().getId(),
                        item.getBook().getTitle(),
                        item.getQuantity()
                ))
                .toList();

        return new ShoppingCartResponseDto(cart.getId(), currentUser.getId(), items);
    }

    @Override
    public ShoppingCartResponseDto updateCartItem(Long cartItemId, UpdateCartItemRequestDto dto) {
        CartItem item = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new EntityNotFoundException("CartItem not found"));

        item.setQuantity(dto.quantity());
        cartItemRepository.save(item);

        return shoppingCartMapper.toDto(item.getShoppingCart());
    }

    @Override
    public void removeCartItem(Long cartItemId) {
        if (!cartItemRepository.existsById(cartItemId)) {
            throw new EntityNotFoundException("CartItem not found");
        }
        cartItemRepository.deleteById(cartItemId);
    }
}
