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
@AutoConfigureTestDatabase(replace =
        AutoConfigureTestDatabase.Replace.NONE)
@DisplayName("Find books by existed category id")
@Sql(scripts = {
        "classpath:database/test/books/add-books-to-table.sql",
        "classpath:database/test/categories/add-categories-to-table.sql",
        "classpath:database/test/books/add-books-and-categories-into-table.sql",
},
        executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = {
        "classpath:database/test/books/delete-books-categories.sql",
        "classpath:database/test/books/remove-books-from-table-books.sql",
        "classpath:database/test/categories/delete-categories.sql",
},
        executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
public class BookRepositoryTest {

    @Autowired
    private BookRepository bookRepository;

    @Test
    @DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
    public void findAllByCategoryId_ExistedId_Success() {
        Long categoryId = 1L;
        Category category = createTestCategory();
        List<Book> expected = List.of(createFirstTestBook(category),
                createSecondTestBook(category));

        Page<Book> actual = bookRepository.findAllByCategoriesId(categoryId, PageRequest.of(0, 10));

        assertThat(actual).usingRecursiveComparison().isEqualTo(expected);
    }

    private Category createTestCategory() {
        Category category = new Category();
        category.setId(12L);
        category.setName("Test a");
        return category;
    }

    private Book createFirstTestBook(Category category) {
        Book book = new Book();
        book.setId(12L);
        book.setTitle("Test Book 1");
        book.setAuthor("Test Author 1");
        book.setIsbn("9783161484100");
        book.setPrice(BigDecimal.valueOf(19.99));
        book.setDescription("Description for Test Book 1");
        book.setCoverImage("http://example.com/cover1.jpg");
        book.setCategories(Set.of(category));
        return book;
    }

    private Book createSecondTestBook(Category category) {
        Book book = new Book();
        book.setId(13L);
        book.setTitle("Test Book 2");
        book.setAuthor("Test Author 2");
        book.setIsbn("9783161484101");
        book.setPrice(BigDecimal.valueOf(29.99));
        book.setDescription("Description for Test Book 2");
        book.setCoverImage("http://example.com/cover2.jpg");
        book.setCategories(Set.of(category));
        return book;
    }
}
