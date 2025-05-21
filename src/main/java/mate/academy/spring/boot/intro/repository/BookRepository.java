package mate.academy.spring.boot.intro.repository;

import java.util.List;
import mate.academy.spring.boot.intro.model.Book;

public interface BookRepository {
    Book save(Book book);

    List<Book> findAll();
}
