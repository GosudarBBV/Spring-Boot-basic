package book.shop.spring.boot.intro.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Data Transfer Object for Category")
public record CategoryDto(
        @Schema(description = "Category ID",
                example = "1")
        Long id,

        @NotBlank
        @Schema(description = "Name of the category",
                example = "Fiction", required = true)
        String name,

        @Schema(description = "Description of the category",
                example = "Books that contain fictional stories")
        String description
) {}
