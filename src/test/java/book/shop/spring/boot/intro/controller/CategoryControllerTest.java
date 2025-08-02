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
import book.shop.spring.boot.intro.dto.CategoryResponseDto;
import book.shop.spring.boot.intro.dto.CreateCategoryRequestDto;
import book.shop.spring.boot.intro.model.Category;
import book.shop.spring.boot.intro.repository.CategoryRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestSecurityConfig.class)
class CategoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CategoryRepository categoryRepository;

    @Test
    @DisplayName("Create category - should return created category")
    void createCategory_ValidRequest_ReturnsCreatedCategory() throws Exception {
        CreateCategoryRequestDto request = new CreateCategoryRequestDto("History", "Books about history");

        mockMvc.perform(post("/categories")
                        .with(csrf())
                        .with(user("admin").roles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("History"))
                .andExpect(jsonPath("$.description").value("Books about history"));
    }

    @Test
    @DisplayName("Get category by ID - should return category")
    void getCategoryById_ReturnsCategory() throws Exception {
        Category category = new Category();
        category.setName("Science");
        category.setDescription("Books about science");
        category = categoryRepository.save(category);

        mockMvc.perform(get("/categories/{id}", category.getId())
                        .with(user("user").roles("USER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(category.getId()))
                .andExpect(jsonPath("$.name").value("Science"));
    }

    @Test
    @DisplayName("Update category - should return updated category")
    void updateCategory_ReturnsUpdatedCategory() throws Exception {
        Category category = new Category();
        category.setName("History");
        category.setDescription("Some desc");
        category = categoryRepository.save(category);

        CreateCategoryRequestDto request = new CreateCategoryRequestDto("Updated History", "Updated desc");

        mockMvc.perform(put("/categories/{id}", category.getId())
                        .with(csrf())
                        .with(user("admin").roles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated History"))
                .andExpect(jsonPath("$.description").value("Updated desc"));
    }

    @Test
    @DisplayName("Delete category - should return 200")
    void deleteCategory_ReturnsOk() throws Exception {
        Category category = new Category();
        category.setName("To Delete");
        category.setDescription("Temp");
        category = categoryRepository.save(category);

        mockMvc.perform(delete("/categories/{id}", category.getId())
                        .with(csrf())
                        .with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk());

        boolean exists = categoryRepository.existsById(category.getId());
        assert !exists : "Category should be deleted from DB";
    }

    @Test
    @DisplayName("Get all categories - should return list")
    void getAllCategories_ReturnsList() throws Exception {
        Category category = new Category();
        category.setName("Fiction");
        category.setDescription("Fictional books");
        categoryRepository.save(category);

        mockMvc.perform(get("/categories")
                        .with(user("user").roles("USER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Fiction"));
    }
}
