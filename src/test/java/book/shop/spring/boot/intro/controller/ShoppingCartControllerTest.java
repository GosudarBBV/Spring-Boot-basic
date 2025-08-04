package book.shop.spring.boot.intro.controller;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import book.shop.spring.boot.intro.config.TestSecurityConfig;
import book.shop.spring.boot.intro.dto.AddCartItemRequestDto;
import book.shop.spring.boot.intro.dto.UpdateCartItemRequestDto;
import book.shop.spring.boot.intro.model.Book;
import book.shop.spring.boot.intro.model.Role;
import book.shop.spring.boot.intro.model.RoleName;
import book.shop.spring.boot.intro.model.User;
import book.shop.spring.boot.intro.repository.BookRepository;
import book.shop.spring.boot.intro.repository.CartItemRepository;
import book.shop.spring.boot.intro.repository.RoleRepository;
import book.shop.spring.boot.intro.repository.ShoppingCartRepository;
import book.shop.spring.boot.intro.repository.UserRepository;
import book.shop.spring.boot.intro.util.TestEntityFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
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

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private ShoppingCartRepository shoppingCartRepository;

    @Autowired
    private CartItemRepository cartItemRepository;

    private User testUser;
    private Book testBook;

    @Test
    @DisplayName("Add book to cart returns updated cart")
    void addBookToCart_ValidRequest_ReturnsCart() throws Exception {
        Role userRole = new Role();
        userRole.setName(RoleName.USER);
        roleRepository.save(userRole);

        testUser = TestEntityFactory.createTestUser("user@example.com");
        testUser.setRoles(Set.of(userRole));
        testUser = userRepository.save(testUser);

        testBook = TestEntityFactory.createBook(null, "Test Book", new BigDecimal("99.99"));
        testBook = bookRepository.save(testBook);

        AddCartItemRequestDto request = new AddCartItemRequestDto(testBook.getId(), 2);

        mockMvc.perform(post("/cart")
                        .with(csrf())
                        .with(user(testUser.getEmail()).roles("USER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(testUser.getId()))
                .andExpect(jsonPath("$.cartItems[0].bookId").value(testBook.getId()))
                .andExpect(jsonPath("$.cartItems[0].quantity").value(2));
    }

    @Test
    @DisplayName("Get current user's cart returns shopping cart")
    void getShoppingCart_ReturnsCart() throws Exception {
        addBookToCart_ValidRequest_ReturnsCart();

        mockMvc.perform(get("/cart")
                        .with(user(testUser.getEmail()).roles("USER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(testUser.getId()))
                .andExpect(jsonPath("$.cartItems[0].bookId").value(testBook.getId()))
                .andExpect(jsonPath("$.cartItems[0].quantity").value(2));
    }

    @Test
    @DisplayName("Update cart item returns updated cart")
    void updateCartItem_ValidRequest_ReturnsCart() throws Exception {
        addBookToCart_ValidRequest_ReturnsCart();
        Long cartItemId = cartItemRepository.findAll().get(0).getId();

        UpdateCartItemRequestDto request = new UpdateCartItemRequestDto(5);

        mockMvc.perform(put("/cart/items/{cartItemId}", cartItemId)
                        .with(csrf())
                        .with(user(testUser.getEmail()).roles("USER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cartItems[0].id").value(cartItemId))
                .andExpect(jsonPath("$.cartItems[0].quantity").value(5));
    }

    @Test
    @DisplayName("Delete cart item returns 204")
    void deleteCartItem_ValidRequest_ReturnsNoContent() throws Exception {
        addBookToCart_ValidRequest_ReturnsCart();
        Long cartItemId = cartItemRepository.findAll().get(0).getId();

        mockMvc.perform(delete("/cart/items/{cartItemId}", cartItemId)
                        .with(csrf())
                        .with(user(testUser.getEmail()).roles("USER")))
                .andExpect(status().isNoContent());
    }
}
