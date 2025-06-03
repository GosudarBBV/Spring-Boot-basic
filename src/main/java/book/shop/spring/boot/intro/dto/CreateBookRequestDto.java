package book.shop.spring.boot.intro.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import lombok.Data;

@Data
public class CreateBookRequestDto {
    @NotBlank(message = "Title is required")
    private String title;

    @NotBlank(message = "Author is required")
    private String author;

    @Pattern(regexp = "\\d{10}|\\d{13}", message = "ISBN must be 10 or 13 digits")
    private String isbn;

    @DecimalMin(value = "0.0", inclusive = false, message = "Price must be positive")
    private BigDecimal price;

    @Size(max = 1000, message = "Description is too long")
    private String description;

    @Size(max = 255, message = "Cover image URL is too long")
    private String coverImage;
}
