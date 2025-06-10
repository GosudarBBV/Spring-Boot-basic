package book.shop.spring.boot.intro.controller;

import book.shop.spring.boot.intro.dto.UserRegistrationRequestDto;
import book.shop.spring.boot.intro.dto.UserResponseDto;
import book.shop.spring.boot.intro.exception.RegistrationException;
import book.shop.spring.boot.intro.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/auth")
public class AuthController {
    private final UserService userService;

    @PostMapping("/registration")
    public UserResponseDto registerUser(@RequestBody UserRegistrationRequestDto requestDto)
                throws RegistrationException {
        return userService.register(requestDto);
    }
}
