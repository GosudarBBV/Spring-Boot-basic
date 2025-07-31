package book.shop.spring.boot.intro.service;

import static org.assertj.core.api.Assertions.assertThat;

import book.shop.spring.boot.intro.dto.BookDto;
import book.shop.spring.boot.intro.dto.CreateBookRequestDto;
import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.jdbc.Sql;

@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@SpringBootTest
public class BookServiceSqlTest {

    @Autowired
    private BookService bookService;

    @Test
    @DisplayName("Save book with valid data and only non-deleted categories")
    @Sql(scripts = "/database/schema.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = "/database/insert-categories.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = "/database/clear-tables.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void save_WithValidDataAndNonDeletedCategories_ReturnsSavedBookDto() {
        CreateBookRequestDto requestDto = new CreateBookRequestDto(
                "Effective Java",
                "Joshua Bloch",
                "1234567890123",
                new BigDecimal("49.99"),
                "Best practices",
                "java.jpg",
                List.of(1L, 2L) // 1L — активна, 2L — видалена
        );

        BookDto savedBookDto = bookService.save(requestDto);

        assertThat(savedBookDto).isNotNull();
        assertThat(savedBookDto.getTitle()).isEqualTo("Effective Java");
        assertThat(savedBookDto.getAuthor()).isEqualTo("Joshua Bloch");
        assertThat(savedBookDto.getPrice()).isEqualByComparingTo("49.99");
        assertThat(savedBookDto.getDescription()).isEqualTo("Best practices");
        assertThat(savedBookDto.getCoverImage()).isEqualTo("java.jpg");
    }
}
