package book.shop.spring.boot.intro.util;

import book.shop.spring.boot.intro.dto.UserRegistrationRequestDto;
import book.shop.spring.boot.intro.dto.UserResponseDto;
import book.shop.spring.boot.intro.model.User;
import book.shop.spring.boot.intro.service.UserService;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Profile("test")
@Primary
@Service
public class FakeUserService implements UserService {

    @Override
    public UserResponseDto register(UserRegistrationRequestDto requestDto) {
        return new UserResponseDto(
                1L,
                requestDto.getEmail(),
                requestDto.getFirstName(),
                requestDto.getLastName(),
                requestDto.getShippingAddress()
        );
    }

    @Override
    public void deleteById(Long id) {
        // no-op for test
    }

    @Override
    public Long getAuthenticatedUserId() {
        return 1L;
    }

    @Override
    public void createShoppingCart(User user) {
        // no-op for test
    }
}