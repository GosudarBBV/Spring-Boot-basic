package book.shop.spring.boot.intro.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record UserLoginRequestDto(@Email
                                  @NotBlank
                                  String email,
                                  @NotBlank
                                  String password) {

}
