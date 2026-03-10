package book.shop.spring.boot.intro.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;

@Schema(description = "Request to update quantity of a cart item")
public record UpdateCartItemRequestDto(
        @Schema(description = "New quantity", example = "3")
        @Min(value = 1, message = "Quantity must be at least 1")
        int quantity
) {}
