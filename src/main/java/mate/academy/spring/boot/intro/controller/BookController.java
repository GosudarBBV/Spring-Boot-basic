package mate.academy.spring.boot.intro.controller;

import java.util.List;
import lombok.RequiredArgsConstructor;
import mate.academy.spring.boot.intro.dto.BookDto;
import mate.academy.spring.boot.intro.dto.CreateBookRequestDto;
import mate.academy.spring.boot.intro.service.BookService;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping(value = "/books")
public class BookController {

    private final BookService bookService;

    @GetMapping
    public List<BookDto> findAll() {
        return bookService.findAll();
    }

    @GetMapping("/{id}")
    public BookDto findById(@PathVariable Long id) {
        return bookService.findById(id);
    }

    @GetMapping("/by-name")
    public List<BookDto> findAllByName(@RequestParam String title) {
        return bookService.findAllByName(title);
    }

    @PostMapping
    public BookDto save(@RequestBody CreateBookRequestDto requestDto) {
        return bookService.save(requestDto);
    }

}
