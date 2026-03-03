package book.shop.spring.boot.intro.controller;

import static org.assertj.core.api.Assertions.assertThat;

import book.shop.spring.boot.intro.config.TestSecurityConfig;
import book.shop.spring.boot.intro.dto.CreateCategoryRequestDto;
import book.shop.spring.boot.intro.model.Category;
import book.shop.spring.boot.intro.repository.CategoryRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Import(TestSecurityConfig.class)
class CategoryControllerTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CategoryRepository categoryRepository;

    private Category createCategory(CreateCategoryRequestDto request) throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<String> entity = new HttpEntity<>(objectMapper.writeValueAsString(request), headers);

        ResponseEntity<Category> response = restTemplate
                .withBasicAuth("admin", "password")
                .postForEntity("http://localhost:" + port + "/categories", entity, Category.class);

        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        return response.getBody();
    }

    @Test
    @DisplayName("Create category - success")
    void createCategory_ValidRequest_ReturnsCreatedCategory() throws Exception {
        CreateCategoryRequestDto request = new CreateCategoryRequestDto("Fantasy", "Magical books");

        Category createdCategory = createCategory(request);

        assertThat(createdCategory).isNotNull();
        assertThat(createdCategory.getId()).isNotNull();
        assertThat(createdCategory.getName()).isEqualTo("Fantasy");
        assertThat(createdCategory.getDescription()).isEqualTo("Magical books");
    }

    @Test
    @DisplayName("Get all categories - returns array")
    void getAllCategories_ReturnsList() throws Exception {
        createCategory(new CreateCategoryRequestDto("Sample", "Sample description"));

        ResponseEntity<Category[]> response = restTemplate
                .withBasicAuth("user", "password")
                .getForEntity("http://localhost:" + port + "/categories", Category[].class);

        Category[] categories = response.getBody();
        assertThat(categories).isNotEmpty();
        assertThat(categories[0].getName()).isNotNull();
    }

    @Test
    @DisplayName("Get category by ID - success")
    void getCategoryById_ReturnsCategory() throws Exception {
        Category createdCategory = createCategory(new CreateCategoryRequestDto("Sci-Fi", "Science fiction books"));

        ResponseEntity<Category> response = restTemplate
                .withBasicAuth("user", "password")
                .getForEntity("http://localhost:" + port + "/categories/" + createdCategory.getId(), Category.class);

        Category category = response.getBody();
        assertThat(category).isNotNull();
        assertThat(category.getId()).isEqualTo(createdCategory.getId());
        assertThat(category.getName()).isEqualTo("Sci-Fi");
        assertThat(category.getDescription()).isEqualTo("Science fiction books");
    }

    @Test
    @DisplayName("Update category - success")
    void updateCategory_ReturnsUpdatedCategory() throws Exception {
        Category createdCategory = createCategory(new CreateCategoryRequestDto("History", "Historical books"));

        CreateCategoryRequestDto updateRequest = new CreateCategoryRequestDto("Updated History", "Updated description");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<>(objectMapper.writeValueAsString(updateRequest), headers);

        restTemplate.withBasicAuth("admin", "password")
                .put("http://localhost:" + port + "/categories/" + createdCategory.getId(), entity);

        ResponseEntity<Category> response = restTemplate
                .withBasicAuth("user", "password")
                .getForEntity("http://localhost:" + port + "/categories/" + createdCategory.getId(), Category.class);

        Category updatedCategory = response.getBody();
        assertThat(updatedCategory.getName()).isEqualTo("Updated History");
        assertThat(updatedCategory.getDescription()).isEqualTo("Updated description");
    }

    @Test
    @DisplayName("Delete category - success (soft delete)")
    void deleteCategory_ReturnsOk() throws Exception {
        Category createdCategory = createCategory(new CreateCategoryRequestDto("To delete", "Description"));

        restTemplate.withBasicAuth("admin", "password")
                .delete("http://localhost:" + port + "/categories/" + createdCategory.getId());

        Category deletedCategory = categoryRepository.findById(createdCategory.getId()).orElseThrow();
        assertThat(deletedCategory.isDeleted()).isTrue();
    }

    @Test
    @DisplayName("Get books by category ID - success")
    void getBooksByCategory_ReturnsPagedBooks() throws Exception {
        Category category = createCategory(new CreateCategoryRequestDto("Category for books", "Description"));

        ResponseEntity<String> response = restTemplate
                .withBasicAuth("user", "password")
                .getForEntity("http://localhost:" + port + "/categories/by-category/"
                        + category.getId() + "?page=0&size=10", String.class);

        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(response.getBody()).contains("\"content\":");
    }
}
