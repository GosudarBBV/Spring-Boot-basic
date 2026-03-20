package book.shop.spring.boot.intro.repository;

import static org.assertj.core.api.Assertions.assertThat;

import book.shop.spring.boot.intro.model.Category;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.annotation.DirtiesContext;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@DisplayName("CategoryRepository tests")
@Sql(scripts = "classpath:database/schemas/books.schema.sql",
     executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = "classpath:database/categories/delete-categories.sql",
     executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
public class CategoryRepositoryTest {

    @Autowired
    private CategoryRepository categoryRepository;

    @Test
    @DisplayName("Save a new category - success")
    void saveCategory_Success() {
        Category category = new Category();
        category.setName("Fiction");

        Category savedCategory = categoryRepository.save(category);

        assertThat(savedCategory).isNotNull();
        assertThat(savedCategory.getId()).isNotNull();
        assertThat(savedCategory.getName()).isEqualTo("Fiction");
    }

    @Test
    @DisplayName("Find category by ID - success")
    void findById_ExistingId_ReturnsCategory() {
        Category category = new Category();
        category.setName("Science");
        Category savedCategory = categoryRepository.save(category);

        Optional<Category> result = categoryRepository.findById(savedCategory.getId());

        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo("Science");
    }

    @Test
    @DisplayName("Find all categories - success")
    void findAll_ReturnsAllCategories() {
        Category cat1 = new Category();
        cat1.setName("History");
        Category cat2 = new Category();
        cat2.setName("Art");

        categoryRepository.save(cat1);
        categoryRepository.save(cat2);

        List<Category> categories = categoryRepository.findAll();

        assertThat(categories).hasSize(2);
        assertThat(categories).extracting(Category::getName)
                .containsExactlyInAnyOrder("History", "Art");
    }

    @Test
    @DisplayName("Delete category - success")
    @DirtiesContext
    void deleteCategory_Success() {
        Category category = new Category();
        category.setName("Philosophy");
        Category savedCategory = categoryRepository.save(category);

        categoryRepository.deleteById(savedCategory.getId());

        Optional<Category> result = categoryRepository.findById(savedCategory.getId());
        assertThat(result).isEmpty();
    }
}
