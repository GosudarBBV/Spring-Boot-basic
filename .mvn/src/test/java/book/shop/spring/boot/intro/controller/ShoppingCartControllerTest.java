package book.shop.spring.boot.intro.controller;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import book.shop.spring.boot.intro.config.TestSecurityConfig;
import book.shop.spring.boot.intro.dto.AddCartItemRequestDto;
import book.shop.spring.boot.intro.dto.CartItemResponseDto;
import book.shop.spring.boot.intro.dto.ShoppingCartResponseDto;
import book.shop.spring.boot.intro.dto.UpdateCartItemRequestDto;
import book.shop.spring.boot.intro.security.JwtUtil;
import book.shop.spring.boot.intro.service.ShoppingCartService;
import book.shop.spring.boot.intro.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestSecurityConfig.class)
class ShoppingCartControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ShoppingCartService shoppingCartService;

    @MockBean
    private UserService userService;

    @MockBean
    private JwtUtil jwtUtil;

    @Test
    @DisplayName("Add book to cart returns updated cart")
    void addBookToCart_ValidRequest_ReturnsCart() throws Exception {
        Long userId = 1L;
        AddCartItemRequestDto request = new AddCartItemRequestDto(2L, 3);

        CartItemResponseDto cartItem = new CartItemResponseDto(
                100L, 2L, "The Great Gatsby", 3
        );
        ShoppingCartResponseDto response = new ShoppingCartResponseDto(
                1L, userId, List.of(cartItem)
        );

        when(userService.getAuthenticatedUserId()).thenReturn(userId);
        when(shoppingCartService.addBookToCart(eq(2L), eq(3), eq(userId)))
                .thenReturn(response);

        mockMvc.perform(post("/cart")
                        .with(csrf())
                        .with(user("user").roles("USER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(response)));
    }

    @Test
    @DisplayName("Get current user's cart returns shopping cart")
    void getShoppingCart_ReturnsCart() throws Exception {
        Long userId = 1L;

        CartItemResponseDto cartItem = new CartItemResponseDto(
                101L, 3L, "1984", 2
        );
        ShoppingCartResponseDto response = new ShoppingCartResponseDto(
                1L, userId, List.of(cartItem)
        );

        when(userService.getAuthenticatedUserId()).thenReturn(userId);
        when(shoppingCartService.getCartByUser(userId)).thenReturn(response);

        mockMvc.perform(get("/cart")
                        .with(user("user").roles("USER")))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(response)));
    }

    @Test
    @DisplayName("Update cart item returns updated cart")
    void updateCartItem_ValidRequest_ReturnsCart() throws Exception {
        Long userId = 1L;
        Long cartItemId = 200L;
        UpdateCartItemRequestDto request = new UpdateCartItemRequestDto(5);

        CartItemResponseDto updatedItem = new CartItemResponseDto(
                cartItemId, 5L, "Dune", 5
        );
        ShoppingCartResponseDto response = new ShoppingCartResponseDto(
                2L, userId, List.of(updatedItem)
        );

        when(userService.getAuthenticatedUserId()).thenReturn(userId);
        when(shoppingCartService.updateCartItem(eq(cartItemId), eq(request), eq(userId)))
                .thenReturn(response);

        mockMvc.perform(put("/cart/items/{cartItemId}", cartItemId)
                        .with(csrf())
                        .with(user("user").roles("USER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(response)));
    }

    @Test
    @DisplayName("Delete cart item returns 204")
    void deleteCartItem_ValidRequest_ReturnsNoContent() throws Exception {
        Long userId = 1L;
        Long cartItemId = 300L;

        when(userService.getAuthenticatedUserId()).thenReturn(userId);

        mockMvc.perform(delete("/cart/items/{cartItemId}", cartItemId)
                        .with(csrf())
                        .with(user("user").roles("USER")))
                .andExpect(status().isNoContent());
    }
}
