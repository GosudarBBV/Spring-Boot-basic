package mate.academy.spring.boot.intro.service;

import java.util.List;
import java.util.Optional;
import java.util.Random;
import lombok.RequiredArgsConstructor;
import mate.academy.spring.boot.intro.dto.BookDto;
import mate.academy.spring.boot.intro.dto.CreateBookRequestDto;
import mate.academy.spring.boot.intro.exception.EntityNotFoundException;
import mate.academy.spring.boot.intro.mapper.BookMapper;
import mate.academy.spring.boot.intro.model.Book;
import mate.academy.spring.boot.intro.repository.BookRepository;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BookServiceImpl implements BookService {
    private final BookRepository repository;
    private final BookMapper bookMapper;

    @Override
    public BookDto save(CreateBookRequestDto requestDto) {
        Book book = bookMapper.toModel(requestDto);
        book.setIsbn("N: " + new Random().nextInt(1000));
        return bookMapper.toDto(repository.save(book));
    }

    @Override
    public List<BookDto> findAll() {
        return repository.findAll().stream()
                .map(bookMapper::toDto)
                .toList();
    }

    @Override
    public BookDto findById(Long id) {
        Book book = repository.findById(id).orElseThrow(
                () -> new EntityNotFoundException("Can't find book by id " + id)
        );
        return bookMapper.toDto(book);
    }

    @Override
    public List<BookDto> findAllByName(String title) {
        return repository.findByName(title).stream()
                .map(bookMapper::toDto)
                .toList();
    }
}
