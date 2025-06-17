package book.shop.spring.boot.intro.repository;

import book.shop.spring.boot.intro.model.Book;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookRepository extends JpaRepository<Book,Long> {
    List<Book> findAllByCategories_Id(Long categoryId);
}
