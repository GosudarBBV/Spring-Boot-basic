package book.shop.spring.boot.intro.service;

import book.shop.spring.boot.intro.dto.BookDto;
import book.shop.spring.boot.intro.dto.CreateBookRequestDto;
import book.shop.spring.boot.intro.dto.UpdateBookRequestDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface BookService {

    BookDto save(CreateBookRequestDto requestDto);

    Page<BookDto> findAll(Pageable pageable);

    BookDto findById(Long id);

    void deleteById(Long id);

    BookDto update(Long id, UpdateBookRequestDto requestDto);
}
