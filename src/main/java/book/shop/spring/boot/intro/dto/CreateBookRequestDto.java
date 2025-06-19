package book.shop.spring.boot.intro.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.util.List;

@Schema(description = "DTO for creating a new book")
public record CreateBookRequestDto(

        @Schema(description = "Title of the book", example = "The Hobbit")
        @NotBlank(message = "Title is required")
        String title,

        @Schema(description = "Author of the book", example = "J.R.R. Tolkien")
        @NotBlank(message = "Author is required")
        String author,

        @Schema(description = "ISBN of the book (10 or 13 digits)", example = "9783161484100")
        @Pattern(regexp = "\\d{10}|\\d{13}", message = "ISBN must be 10 or 13 digits")
        String isbn,

        @Schema(description = "Price of the book", example = "19.99")
        @DecimalMin(value = "0.0", inclusive = false, message = "Price must be positive")
        BigDecimal price,

        @Schema(description = "Description of the book",
                example = "A fantasy novel about hobbits and dragons")
        @Size(max = 1000, message = "Description is too long")
        String description,

        @Schema(description = "Cover image URL", example = "http://example.com/cover.jpg")
        @Size(max = 255, message = "Cover image URL is too long")
        String coverImage,

        @Schema(description = "List of category IDs the book belongs to", example = "[1, 2, 3]")
        List<Long> categoryIds

) {}
