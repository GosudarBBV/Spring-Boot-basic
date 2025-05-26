package mate.academy.spring.boot.intro.service;

import java.util.List;
import mate.academy.spring.boot.intro.dto.BookDto;
import mate.academy.spring.boot.intro.dto.CreateBookRequestDto;

public interface BookService {

    BookDto save(CreateBookRequestDto requestDto);

    List<BookDto> findAll();

    BookDto findById(Long id);

    List<BookDto> findAllByName(String title);
}
