package book.shop.spring.boot.intro.service;

import book.shop.spring.boot.intro.dto.UserRegistrationRequestDto;
import book.shop.spring.boot.intro.dto.UserResponseDto;
import book.shop.spring.boot.intro.exception.RegistrationException;
import book.shop.spring.boot.intro.mapper.UserMapper;
import book.shop.spring.boot.intro.model.User;
import book.shop.spring.boot.intro.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Override
    public UserResponseDto register(UserRegistrationRequestDto requestDto) {
        if (userRepository.existsByEmail(requestDto.getEmail())) {
            throw new RegistrationException("User already exists with email: "
                    + requestDto.getEmail());
        }

        User user = new User();
        user.setEmail(requestDto.getEmail());
        user.setPassword(requestDto.getPassword());
        User savedUser = userRepository.save(user);
        return userMapper.toResponseDto(savedUser);
    }
}
