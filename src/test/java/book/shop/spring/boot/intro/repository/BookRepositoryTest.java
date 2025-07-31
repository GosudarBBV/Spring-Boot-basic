package book.shop.spring.boot.intro.repository;

import static org.assertj.core.api.Assertions.assertThat;

import book.shop.spring.boot.intro.config.TestRepositoryConfig;
import book.shop.spring.boot.intro.model.Book;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.jdbc.Sql;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class BookRepositoryTest {

    @Container
    static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", mysql::getJdbcUrl);
        registry.add("spring.datasource.username", mysql::getUsername);
        registry.add("spring.datasource.password", mysql::getPassword);
        registry.add("spring.datasource.driver-class-name", () -> "com.mysql.cj.jdbc.Driver");
    }

    @Autowired
    private BookRepository bookRepository;

    @Test
    @DisplayName("Find books by category ID - returns correct page")
    @Sql(scripts = {
            "classpath:database/schema.sql",
            "classpath:database/insert-category.sql",
            "classpath:database/insert-book.sql",
            "classpath:database/insert-books_categories.sql",
    }, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = "classpath:database/clear-tables.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void findAllByCategoriesId_ValidId_ReturnsBooks() {
        Long categoryId = 1L;

        Page<Book> page = bookRepository.findAllByCategoriesId(categoryId, PageRequest.of(0, 10));

        assertThat(page).isNotNull();
        assertThat(page.getContent()).hasSize(1);
        Book book = page.getContent().get(0);
        assertThat(book.getTitle()).isEqualTo("Test Book");
        assertThat(book.getCategories()).anyMatch(category -> category.getId().equals(categoryId));
    }

    @Test
    @DisplayName("Find books by category ID - empty result")
    @Sql(scripts = "classpath:database/clear-tables.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    void findAllByCategoriesId_InvalidId_ReturnsEmptyPage() {
        Page<Book> page = bookRepository.findAllByCategoriesId(999L, PageRequest.of(0, 10));
        assertThat(page).isNotNull();
        assertThat(page.getContent()).isEmpty();
    }
}
