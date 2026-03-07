package book.shop.spring.boot.intro.controller;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import book.shop.spring.boot.intro.config.TestSecurityConfig;
import book.shop.spring.boot.intro.dto.BookDtoWithoutCategoryIds;
import book.shop.spring.boot.intro.dto.CategoryDto;
import book.shop.spring.boot.intro.dto.CategoryResponseDto;
import book.shop.spring.boot.intro.dto.CreateCategoryRequestDto;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.testcontainers.shaded.org.apache.commons.lang3.builder.EqualsBuilder;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(TestSecurityConfig.class)
class CategoryControllerTest {

    protected static MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeAll
    static void beforeAll(
            @Autowired DataSource dataSource,
            @Autowired WebApplicationContext applicationContext
    ) throws SQLException {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(applicationContext)
                .apply(springSecurity())
                .build();

        try (Connection connection = dataSource.getConnection()) {
            connection.setAutoCommit(true);
            ScriptUtils.executeSqlScript(connection,
                    new ClassPathResource("database/categories/add-categories-to-table.sql"));
        }
    }

    @Test
    @DisplayName("Create a new category")
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    @Sql(scripts = "classpath:database/categories/delete-categories.sql",
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void createCategory_ValidRequestDto_Success() throws Exception {
        CreateCategoryRequestDto requestDto
                = new CreateCategoryRequestDto("Category1", "C1");

        CategoryDto expected = new CategoryDto()
                .setName(requestDto.name())
                .setDescription(requestDto.description());

        String jsonRequest = objectMapper.writeValueAsString(requestDto);

        MvcResult result = mockMvc.perform(
                        post("/categories")
                                .with(csrf())
                                .content(jsonRequest)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isCreated())
                .andReturn();

        CategoryResponseDto actual = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                CategoryResponseDto.class
        );

        EqualsBuilder.reflectionEquals(expected, actual, "id");
    }

    @Test
    @DisplayName("Get all categories")
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    @Sql(scripts = "classpath:database/categories/add-categories-to-table.sql",
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = "classpath:database/categories/delete-categories.sql",
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void getAllCategories_ShouldReturnAll() throws Exception {
        List<CategoryDto> expected = new ArrayList<>();
        expected.add(new CategoryDto().setId(1L).setName("Test a"));
        expected.add(new CategoryDto().setId(2L).setName("Test b"));

        MvcResult result = mockMvc.perform(
                        get("/categories")
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andReturn();

        CategoryResponseDto[] actual = objectMapper.readValue(
                result.getResponse().getContentAsByteArray(),
                CategoryResponseDto[].class
        );

        Assertions.assertEquals(2, actual.length);
        Assertions.assertEquals(expected, Arrays.stream(actual).toList());
    }

    @Test
    @DisplayName("Get category by ID")
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void getCategoryById_ExistingId_ReturnsCategory() throws Exception {
        MvcResult result = mockMvc.perform(
                        get("/categories/1")
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andReturn();

        CategoryDto actual = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                CategoryDto.class
        );

        Assertions.assertEquals(1L, actual.getId());
        Assertions.assertEquals("Test a", actual.getName());
    }

    @Test
    @DisplayName("Update category")
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void updateCategory_ExistingId_Success() throws Exception {
        CreateCategoryRequestDto requestDto
                = new CreateCategoryRequestDto("Updated Name",
                "Updated Desc");
        String jsonRequest = objectMapper.writeValueAsString(requestDto);

        MvcResult result = mockMvc.perform(
                        put("/categories/1")
                                .with(csrf())
                                .content(jsonRequest)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andReturn();

        CategoryDto actual = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                CategoryDto.class
        );

        Assertions.assertEquals("Updated Name", actual.getName());
        Assertions.assertEquals("Updated Desc", actual.getDescription());
    }

    @Test
    @DisplayName("Delete category")
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void deleteCategory_ExistingId_Success() throws Exception {
        mockMvc.perform(
                        delete("/categories/1")
                                .with(csrf())
                )
                .andExpect(status().isOk());

        mockMvc.perform(
                        get("/categories/1")
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Get books by category ID")
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void getBooksByCategoryId_ShouldReturnPage() throws Exception {
        MvcResult result = mockMvc.perform(
                        get("/categories/by-category/1")
                                .param("page", "0")
                                .param("size", "10")
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andReturn();

        PageImpl<BookDtoWithoutCategoryIds> page =
                objectMapper.readValue(result.getResponse().getContentAsString(),
                        objectMapper.getTypeFactory().constructParametricType(PageImpl.class,
                                BookDtoWithoutCategoryIds.class));

        Assertions.assertNotNull(page);
    }
}
