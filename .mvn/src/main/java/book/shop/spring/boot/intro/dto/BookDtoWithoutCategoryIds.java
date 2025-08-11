package book.shop.spring.boot.intro.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;

@Schema(description = "Book DTO without category IDs"
        + " (used for category-related responses)")
public record BookDtoWithoutCategoryIds(
        @Schema(description = "Book ID", example = "1")
        Long id,

        @Schema(description = "Title of the book", example = "The Great Gatsby")
        String title,

        @Schema(description = "Author of the book", example = "F. Scott Fitzgerald")
        String author,

        @Schema(description = "Price of the book", example = "19.99")
        BigDecimal price,

        @Schema(description = "Description of the book",
                example = "A novel about the American dream.")
        String description
) {}
