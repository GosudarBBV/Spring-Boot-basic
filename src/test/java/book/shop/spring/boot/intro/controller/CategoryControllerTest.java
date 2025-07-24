package book.shop.spring.boot.intro.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import book.shop.spring.boot.intro.config.TestSecurityConfig;
import book.shop.spring.boot.intro.dto.BookDtoWithoutCategoryIds;
import book.shop.spring.boot.intro.dto.CategoryResponseDto;
import book.shop.spring.boot.intro.dto.CreateCategoryRequestDto;
import book.shop.spring.boot.intro.security.JwtUtil;
import book.shop.spring.boot.intro.service.BookService;
import book.shop.spring.boot.intro.service.CategoryService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(CategoryController.class)
@Import(TestSecurityConfig.class)
public class CategoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CategoryService categoryService;

    @MockBean
    private BookService bookService;

    @MockBean
    private JwtUtil jwtUtil;

    @Test
    @DisplayName("Create category - should return created category")
    void createCategory_ValidRequest_ReturnsCreatedCategory() throws Exception {
        CreateCategoryRequestDto request
                = new CreateCategoryRequestDto("Fantasy", "Magical books");
        CategoryResponseDto response
                = new CategoryResponseDto(1L, "Fantasy", "Magical books");

        when(categoryService.save(any(CreateCategoryRequestDto.class))).thenReturn(response);

        mockMvc.perform(post("/categories")
                        .with(csrf())
                        .with(user("admin").roles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(content().json(objectMapper.writeValueAsString(response)));
    }

    @Test
    @DisplayName("Get all categories - should return list")
    void getAllCategories_ReturnsList() throws Exception {
        CategoryResponseDto category = new CategoryResponseDto(1L,
                "Fiction", "Fictional books");
        when(categoryService.findAll()).thenReturn(List.of(category));

        mockMvc.perform(get("/categories")
                        .with(user("user").roles("USER")))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(List.of(category))));
    }

    @Test
    @DisplayName("Get category by ID - should return category")
    void getCategoryById_ReturnsCategory() throws Exception {
        CategoryResponseDto category = new CategoryResponseDto(1L,
                "Fiction", "Fictional books");
        when(categoryService.getById(1L)).thenReturn(category);

        mockMvc.perform(get("/categories/1")
                        .with(user("user").roles("USER")))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(category)));
    }

    @Test
    @DisplayName("Update category - should return updated category")
    void updateCategory_ReturnsUpdatedCategory() throws Exception {
        CreateCategoryRequestDto request = new CreateCategoryRequestDto("Updated",
                "Updated desc");
        CategoryResponseDto response = new CategoryResponseDto(1L,
                "Updated", "Updated desc");

        when(categoryService.update(any(Long.class), any(CreateCategoryRequestDto.class)))
                .thenReturn(response);

        mockMvc.perform(put("/categories/1")
                        .with(csrf())
                        .with(user("admin").roles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(response)));
    }

    @Test
    @DisplayName("Delete category - should return 200")
    void deleteCategory_ReturnsOk() throws Exception {
        mockMvc.perform(delete("/categories/1")
                        .with(csrf())
                        .with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Get books by category ID - should return paged books")
    void getBooksByCategory_ReturnsPageOfBooks() throws Exception {
        BookDtoWithoutCategoryIds book = new BookDtoWithoutCategoryIds(
                1L, "Title", "Author", new BigDecimal("9.99"),
                "Some description");
        Pageable pageable = PageRequest.of(0, 10);

        when(bookService.findAllByCategoryId(1L, pageable))
                .thenReturn(new PageImpl<>(Collections.singletonList(book)));

        mockMvc.perform(get("/categories/by-category/1?page=0&size=10")
                        .with(user("user").roles("USER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1L));
    }
}
