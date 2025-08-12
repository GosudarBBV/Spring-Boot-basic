package book.shop.spring.boot.intro.controller;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import book.shop.spring.boot.intro.config.TestSecurityConfig;
import book.shop.spring.boot.intro.dto.AddCartItemRequestDto;
import book.shop.spring.boot.intro.dto.UpdateCartItemRequestDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Import(TestSecurityConfig.class)
@ActiveProfiles("test")
@Sql(scripts = {
        "classpath:database/test/schema-cart.sql",
        "classpath:database/test/data-cart.sql"
}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = {
        "classpath:database/test/truncate-cart.sql"
}, executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
class ShoppingCartControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("Add book to cart returns updated cart")
    void addBookToCart_ValidRequest_ReturnsCart() throws Exception {
        AddCartItemRequestDto request = new AddCartItemRequestDto(1L, 3);

        mockMvc.perform(post("/cart")
                        .with(csrf())
                        .with(user("testuser@example.com").roles("USER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Get current user's cart returns shopping cart")
    void getShoppingCart_ReturnsCart() throws Exception {
        mockMvc.perform(get("/cart")
                        .with(user("testuser@example.com").roles("USER")))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Update cart item returns updated cart")
    void updateCartItem_ValidRequest_ReturnsCart() throws Exception {
        Long cartItemId = 1L;
        UpdateCartItemRequestDto request = new UpdateCartItemRequestDto(5);

        mockMvc.perform(put("/cart/items/{cartItemId}", cartItemId)
                        .with(csrf())
                        .with(user("testuser@example.com").roles("USER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Delete cart item returns 204 No Content")
    void deleteCartItem_ValidRequest_ReturnsNoContent() throws Exception {
        Long cartItemId = 1L;

        mockMvc.perform(delete("/cart/items/{cartItemId}", cartItemId)
                        .with(csrf())
                        .with(user("testuser@example.com").roles("USER")))
                .andExpect(status().isNoContent());
    }
}
