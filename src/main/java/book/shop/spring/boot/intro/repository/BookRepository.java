package book.shop.spring.boot.intro.repository;

import book.shop.spring.boot.intro.model.Book;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookRepository extends JpaRepository<Book,Long> {

}
