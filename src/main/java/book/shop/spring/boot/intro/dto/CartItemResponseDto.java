package book.shop.spring.boot.intro.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Cart item response")
public record CartItemResponseDto(
        @Schema(description = "Cart item ID", example = "1")
        Long id,

        @Schema(description = "Book ID", example = "5")
        Long bookId,

        @Schema(description = "Title of the book", example = "The Great Gatsby")
        String bookTitle,

        @Schema(description = "Quantity of the book in the cart", example = "2")
        int quantity
) {}
