package book.shop.spring.boot.intro.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import book.shop.spring.boot.intro.dto.BookDto;
import book.shop.spring.boot.intro.dto.CreateBookRequestDto;
import book.shop.spring.boot.intro.dto.UpdateBookRequestDto;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.jdbc.Sql;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class BookControllerTest {

    @Autowired
    private TestRestTemplate restTemplate;

    private TestRestTemplate adminRestTemplate() {
        return restTemplate.withBasicAuth("admin", "admin");
    }

    private TestRestTemplate userRestTemplate() {
        return restTemplate.withBasicAuth("user", "user");
    }

    @Test
    @DisplayName("Create book with valid data")
    @Sql(scripts = {
            "classpath:database/categories/clear-categories.sql",
            "classpath:database/books/clear-books-table.sql"
    }, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = {
            "classpath:database/books/clear-books-table.sql",
            "classpath:database/categories/delete-categories.sql"
    }, executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    public void saveBook_ValidData_ReturnsCreatedBook() {
        // Створюємо категорії через API або SQL
        restTemplate.postForEntity("/categories", List.of(
                new Object() { public Long id = 1L; public String name = "Test a"; },
                new Object() { public Long id = 2L; public String name = "Test b"; }
        ), Void.class);

        CreateBookRequestDto request = new CreateBookRequestDto(
                "New Book",
                "New Author",
                "1234567890123",
                BigDecimal.valueOf(25.50),
                "Book description",
                "http://example.com/newcover.jpg",
                List.of(1L, 2L)
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<CreateBookRequestDto> entity = new HttpEntity<>(request, headers);

        ResponseEntity<BookDto> response = adminRestTemplate()
                .exchange("/books", HttpMethod.POST, entity, BookDto.class);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("New Book", response.getBody().getTitle());
    }

    @Test
    @DisplayName("Get all books with pagination")
    @Sql(scripts = {
            "classpath:database/categories/add-categories-to-table.sql",
            "classpath:database/books/add-books-to-table.sql",
            "classpath:database/books/add-books-and-categories-into-table.sql"
    }, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = {
            "classpath:database/books/remove-books-from-table-books.sql",
            "classpath:database/books/delete-books-categories.sql",
            "classpath:database/categories/delete-categories.sql"
    }, executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    public void findAllBooks_BooksExist_ReturnsBooksPage() {
        ResponseEntity<BookDto[]> response = adminRestTemplate()
                .getForEntity("/books", BookDto[].class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        BookDto[] books = response.getBody();
        assertNotNull(books);
        assertTrue(books.length >= 2);
        assertEquals("Test Book 1", books[0].getTitle());
        assertEquals("Test Book 2", books[1].getTitle());
    }

    @Test
    @DisplayName("Get book by ID")
    @Sql(scripts = {
            "classpath:database/categories/add-categories-to-table.sql",
            "classpath:database/books/add-books-to-table.sql",
            "classpath:database/books/add-books-and-categories-into-table.sql"
    }, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = {
            "classpath:database/books/remove-books-from-table-books.sql",
            "classpath:database/books/delete-books-categories.sql",
            "classpath:database/categories/delete-categories.sql"
    }, executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    public void findBookById_BookExists_ReturnsBook() {
        ResponseEntity<BookDto> response = adminRestTemplate()
                .getForEntity("/books/1", BookDto.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        BookDto book = response.getBody();
        assertNotNull(book);
        assertEquals("Test Book 1", book.getTitle());
    }

    @Test
    @DisplayName("Update book by ID")
    @Sql(scripts = {
            "classpath:database/categories/add-categories-to-table.sql",
            "classpath:database/books/add-books-to-table.sql",
            "classpath:database/books/add-books-and-categories-into-table.sql"
    }, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = {
            "classpath:database/books/remove-books-from-table-books.sql",
            "classpath:database/books/delete-books-categories.sql",
            "classpath:database/categories/delete-categories.sql"
    }, executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    public void updateBook_BookExists_ReturnsUpdatedBook() {
        UpdateBookRequestDto request = new UpdateBookRequestDto();
        request.setTitle("Updated Title");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<UpdateBookRequestDto> entity = new HttpEntity<>(request, headers);

        ResponseEntity<BookDto> response = adminRestTemplate()
                .exchange("/books/1", HttpMethod.PUT, entity, BookDto.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        BookDto updatedBook = response.getBody();
        assertNotNull(updatedBook);
        assertEquals("Updated Title", updatedBook.getTitle());
    }

    @Test
    @DisplayName("Delete book by ID")
    @Sql(scripts = {
            "classpath:database/categories/add-categories-to-table.sql",
            "classpath:database/books/add-books-to-table.sql",
            "classpath:database/books/add-books-and-categories-into-table.sql"
    }, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = {
            "classpath:database/books/remove-books-from-table-books.sql",
            "classpath:database/books/delete-books-categories.sql",
            "classpath:database/categories/delete-categories.sql"
    }, executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    public void deleteBook_BookExists_ReturnsNoContent() {
        ResponseEntity<Void> response = adminRestTemplate()
                .exchange("/books/1", HttpMethod.DELETE, null, Void.class);
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
    }
}
