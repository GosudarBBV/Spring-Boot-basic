package book.shop.spring.boot.intro.dto;

import jakarta.validation.constraints.NotBlank;

public record OrderRequestDto(
        @NotBlank String shippingAddress
) {
}
