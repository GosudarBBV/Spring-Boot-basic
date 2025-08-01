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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
    @DisplayName("Registration returns user DTO")
    void registerUser_ValidRequest_ReturnsUserResponse() throws Exception {
        var request = new UserRegistrationRequestDto();
        request.setEmail("test@example.com");
        request.setPassword("pass123");
        request.setRepeatPassword("pass123");
        request.setFirstName("John");
        request.setLastName("Doe");
        request.setShippingAddress("Kyiv");

        mockMvc.perform(post("/auth/registration")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.firstName").value("John"))
                .andExpect(jsonPath("$.lastName").value("Doe"))
                .andExpect(jsonPath("$.shippingAddress").value("Kyiv"));
    }

    @Test
    @DisplayName("Login with valid credentials returns token")
    void login_ValidCredentials_ReturnsToken() throws Exception {
        // Спочатку зареєструємо користувача
        UserRegistrationRequestDto registration = new UserRegistrationRequestDto();
        registration.setEmail("loginuser@example.com");
        registration.setPassword("pass123");
        registration.setRepeatPassword("pass123");
        registration.setFirstName("Jane");
        registration.setLastName("Smith");
        registration.setShippingAddress("Lviv");

        mockMvc.perform(post("/auth/registration")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registration)))
                .andExpect(status().isOk());

        // Тепер логін
        UserLoginRequestDto login = new UserLoginRequestDto("loginuser@example.com", "pass123");

        mockMvc.perform(post("/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(login)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty());
    }
}
