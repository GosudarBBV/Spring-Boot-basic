package book.shop.spring.boot.intro.service;

import book.shop.spring.boot.intro.dto.BookDto;
import book.shop.spring.boot.intro.dto.CreateBookRequestDto;
import book.shop.spring.boot.intro.dto.UpdateBookRequestDto;
import book.shop.spring.boot.intro.exception.EntityNotFoundException;
import book.shop.spring.boot.intro.mapper.BookMapper;
import book.shop.spring.boot.intro.model.Book;
import book.shop.spring.boot.intro.repository.BookRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BookServiceImpl implements BookService {
    private final BookRepository repository;
    private final BookMapper bookMapper;

    @Override
    public BookDto save(CreateBookRequestDto requestDto) {
        Book book = bookMapper.toModel(requestDto);
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
                () -> new EntityNotFoundException("Can't find book by id " + id));
        return bookMapper.toDto(book);
    }

    @Override
    public void deleteById(Long id) {
        repository.deleteById(id);
    }

    @Override
    public BookDto update(Long id, UpdateBookRequestDto dto) {
        Book book = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Book not found with id: " + id));

        book.setTitle(dto.getTitle());
        book.setAuthor(dto.getAuthor());
        book.setIsbn(dto.getIsbn());
        book.setPrice(dto.getPrice());
        book.setDescription(dto.getDescription());
        book.setCoverImage(dto.getCoverImage());

        return bookMapper.toDto(repository.save(book));
    }
}
