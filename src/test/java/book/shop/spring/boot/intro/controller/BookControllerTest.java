package book.shop.spring.boot.intro.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import book.shop.spring.boot.intro.config.TestSecurityConfig;
import book.shop.spring.boot.intro.dto.BookDto;
import book.shop.spring.boot.intro.dto.CreateBookRequestDto;
import book.shop.spring.boot.intro.dto.UpdateBookRequestDto;
import book.shop.spring.boot.intro.security.JwtUtil;
import book.shop.spring.boot.intro.service.BookService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(BookController.class)
@Import(TestSecurityConfig.class)
class BookControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private BookService bookService;

    @MockBean
    private JwtUtil jwtUtil;

    @Test
    @DisplayName("Create book - success")
    void save_ValidRequest_ReturnsCreatedBook() throws Exception {
        CreateBookRequestDto request = new CreateBookRequestDto(
                "The Hobbit",
                "J.R.R. Tolkien",
                "9783161484100",
                BigDecimal.valueOf(19.99),
                "A fantasy novel",
                "http://example.com/cover.jpg",
                List.of(1L, 2L)
        );

        BookDto response = new BookDto();
        response.setId(1L);
        response.setTitle(request.title());
        response.setAuthor(request.author());
        response.setPrice(request.price());
        response.setDescription(request.description());
        response.setCoverImage(request.coverImage());

        when(bookService.save(any(CreateBookRequestDto.class))).thenReturn(response);

        mockMvc.perform(post("/books")
                        .with(csrf())
                        .with(user("admin").roles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(content().json(objectMapper.writeValueAsString(response)));
    }

    @Test
    @DisplayName("Get book by id - success")
    void findById_ValidId_ReturnsBook() throws Exception {
        Long bookId = 1L;
        BookDto response = new BookDto();
        response.setId(bookId);
        response.setTitle("The Hobbit");
        response.setAuthor("J.R.R. Tolkien");
        response.setPrice(BigDecimal.valueOf(19.99));
        response.setDescription("A fantasy novel");
        response.setCoverImage("http://example.com/cover.jpg");

        when(bookService.findById(bookId)).thenReturn(response);

        mockMvc.perform(get("/books/1")
                        .with(csrf())
                        .with(user("user").roles("USER")))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(response)));
    }

    @Test
    @DisplayName("Get all books with pagination - success")
    void findAll_ReturnsPageOfBooks() throws Exception {
        BookDto book1 = new BookDto();
        book1.setId(1L);
        book1.setTitle("Book One");
        book1.setAuthor("Author A");
        book1.setPrice(BigDecimal.valueOf(10.99));
        book1.setDescription("Description 1");
        book1.setCoverImage("img1.jpg");

        BookDto book2 = new BookDto();
        book2.setId(2L);
        book2.setTitle("Book Two");
        book2.setAuthor("Author B");
        book2.setPrice(BigDecimal.valueOf(12.99));
        book2.setDescription("Description 2");
        book2.setCoverImage("img2.jpg");

        when(bookService.findAll(any()))
                .thenReturn(new PageImpl<>(List.of(book1, book2),
                        PageRequest.of(0, 10), 2));

        mockMvc.perform(get("/books?page=0&size=10")
                        .with(csrf())
                        .with(user("user").roles("USER")))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Delete book by ID - success")
    void deleteById_ValidId_ReturnsNoContent() throws Exception {
        mockMvc.perform(delete("/books/{id}", 1L)
                        .with(csrf())
                        .with(user("admin").roles("ADMIN")))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("Update book by ID - success")
    void update_ValidRequest_ReturnsUpdatedBook() throws Exception {
        Long bookId = 1L;
        UpdateBookRequestDto request = new UpdateBookRequestDto();
        request.setTitle("Updated Title");
        request.setAuthor("Updated Author");
        request.setIsbn("1234567890123");
        request.setPrice(BigDecimal.valueOf(25.50));
        request.setDescription("Updated Description");
        request.setCoverImage("newimg.jpg");

        BookDto response = new BookDto();
        response.setId(bookId);
        response.setTitle(request.getTitle());
        response.setAuthor(request.getAuthor());
        response.setPrice(request.getPrice());
        response.setDescription(request.getDescription());
        response.setCoverImage(request.getCoverImage());

        when(bookService.update(eq(bookId), any(UpdateBookRequestDto.class)))
                .thenReturn(response);

        mockMvc.perform(put("/books/{id}", bookId)
                        .with(csrf())
                        .with(user("admin").roles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(response)));
    }
}
