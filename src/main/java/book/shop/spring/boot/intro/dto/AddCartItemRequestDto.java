package book.shop.spring.boot.intro.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record AddCartItemRequestDto(
        @NotNull(message = "Book ID is required") Long bookId,
        @Min(value = 1, message = "Quantity must be at least 1") int quantity
) {}
