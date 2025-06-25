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

    public ShoppingCartResponseDto createCart(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(()
                        -> new EntityNotFoundException("User not found with email: " + email));

        if (shoppingCartRepository.existsByUser(user)) {
            throw new IllegalStateException("Shopping cart already exists for this user");
        }

        ShoppingCart shoppingCart = new ShoppingCart();
        shoppingCart.setUser(user);
        shoppingCart = shoppingCartRepository.save(shoppingCart);

        return shoppingCartMapper.toDto(shoppingCart);
    }

    @Override
    public ShoppingCartResponseDto getCartByUser(User user) {
        ShoppingCart cart = shoppingCartRepository.findByUser(user)
                .orElseThrow(()
                        -> new EntityNotFoundException("Shopping cart not found for user: "
                        + user.getEmail()));
        return shoppingCartMapper.toDto(cart);
    }

    @Override
    public ShoppingCartResponseDto updateCartItem(Long cartItemId,
                                                  UpdateCartItemRequestDto dto,
                                                  User user) {
        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(()
                        -> new EntityNotFoundException("Cart item not found: id = "
                        + cartItemId));

        if (!cartItem.getShoppingCart().getUser().getId().equals(user.getId())) {
            throw new SecurityException("Access denied: this item "
                    + "does not belong to the authenticated user.");
        }

        cartItem.setQuantity(dto.quantity());
        cartItemRepository.save(cartItem);
        return shoppingCartMapper.toDto(cartItem.getShoppingCart());
    }

    @Override
    public void removeCartItem(Long cartItemId, User user) {
        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(()
                        -> new EntityNotFoundException("Cart item not found: id = "
                        + cartItemId));

        if (!cartItem.getShoppingCart().getUser().getId().equals(user.getId())) {
            throw new SecurityException("Access denied: this item does "
                    + "not belong to the authenticated user.");
        }

        cartItemRepository.delete(cartItem);
    }
}
