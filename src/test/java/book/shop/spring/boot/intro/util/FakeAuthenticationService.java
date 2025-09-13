package book.shop.spring.boot.intro.util;

import book.shop.spring.boot.intro.dto.UserLoginRequestDto;
import book.shop.spring.boot.intro.dto.UserLoginResponseDto;
import book.shop.spring.boot.intro.security.AuthenticationService;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.context.annotation.Primary;

@Profile("test")
@Primary
@Service
public class FakeAuthenticationService extends AuthenticationService {

    public FakeAuthenticationService() {
        super(null, null);
    }

    @Override
    public UserLoginResponseDto authenticate(UserLoginRequestDto requestDto) {
        return new UserLoginResponseDto("fake-jwt-token");
    }
}
