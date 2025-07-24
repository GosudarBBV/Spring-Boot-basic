package book.shop.spring.boot.intro.controller;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import book.shop.spring.boot.intro.config.TestSecurityConfig;
import book.shop.spring.boot.intro.security.JwtUtil;
import book.shop.spring.boot.intro.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(UserController.class)
@Import(TestSecurityConfig.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @MockBean
    private JwtUtil jwtUtil;

    @Test
    @DisplayName("Delete user by ID - success with ADMIN role")
    void deleteUser_ByAdminRole_ReturnsNoContent() throws Exception {
        Long userId = 5L;
        doNothing().when(userService).deleteById(userId);

        mockMvc.perform(delete("/users/{id}", userId)
                        .with(csrf())
                        .with(user("admin").roles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        verify(userService).deleteById(userId);
    }

    @DisplayName("Given user with USER role, when DELETE /users/{id}, then status 403")
    @Test
    void deleteById_UserRole_Forbidden() throws Exception {
        Long userId = 5L;

        mockMvc.perform(delete("/users/{id}", userId)
                        .with(csrf())
                        .with(SecurityMockMvcRequestPostProcessors.user("user").roles("USER"))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Delete user by ID - unauthorized when not authenticated")
    void deleteUser_WithoutAuthentication_ReturnsUnauthorized() throws Exception {
        Long userId = 5L;

        mockMvc.perform(delete("/users/{id}", userId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }
}
