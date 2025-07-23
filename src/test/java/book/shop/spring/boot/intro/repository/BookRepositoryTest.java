package book.shop.spring.boot.intro.repository;

import book.shop.spring.boot.intro.config.TestRepositoryConfig;
import book.shop.spring.boot.intro.model.Book;
import book.shop.spring.boot.intro.model.Category;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Testcontainers
@Import(TestRepositoryConfig.class)
class BookRepositoryTest {

    @Container
    @ServiceConnection
    static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0")
            .withDatabaseName("book_shop_test")
            .withUsername("user")
            .withPassword("password");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", mysql::getJdbcUrl);
        registry.add("spring.datasource.username", mysql::getUsername);
        registry.add("spring.datasource.password", mysql::getPassword);
    }

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Test
    @DisplayName("Find books by category ID - returns correct page")
    void findAllByCategoriesId_ValidId_ReturnsBooks() {
        Category category = new Category();
        category.setName("Fiction");
        category.setDescription("Fiction books");
        categoryRepository.save(category);

        Book book = new Book();
        book.setTitle("Test Book");
        book.setAuthor("Author");
        book.setPrice(BigDecimal.valueOf(19.99));
        book.setDescription("Description");
        book.setCategories(Set.of(category));
        bookRepository.save(book);

        Page<Book> page = bookRepository.findAllByCategoriesId(category.getId(), PageRequest.of(0, 10));

        assertThat(page).isNotNull();
        assertThat(page.getContent()).hasSize(1);
        assertThat(page.getContent().get(0).getTitle()).isEqualTo("Test Book");
    }

    @Test
    @DisplayName("Find books by category ID - empty result")
    void findAllByCategoriesId_InvalidId_ReturnsEmptyPage() {
        Page<Book> page = bookRepository.findAllByCategoriesId(999L, PageRequest.of(0, 10));
        assertThat(page).isNotNull();
        assertThat(page.getContent()).isEmpty();
    }
}
