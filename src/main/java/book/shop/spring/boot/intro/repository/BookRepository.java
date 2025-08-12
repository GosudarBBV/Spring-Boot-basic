package book.shop.spring.boot.intro.repository;

import book.shop.spring.boot.intro.model.Book;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookRepository extends JpaRepository<Book,Long> {
    Page<Book> findAllByCategoriesId(Long id, Pageable pageable);

    Optional<Book> findByTitle(String title);
}
