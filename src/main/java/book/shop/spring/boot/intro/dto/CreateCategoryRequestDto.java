package book.shop.spring.boot.intro.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "DTO for creating a new category")
public record CreateCategoryRequestDto(

        @Schema(description = "Name of the category", example = "Fantasy")
        @NotBlank(message = "Category name is required")
        String name,

        @Schema(description = "Description of the category",
                example = "Books that involve magical or supernatural elements")
        String description

) {}
