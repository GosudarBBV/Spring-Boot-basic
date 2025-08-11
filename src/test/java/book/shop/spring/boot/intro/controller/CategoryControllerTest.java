package book.shop.spring.boot.intro.controller;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import book.shop.spring.boot.intro.config.TestSecurityConfig;
import book.shop.spring.boot.intro.dto.CreateCategoryRequestDto;
import book.shop.spring.boot.intro.model.Category;
import book.shop.spring.boot.intro.repository.CategoryRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(TestSecurityConfig.class)
class CategoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CategoryRepository categoryRepository;

    private Long createCategoryAndGetId(CreateCategoryRequestDto request) throws Exception {
        String response = mockMvc.perform(post("/categories")
                        .with(csrf())
                        .with(user("admin").roles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andReturn()
                .getResponse()
                .getContentAsString();

        return objectMapper.readTree(response).get("id").asLong();
    }

    @Test
    @DisplayName("Create category - success")
    void createCategory_ValidRequest_ReturnsCreatedCategory() throws Exception {
        CreateCategoryRequestDto request = new CreateCategoryRequestDto("Fantasy", "Magical books");

        mockMvc.perform(post("/categories")
                        .with(csrf())
                        .with(user("admin").roles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Fantasy"))
                .andExpect(jsonPath("$.description").value("Magical books"))
                .andExpect(jsonPath("$.id").isNumber());
    }

    @Test
    @DisplayName("Get all categories - returns array")
    void getAllCategories_ReturnsList() throws Exception {
        createCategoryAndGetId(new CreateCategoryRequestDto("Sample", "Sample description"));

        mockMvc.perform(get("/categories")
                        .with(user("user").roles("USER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].name").exists());
    }

    @Test
    @DisplayName("Get category by ID - success")
    void getCategoryById_ReturnsCategory() throws Exception {
        CreateCategoryRequestDto request = new CreateCategoryRequestDto("Sci-Fi", "Science fiction books");
        Long id = createCategoryAndGetId(request);

        mockMvc.perform(get("/categories/{id}", id)
                        .with(user("user").roles("USER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.name").value("Sci-Fi"))
                .andExpect(jsonPath("$.description").value("Science fiction books"));
    }

    @Test
    @DisplayName("Update category - success")
    void updateCategory_ReturnsUpdatedCategory() throws Exception {
        CreateCategoryRequestDto createRequest = new CreateCategoryRequestDto("History", "Historical books");
        Long id = createCategoryAndGetId(createRequest);

        CreateCategoryRequestDto updateRequest = new CreateCategoryRequestDto("Updated History", "Updated description");

        mockMvc.perform(put("/categories/{id}", id)
                        .with(csrf())
                        .with(user("admin").roles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.name").value("Updated History"))
                .andExpect(jsonPath("$.description").value("Updated description"));

        mockMvc.perform(get("/categories/{id}", id)
                        .with(user("user").roles("USER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated History"))
                .andExpect(jsonPath("$.description").value("Updated description"));
    }

    @Test
    @DisplayName("Delete category - success")
    void deleteCategory_ReturnsOk() throws Exception {
        CreateCategoryRequestDto request = new CreateCategoryRequestDto("To delete", "Description");
        Long id = createCategoryAndGetId(request);

        mockMvc.perform(delete("/categories/{id}", id)
                        .with(csrf())
                        .with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk());

        Category deletedCategory = categoryRepository.findById(id).orElseThrow();
        assertTrue(deletedCategory.isDeleted(), "Category should be marked as deleted");

    }

    @Test
    @DisplayName("Get books by category ID - success")
    void getBooksByCategory_ReturnsPagedBooks() throws Exception {
        CreateCategoryRequestDto request = new CreateCategoryRequestDto("Category for books", "Description");
        Long categoryId = createCategoryAndGetId(request);

        mockMvc.perform(get("/categories/by-category/{id}?page=0&size=10", categoryId)
                        .with(user("user").roles("USER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }
}
