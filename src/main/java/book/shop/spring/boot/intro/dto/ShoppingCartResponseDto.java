package book.shop.spring.boot.intro.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "Shopping cart response")
public record ShoppingCartResponseDto(
        @Schema(description = "Shopping cart ID", example = "10")
        Long id,

        @Schema(description = "User ID to whom the cart belongs", example = "3")
        Long userId,

        @Schema(description = "List of items in the shopping cart")
        List<CartItemResponseDto> cartItems
) {}
