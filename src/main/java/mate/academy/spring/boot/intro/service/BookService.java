package mate.academy.spring.boot.intro.service;

import java.util.List;
import mate.academy.spring.boot.intro.model.Book;

public interface BookService {

    Book save(Book book);

    List<Book> findAll();
}
