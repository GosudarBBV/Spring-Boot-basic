package book.shop.spring.boot.intro.controller;

import book.shop.spring.boot.intro.dto.ShoppingCartResponseDto;
import book.shop.spring.boot.intro.dto.UpdateCartItemRequestDto;
import book.shop.spring.boot.intro.service.ShoppingCartService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
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
    private final ShoppingCartService cartService;
    private final ShoppingCartService shoppingCartService;

    @PutMapping("items/{cartItemId}/quantity")
    @Operation(summary = "Update quantity of a book in the cart")
    @ApiResponse(responseCode = "200", description = "Cart item updated")
    public void updateCartItemQuantity(
            @PathVariable Long cartItemId,
            @RequestBody @Valid UpdateCartItemRequestDto requestDto) {
        cartService.updateCartItemQuantity(cartItemId, requestDto.quantity());
    }

    @GetMapping
    @Operation(summary = "Get user's shopping cart")
    public ShoppingCartResponseDto getShoppingCart() {
        return shoppingCartService.getCurrentUserCart();
    }

    @PutMapping("/items/{cartItemId}")
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "Update cart item quantity",
            description = "Update the quantity of a book in the user's cart")
    public ShoppingCartResponseDto updateCartItem(
            @Parameter(description = "ID of the cart item")
            @PathVariable Long cartItemId,
            @RequestBody @Valid UpdateCartItemRequestDto dto) {
        return shoppingCartService.updateCartItem(cartItemId, dto);
    }

    @DeleteMapping("/items/{cartItemId}")
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "Remove book from cart",
            description = "Deletes a cart item from the shopping cart")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeCartItem(
            @Parameter(description = "ID of the cart item") @PathVariable Long cartItemId) {
        shoppingCartService.removeCartItem(cartItemId);
    }
}
