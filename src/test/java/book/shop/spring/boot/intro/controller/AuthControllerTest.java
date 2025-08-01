package book.shop.spring.boot.intro.controller;

import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.any;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import book.shop.spring.boot.intro.config.TestSecurityConfig;
import book.shop.spring.boot.intro.dto.UserLoginRequestDto;
import book.shop.spring.boot.intro.dto.UserLoginResponseDto;
import book.shop.spring.boot.intro.dto.UserRegistrationRequestDto;
import book.shop.spring.boot.intro.dto.UserResponseDto;
import book.shop.spring.boot.intro.security.AuthenticationService;
import book.shop.spring.boot.intro.security.JwtUtil;
import book.shop.spring.boot.intro.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(AuthController.class)
@Import(TestSecurityConfig.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthenticationService authenticationService;

    @MockBean
    private UserService userService;

    @MockBean
    private JwtUtil jwtUtil;

    @Test
    @DisplayName("Login with valid credentials returns token")
    void login_ValidCredentials_ReturnsToken() throws Exception {
        var request = new UserLoginRequestDto("test@example.com", "pass123");
        var response = new UserLoginResponseDto("jwt-token");

        when(authenticationService.authenticate(any())).thenReturn(response);

        mockMvc.perform(post("/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(response)));
    }

    @Test
    @DisplayName("Registration returns user DTO")
    void registerUser_ValidRequest_ReturnsUserResponse() throws Exception {
        var request = new UserRegistrationRequestDto();
        request.setEmail("test@example.com");
        request.setPassword("pass123");
        request.setRepeatPassword("pass123");
        request.setFirstName("John");
        request.setLastName("Doe");
        request.setShippingAddress("Kyiv");

        var response = new UserResponseDto(
                1L, "test@example.com", "John", "Doe", "Kyiv"
        );

        when(userService.register(any())).thenReturn(response);

        mockMvc.perform(post("/auth/registration")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(response)));
    }
}
