package book.shop.spring.boot.intro.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "DTO for creating a new user")
public record CreateUserRequestDto(

    @Schema(description = "User's email address", example = "user@example.com", required = true)
    @NotBlank(message = "Email cannot be blank")
    @Email(message = "Email should be valid")
    String email,

    @Schema(description = "User's password", example = "strongPassword123", required = true)
    @NotBlank(message = "Password cannot be blank")
    @Size(min = 6, max = 100, message = "Password must be between 6 and 100 characters")
    String password,

    @Schema(description = "User's first name", example = "John", required = true)
    @NotBlank(message = "First name cannot be blank")
    String firstName,

    @Schema(description = "User's last name", example = "Doe", required = true)
    @NotBlank(message = "Last name cannot be blank")
    String lastName,

    @Schema(description = "User's role", example = "USER", required = true)
    @NotBlank(message = "Role cannot be blank")
    String role

) {}