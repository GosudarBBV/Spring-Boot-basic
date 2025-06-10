package book.shop.spring.boot.intro.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record UserResponseDto(
        @Schema(description = "User's ID") Long id,
        @Schema(description = "User's email") String email,
        @Schema(description = "User's first name") String firstName,
        @Schema(description = "User's last name") String lastName,
        @Schema(description = "User's shipping address") String shippingAddress
) {}
