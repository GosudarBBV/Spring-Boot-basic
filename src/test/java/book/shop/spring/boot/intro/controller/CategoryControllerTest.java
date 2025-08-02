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
import book.shop.spring.boot.intro.dto.CreateCategoryRequestDto;
import book.shop.spring.boot.intro.model.Category;
import book.shop.spring.boot.intro.repository.CategoryRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
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
class CategoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CategoryRepository categoryRepository;

    @AfterEach
    void tearDown() {
        categoryRepository.deleteAll();
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
    @DisplayName("Get all categories - success")
    void getAllCategories_ReturnsList() throws Exception {
        Category category = new Category();
        category.setName("Fiction");
        category.setDescription("Fictional books");
        categoryRepository.save(category);

        mockMvc.perform(get("/categories")
                        .with(user("user").roles("USER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Fiction"))
                .andExpect(jsonPath("$[0].description").value("Fictional books"));
    }

    @Test
    @DisplayName("Get category by ID - success")
    void getCategoryById_ReturnsCategory() throws Exception {
        CreateCategoryRequestDto createRequest = new CreateCategoryRequestDto("Sci-Fi", "Science fiction books");

        String jsonResponse = mockMvc.perform(post("/categories")
                        .with(csrf())
                        .with(user("admin").roles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Long categoryId = objectMapper.readTree(jsonResponse).get("id").asLong();

        mockMvc.perform(get("/categories/{id}", categoryId)
                        .with(user("user").roles("USER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(categoryId))
                .andExpect(jsonPath("$.name").value("Sci-Fi"))
                .andExpect(jsonPath("$.description").value("Science fiction books"));
    }

    @Test
    @DisplayName("Update category - success")
    void updateCategory_ReturnsUpdatedCategory() throws Exception {
        CreateCategoryRequestDto createRequest = new CreateCategoryRequestDto("History", "Historical books");

        String jsonResponse = mockMvc.perform(post("/categories")
                        .with(csrf())
                        .with(user("admin").roles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Long categoryId = objectMapper.readTree(jsonResponse).get("id").asLong();

        CreateCategoryRequestDto updateRequest = new CreateCategoryRequestDto("Updated History", "Updated description");

        mockMvc.perform(put("/categories/{id}", categoryId)
                        .with(csrf())
                        .with(user("admin").roles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(categoryId))
                .andExpect(jsonPath("$.name").value("Updated History"))
                .andExpect(jsonPath("$.description").value("Updated description"));
    }

    @Test
    @DisplayName("Delete category - success")
    void deleteCategory_ReturnsOk() throws Exception {
        CreateCategoryRequestDto createRequest = new CreateCategoryRequestDto("To delete", "Description");

        String jsonResponse = mockMvc.perform(post("/categories")
                        .with(csrf())
                        .with(user("admin").roles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Long categoryId = objectMapper.readTree(jsonResponse).get("id").asLong();

        mockMvc.perform(delete("/categories/{id}", categoryId)
                        .with(csrf())
                        .with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk());

        mockMvc.perform(get("/categories/{id}", categoryId)
                        .with(user("user").roles("USER")))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Get books by category ID - success")
    void getBooksByCategory_ReturnsPagedBooks() throws Exception {
        CreateCategoryRequestDto createRequest = new CreateCategoryRequestDto("Category for books", "Description");

        String jsonResponse = mockMvc.perform(post("/categories")
                        .with(csrf())
                        .with(user("admin").roles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Long categoryId = objectMapper.readTree(jsonResponse).get("id").asLong();

        mockMvc.perform(get("/categories/by-category/{id}?page=0&size=10", categoryId)
                        .with(user("user").roles("USER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }
}

