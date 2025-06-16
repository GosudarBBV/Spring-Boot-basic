package book.shop.spring.boot.intro.controller;

import book.shop.spring.boot.intro.dto.UserLoginRequestDto;
import book.shop.spring.boot.intro.dto.UserLoginResponseDto;
import book.shop.spring.boot.intro.dto.UserRegistrationRequestDto;
import book.shop.spring.boot.intro.dto.UserResponseDto;
import book.shop.spring.boot.intro.exception.RegistrationException;
import book.shop.spring.boot.intro.security.AuthenticationService;
import book.shop.spring.boot.intro.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/auth")
@Tag(name = "Authentication", description = "Endpoints for user registration and login")
public class AuthController {
    private final UserService userService;
    private final AuthenticationService authService;

    @PostMapping("/registration")
    @Operation(summary = "Register a new user",
            description = "Creates a new user in the system")
    public UserResponseDto registerUser(@RequestBody @Valid UserRegistrationRequestDto requestDto)
            throws RegistrationException {
        return userService.register(requestDto);
    }

    @Operation(
            summary = "Login user",
            description = "Authenticate user and return JWT token"
    )
    @PostMapping("/login")
    public UserLoginResponseDto login(@RequestBody @Valid UserLoginRequestDto requestDto) {
        return authService.authenticate(requestDto);
    }
}
