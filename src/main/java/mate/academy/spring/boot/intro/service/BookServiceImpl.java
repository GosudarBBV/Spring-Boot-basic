package mate.academy.spring.boot.intro.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import mate.academy.spring.boot.intro.model.Book;
import mate.academy.spring.boot.intro.repository.BookRepository;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BookServiceImpl implements BookService {
    private final BookRepository repository;

    @Override
    public Book save(Book book) {
        return repository.save(book);
    }

    @Override
    public List<Book> findAll() {
        return repository.findAll();
    }
}
