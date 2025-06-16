package book.shop.spring.boot.intro.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record UserLoginRequestDto(@Email
                                  @NotBlank
                                  @Schema(example = "john.doe@example.com",
                                          description = "User email")
                                  String email,
                                  @NotBlank
                                  @Schema(example = "securePassword123",
                                          description = "User password")
                                  String password) {

}
