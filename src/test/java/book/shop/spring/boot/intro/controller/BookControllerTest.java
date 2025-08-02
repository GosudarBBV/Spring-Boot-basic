package book.shop.spring.boot.intro.controller;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.junit.jupiter.api.Assertions.assertTrue;

import book.shop.spring.boot.intro.config.TestSecurityConfig;
import book.shop.spring.boot.intro.dto.CreateBookRequestDto;
import book.shop.spring.boot.intro.dto.UpdateBookRequestDto;
import book.shop.spring.boot.intro.model.Book;
import book.shop.spring.boot.intro.repository.BookRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
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

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestSecurityConfig.class)
class BookControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private BookRepository bookRepository;

    private Long createBookAndGetId(CreateBookRequestDto request) throws Exception {
        String responseContent = mockMvc.perform(post("/books")
                        .with(csrf())
                        .with(user("admin").roles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        // Парсимо просто через ObjectMapper в Map або читаємо поле id напряму
        return objectMapper.readTree(responseContent).get("id").asLong();
    }

    @Test
    @DisplayName("Create book - success")
    void save_ValidRequest_ReturnsCreatedBook() throws Exception {
        CreateBookRequestDto request = new CreateBookRequestDto(
                "The Hobbit", "J.R.R. Tolkien", "9783161484100",
                BigDecimal.valueOf(19.99), "A fantasy novel",
                "http://example.com/cover.jpg", List.of(1L, 2L));

        mockMvc.perform(post("/books")
                        .with(csrf())
                        .with(user("admin").roles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.title").value("The Hobbit"))
                .andExpect(jsonPath("$.author").value("J.R.R. Tolkien"))
                .andExpect(jsonPath("$.description").value("A fantasy novel"))
                .andExpect(jsonPath("$.coverImage").value("http://example.com/cover.jpg"));
    }

    @Test
    @DisplayName("Get book by id - success")
    void findById_ValidId_ReturnsBook() throws Exception {
        CreateBookRequestDto createRequest = new CreateBookRequestDto(
                "1984", "George Orwell", "1234567890123",
                BigDecimal.valueOf(15.00), "Dystopian novel",
                "http://example.com/1984.jpg", List.of());

        Long bookId = createBookAndGetId(createRequest);

        mockMvc.perform(get("/books/{id}", bookId)
                        .with(csrf())
                        .with(user("user").roles("USER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(bookId))
                .andExpect(jsonPath("$.title").value("1984"))
                .andExpect(jsonPath("$.author").value("George Orwell"))
                .andExpect(jsonPath("$.description").value("Dystopian novel"))
                .andExpect(jsonPath("$.coverImage").value("http://example.com/1984.jpg"));
    }

    @Test
    @DisplayName("Update book by ID - success")
    void update_ValidRequest_ReturnsUpdatedBook() throws Exception {
        CreateBookRequestDto createRequest = new CreateBookRequestDto(
                "Original Title", "Original Author", "1111111111111",
                BigDecimal.valueOf(20.00), "Original description",
                "http://example.com/original.jpg", List.of());

        Long bookId = createBookAndGetId(createRequest);

        UpdateBookRequestDto updateRequest = new UpdateBookRequestDto();
        updateRequest.setTitle("Updated Title");
        updateRequest.setAuthor("Updated Author");
        updateRequest.setIsbn("2222222222222");
        updateRequest.setPrice(BigDecimal.valueOf(25.50));
        updateRequest.setDescription("Updated Description");
        updateRequest.setCoverImage("http://example.com/updated.jpg");

        mockMvc.perform(put("/books/{id}", bookId)
                        .with(csrf())
                        .with(user("admin").roles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(bookId))
                .andExpect(jsonPath("$.title").value("Updated Title"))
                .andExpect(jsonPath("$.author").value("Updated Author"))
                .andExpect(jsonPath("$.description").value("Updated Description"))
                .andExpect(jsonPath("$.coverImage").value("http://example.com/updated.jpg"));
    }

    @Test
    @DisplayName("Delete book by ID - success (soft delete)")
    void deleteById_ValidId_SetsDeletedFlag() throws Exception {
        CreateBookRequestDto createRequest = new CreateBookRequestDto(
                "To be deleted", "Author", "3333333333333",
                BigDecimal.valueOf(10.00), "To be deleted description",
                "http://example.com/delete.jpg", List.of());

        Long bookId = createBookAndGetId(createRequest);

        mockMvc.perform(delete("/books/{id}", bookId)
                        .with(csrf())
                        .with(user("admin").roles("ADMIN")))
                .andExpect(status().isNoContent());

        Book deletedBook = bookRepository.findById(bookId).orElseThrow();
        assertTrue(deletedBook.isDeleted(), "Book should be marked as deleted");
    }
}
