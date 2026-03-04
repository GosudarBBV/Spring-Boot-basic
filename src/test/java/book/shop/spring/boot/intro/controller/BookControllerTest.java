package book.shop.spring.boot.intro.controller;

import static org.assertj.core.api.Assertions.assertThat;

import book.shop.spring.boot.intro.dto.BookDto;
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
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
                "spring.main.allow-bean-definition-overriding=true",
                "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration"
        }
)
class BookControllerTest {

    @LocalServerPort
    private int port;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TestRestTemplate restTemplate;

    private BookDto createBook(CreateBookRequestDto request) throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<String> entity = new HttpEntity<>(objectMapper.writeValueAsString(request), headers);

        ResponseEntity<BookDto> response = restTemplate
                .postForEntity("http://localhost:" + port + "/books", entity, BookDto.class);

        assertThat(response.getStatusCode())
                .withFailMessage("Status: "
                        + response.getStatusCode() + " Body: "
                        + response.getBody())
                .isEqualTo(HttpStatus.CREATED);

        return response.getBody();
    }

    @Test
    @DisplayName("Create book - success")
    void save_ValidRequest_ReturnsCreatedBook() throws Exception {
        CreateBookRequestDto request = new CreateBookRequestDto(
                "The Hobbit", "J.R.R. Tolkien", "9783161484100",
                BigDecimal.valueOf(19.99), "A fantasy novel",
                "http://example.com/cover.jpg", List.of());

        BookDto actualResponse = createBook(request);

        assertThat(actualResponse).isNotNull();
        assertThat(actualResponse.getId()).isNotNull();
        assertThat(actualResponse.getTitle()).isEqualTo("The Hobbit");
        assertThat(actualResponse.getAuthor()).isEqualTo("J.R.R. Tolkien");
        assertThat(actualResponse.getPrice()).isEqualTo(BigDecimal.valueOf(19.99));
        assertThat(actualResponse.getDescription()).isEqualTo("A fantasy novel");
        assertThat(actualResponse.getCoverImage()).isEqualTo("http://example.com/cover.jpg");
    }

    @Test
    @DisplayName("Get book by id - success")
    void findById_ValidId_ReturnsBook() throws Exception {
        CreateBookRequestDto createRequest = new CreateBookRequestDto(
                "1984", "George Orwell", "1234567890123",
                BigDecimal.valueOf(15.00), "Dystopian novel",
                "http://example.com/1984.jpg", List.of());

        BookDto createdBook = createBook(createRequest);

        ResponseEntity<BookDto> response = restTemplate
                .getForEntity("http://localhost:" + port
                        + "/books/" + createdBook.getId(), BookDto.class);

        BookDto actualResponse = response.getBody();
        assertThat(actualResponse).isEqualTo(createdBook);
    }

    @Test
    @DisplayName("Update book by ID - success")
    void update_ValidRequest_ReturnsUpdatedBook() throws Exception {
        CreateBookRequestDto createRequest = new CreateBookRequestDto(
                "Original Title", "Original Author", "1111111111111",
                BigDecimal.valueOf(20.00), "Original description",
                "http://example.com/original.jpg", List.of());

        BookDto createdBook = createBook(createRequest);

        UpdateBookRequestDto updateRequest = new UpdateBookRequestDto();
        updateRequest.setTitle("Updated Title");
        updateRequest.setAuthor("Updated Author");
        updateRequest.setIsbn("2222222222222");
        updateRequest.setPrice(BigDecimal.valueOf(25.50));
        updateRequest.setDescription("Updated Description");
        updateRequest.setCoverImage("http://example.com/updated.jpg");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<>(objectMapper
                .writeValueAsString(updateRequest), headers);

        restTemplate.put("http://localhost:" + port + "/books/"
                + createdBook.getId(), entity);

        ResponseEntity<BookDto> response = restTemplate
                .getForEntity("http://localhost:" + port + "/books/"
                        + createdBook.getId(), BookDto.class);

        BookDto actualResponse = response.getBody();

        assertThat(actualResponse.getTitle()).isEqualTo("Updated Title");
        assertThat(actualResponse.getAuthor()).isEqualTo("Updated Author");
        assertThat(actualResponse.getPrice()).isEqualTo(BigDecimal.valueOf(25.50));
        assertThat(actualResponse.getDescription()).isEqualTo("Updated Description");
        assertThat(actualResponse.getCoverImage())
                .isEqualTo("http://example.com/updated.jpg");
    }

    @Test
    @DisplayName("Delete book by ID - success (soft delete)")
    void deleteById_ValidId_SetsDeletedFlag() throws Exception {
        CreateBookRequestDto createRequest = new CreateBookRequestDto(
                "To be deleted", "Author", "3333333333333",
                BigDecimal.valueOf(10.00), "To be deleted description",
                "http://example.com/delete.jpg", List.of());

        BookDto createdBook = createBook(createRequest);

        restTemplate.delete("http://localhost:" + port + "/books/" + createdBook.getId());

        Book deletedBook = bookRepository.findById(createdBook.getId()).orElseThrow();
        assertThat(deletedBook.isDeleted()).isTrue();
    }
}
