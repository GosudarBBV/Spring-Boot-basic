package book.shop.spring.boot.intro.controller;

import book.shop.spring.boot.intro.config.TestSecurityConfig;
import book.shop.spring.boot.intro.dto.UserLoginRequestDto;
import book.shop.spring.boot.intro.dto.UserRegistrationRequestDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Import(TestSecurityConfig.class)
@ActiveProfiles("test")
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("Registration should return user response DTO")
    void registerUser_ValidRequest_ReturnsUserResponse() throws Exception {
        UserRegistrationRequestDto request = new UserRegistrationRequestDto();
        request.setEmail("user1@example.com");
        request.setPassword("pass123");
        request.setRepeatPassword("pass123");
        request.setFirstName("Ivan");
        request.setLastName("Petrenko");
        request.setShippingAddress("Lviv");

        mockMvc.perform(post("/auth/registration")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("user1@example.com"))
                .andExpect(jsonPath("$.firstName").value("Ivan"))
                .andExpect(jsonPath("$.lastName").value("Petrenko"))
                .andExpect(jsonPath("$.shippingAddress").value("Lviv"));
    }

    @Test
    @DisplayName("Login with valid credentials should return JWT token")
    void login_ValidCredentials_ReturnsToken() throws Exception {

        UserRegistrationRequestDto registrationRequest = new UserRegistrationRequestDto();
        registrationRequest.setEmail("user2@example.com");
        registrationRequest.setPassword("pass123");
        registrationRequest.setRepeatPassword("pass123");
        registrationRequest.setFirstName("Olga");
        registrationRequest.setLastName("Kovalchuk");
        registrationRequest.setShippingAddress("Kyiv");

        mockMvc.perform(post("/auth/registration")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registrationRequest)))
                .andExpect(status().isOk());

        // Логін
        UserLoginRequestDto loginRequest = new UserLoginRequestDto(
                "user2@example.com", "pass123"
        );

        mockMvc.perform(post("/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty());
    }
}
