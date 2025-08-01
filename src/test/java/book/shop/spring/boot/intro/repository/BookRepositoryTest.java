package book.shop.spring.boot.intro.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;
import book.shop.spring.boot.intro.model.Book;
import book.shop.spring.boot.intro.model.Category;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.jdbc.Sql;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@DisplayName("Find books by existed category id")
@Sql(scripts = "classpath:database/schema.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = {
        "classpath:database/books/add-books-to-table.sql",
        "classpath:database/categories/add-categories-to-table.sql",
        "classpath:database/books/add-books-and-categories-into-table.sql",
}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = {
        "classpath:database/books/delete-books-categories.sql",
        "classpath:database/books/remove-books-from-table-books.sql",
        "classpath:database/categories/delete-categories.sql",
}, executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
public class BookRepositoryTest {

    @Autowired
    private BookRepository bookRepository;

    @Test
    @DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
    void findAllByCategoryId_ExistedId_Success() {
        Long categoryId = 1L;

        Page<Book> result = bookRepository.findAllByCategoriesId(categoryId, PageRequest.of(0, 10));

        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(2);

        assertThat(result.getContent())
                .extracting(Book::getTitle)
                .containsExactlyInAnyOrder("Test Book 1", "Test Book 2");
    }
}
