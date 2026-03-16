package book.shop.spring.boot.intro.controller;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.junit.jupiter.api.Assertions.assertTrue;

import book.shop.spring.boot.intro.config.TestSecurityConfig;
import book.shop.spring.boot.intro.dto.BookDto;
import book.shop.spring.boot.intro.dto.CreateBookRequestDto;
import book.shop.spring.boot.intro.dto.UpdateBookRequestDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.apache.commons.lang3.builder.EqualsBuilder;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(TestSecurityConfig.class)
class BookControllerTest {

    protected static MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeAll
    static void beforeAll(@Autowired WebApplicationContext applicationContext) {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(applicationContext)
                .apply(springSecurity())
                .build();
    }

    @Test
    @DisplayName("Create book")
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    @Sql(scripts = "classpath:database/categories/add-categories-to-table.sql",
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = "classpath:database/books/remove-books-from-table-books.sql",
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void createBook_ValidRequestDto_ReturnBook() throws Exception {
        CreateBookRequestDto requestDto = new CreateBookRequestDto(
                "New Book",
                "New Author",
                "9783161484102",
                BigDecimal.valueOf(39.99),
                "Test description",
                "http://example.com/new.jpg",
                List.of(1L)
        );

        String jsonRequest = objectMapper.writeValueAsString(requestDto);

        MvcResult result = mockMvc.perform(
                        post("/books")
                                .with(csrf())
                                .content(jsonRequest)
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andReturn();

        BookDto actual = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                BookDto.class
        );

        BookDto expected = new BookDto();
        expected.setTitle("New Book");
        expected.setAuthor("New Author");
        expected.setPrice(BigDecimal.valueOf(39.99));
        expected.setDescription("Test description");
        expected.setCoverImage("http://example.com/new.jpg");

        assertTrue(
                EqualsBuilder.reflectionEquals(expected, actual, "id")
        );
    }

    @Test
    @DisplayName("Get all books")
    @WithMockUser(username = "user", roles = {"USER"})
    @Sql(scripts = {
            "classpath:database/categories/add-categories-to-table.sql",
            "classpath:database/books/add-books-to-table.sql",
            "classpath:database/books/add-books-and-categories-into-table.sql"
    }, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = {
            "classpath:database/books/remove-books-from-table-books.sql",
            "classpath:database/categories/delete-categories.sql"
    }, executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void findAll_GivenBooks_ReturnPage() throws Exception {
        MvcResult result = mockMvc.perform(
                        get("/books")
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        String json = result.getResponse().getContentAsString();

        assertTrue(json.contains("Test Book 1"));
        assertTrue(json.contains("Test Book 2"));
    }

    @Test
    @DisplayName("Get book by id")
    @WithMockUser(username = "user", roles = {"USER"})
    @Sql(scripts = {
            "classpath:database/schemas/clear-all-tables.sql",
            "classpath:database/categories/add-categories-to-table.sql",
            "classpath:database/books/add-books-to-table.sql",
            "classpath:database/books/add-books-and-categories-into-table.sql"
    }, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = {
            "classpath:database/categories/delete-categories.sql",
            "classpath:database/books/remove-books-from-table-books.sql",
    }, executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void findById_ExistingId_ReturnBook() throws Exception {
        MvcResult result = mockMvc.perform(
                        get("/books/1")
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        BookDto actual = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                BookDto.class
        );

        BookDto expected = new BookDto();
        expected.setId(1L);
        expected.setTitle("Test Book 1");

        assertTrue(
                EqualsBuilder.reflectionEquals(expected, actual, "author", "price", "description", "coverImage")
        );
    }

    @Test
    @DisplayName("Update book")
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    @Sql(scripts = {
            "classpath:database/categories/add-categories-to-table.sql",
            "classpath:database/books/add-books-to-table.sql",
            "classpath:database/books/add-books-and-categories-into-table.sql"
    }, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = {
            "classpath:database/books/remove-books-from-table-books.sql",
            "classpath:database/categories/delete-categories.sql"
    }, executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void updateBook_ValidRequest_ReturnUpdatedBook() throws Exception {
        UpdateBookRequestDto requestDto = new UpdateBookRequestDto();
        requestDto.setTitle("Updated Book");
        requestDto.setAuthor("Updated Author");
        requestDto.setIsbn("9783161484105");
        requestDto.setPrice(BigDecimal.valueOf(49.99));
        requestDto.setDescription("Updated description");
        requestDto.setCoverImage("http://example.com/updated.jpg");

        String jsonRequest = objectMapper.writeValueAsString(requestDto);

        MvcResult result = mockMvc.perform(
                        put("/books/1")
                                .with(csrf())
                                .content(jsonRequest)
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        BookDto actual = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                BookDto.class
        );

        BookDto expected = new BookDto();
        expected.setTitle("Updated Book");

        assertTrue(
                EqualsBuilder.reflectionEquals(expected, actual,
                        "id", "author", "price", "description", "coverImage")
        );
    }

    @Test
    @DisplayName("Delete book")
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    @Sql(scripts = {
            "classpath:database/categories/add-categories-to-table.sql",
            "classpath:database/books/add-books-to-table.sql",
            "classpath:database/books/add-books-and-categories-into-table.sql"
    }, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = {
            "classpath:database/books/remove-books-from-table-books.sql",
            "classpath:database/categories/delete-categories.sql"
    }, executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void deleteBook_ExistingId_Success() throws Exception {
        mockMvc.perform(
                        delete("/books/1")
                                .with(csrf()))
                .andExpect(status().isNoContent());
    }
}
