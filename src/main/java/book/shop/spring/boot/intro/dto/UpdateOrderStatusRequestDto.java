package book.shop.spring.boot.intro.dto;

import jakarta.validation.constraints.NotBlank;

public record UpdateOrderStatusRequestDto(
        @NotBlank String status
) {
}
