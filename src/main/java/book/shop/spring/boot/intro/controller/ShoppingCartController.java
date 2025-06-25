package book.shop.spring.boot.intro.controller;

import book.shop.spring.boot.intro.dto.ShoppingCartResponseDto;
import book.shop.spring.boot.intro.dto.UpdateCartItemRequestDto;
import book.shop.spring.boot.intro.service.ShoppingCartService;
import book.shop.spring.boot.intro.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/cart")
@RequiredArgsConstructor
@PreAuthorize("hasRole('USER')")
public class ShoppingCartController {
    private final ShoppingCartService shoppingCartService;
    private final UserService userService;

    @PostMapping
    @Operation(summary = "Create shopping cart for the user")
    @ApiResponse(responseCode = "201", description = "Shopping cart created")
    public ResponseEntity<ShoppingCartResponseDto> createShoppingCart() {
        Long userId = userService.getAuthenticatedUserId();
        ShoppingCartResponseDto createdCart = shoppingCartService.createCart(userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdCart);
    }

    @GetMapping
    @Operation(summary = "Get current user's shopping cart")
    public ShoppingCartResponseDto getShoppingCart() {
        Long userId = userService.getAuthenticatedUserId();
        return shoppingCartService.getCartByUser(userId);
    }

    @PutMapping("/items/{cartItemId}")
    @Operation(summary = "Update cart item quantity")
    public ShoppingCartResponseDto updateCartItem(
            @Parameter(description = "Cart item ID") @PathVariable Long cartItemId,
            @RequestBody @Valid UpdateCartItemRequestDto dto) {
        Long userId = userService.getAuthenticatedUserId();
        return shoppingCartService.updateCartItem(cartItemId, dto, userId);
    }

    @DeleteMapping("/items/{cartItemId}")
    @Operation(summary = "Remove item from cart")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeCartItem(
            @Parameter(description = "Cart item ID") @PathVariable Long cartItemId) {
        Long userId = userService.getAuthenticatedUserId();
        shoppingCartService.removeCartItem(cartItemId, userId);
    }
}
