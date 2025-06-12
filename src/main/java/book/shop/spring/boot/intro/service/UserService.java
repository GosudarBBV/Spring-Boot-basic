package book.shop.spring.boot.intro.service;

import book.shop.spring.boot.intro.dto.UserRegistrationRequestDto;
import book.shop.spring.boot.intro.dto.UserResponseDto;

public interface UserService {
    UserResponseDto register(UserRegistrationRequestDto requestDto);

    void deleteById(Long id);
}
