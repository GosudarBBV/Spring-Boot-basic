package mate.academy.spring.boot.intro.repository;

import java.util.List;
import java.util.Optional;

import mate.academy.spring.boot.intro.dto.BookDto;
import mate.academy.spring.boot.intro.model.Book;

public interface BookRepository {
    Book save(Book book);

    Optional<Book> findById(Long id);

    List<Book> findAll();

    List<Book> findByName(String title);
}
