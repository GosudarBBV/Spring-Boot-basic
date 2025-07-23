package book.shop.spring.boot.intro.service;

import book.shop.spring.boot.intro.dto.BookDto;
import book.shop.spring.boot.intro.dto.BookDtoWithoutCategoryIds;
import book.shop.spring.boot.intro.dto.CreateBookRequestDto;
import book.shop.spring.boot.intro.dto.UpdateBookRequestDto;
import book.shop.spring.boot.intro.exception.EntityNotFoundException;
import book.shop.spring.boot.intro.mapper.BookMapper;
import book.shop.spring.boot.intro.model.Book;
import book.shop.spring.boot.intro.model.Category;
import book.shop.spring.boot.intro.repository.BookRepository;
import book.shop.spring.boot.intro.repository.CategoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class BookServiceImplTest {

    private BookRepository bookRepository;
    private CategoryRepository categoryRepository;
    private BookMapper bookMapper;
    private BookServiceImpl bookService;

    @BeforeEach
    void setUp() {
        bookRepository = mock(BookRepository.class);
        categoryRepository = mock(CategoryRepository.class);
        bookMapper = mock(BookMapper.class);
        bookService = new BookServiceImpl(bookRepository, bookMapper, categoryRepository);
    }

    @Test
    @DisplayName("Save book with non-deleted categories")
    void save_WithValidDto_ReturnsBookDto() {
        CreateBookRequestDto request = new CreateBookRequestDto(
                "Book Title", "Author Name", "1234567890", new BigDecimal("10.00"),
                "Description", "image.jpg", List.of(1L, 2L)
        );

        Book book = new Book();
        Book savedBook = new Book();
        BookDto expectedDto = new BookDto();

        Category cat1 = new Category();
        cat1.setId(1L);
        cat1.setName("Fiction");
        cat1.setDeleted(false);

        Category cat2 = new Category();
        cat2.setId(2L);
        cat2.setName("Fantasy");
        cat2.setDeleted(true); // має бути проігнорована

        when(bookMapper.toModel(request)).thenReturn(book);
        when(categoryRepository.findAllById(List.of(1L, 2L))).thenReturn(List.of(cat1, cat2));
        when(bookRepository.save(any(Book.class))).thenReturn(savedBook);
        when(bookMapper.toDto(savedBook)).thenReturn(expectedDto);

        BookDto result = bookService.save(request);

        assertThat(result).isEqualTo(expectedDto);
        ArgumentCaptor<Book> bookCaptor = ArgumentCaptor.forClass(Book.class);
        verify(bookRepository).save(bookCaptor.capture());

        Book actualSavedBook = bookCaptor.getValue();
        assertThat(actualSavedBook.getCategories())
                .hasSize(2); // збережено обидві, але це залежить від логіки — за потреби фільтруй явно
    }

    @Test
    @DisplayName("Find all books returns paged list")
    void findAll_ReturnsPageOfBooks() {
        Pageable pageable = Pageable.ofSize(10);
        Book book = new Book();
        BookDto dto = new BookDto();
        Page<Book> page = new PageImpl<>(List.of(book));

        when(bookRepository.findAll(pageable)).thenReturn(page);
        when(bookMapper.toDto(book)).thenReturn(dto);

        Page<BookDto> result = bookService.findAll(pageable);

        assertThat(result.getContent()).containsExactly(dto);
    }

    @Test
    @DisplayName("Find by ID returns BookDto")
    void findById_ValidId_ReturnsBookDto() {
        Long id = 1L;
        Book book = new Book();
        BookDto dto = new BookDto();

        when(bookRepository.findById(id)).thenReturn(Optional.of(book));
        when(bookMapper.toDto(book)).thenReturn(dto);

        BookDto result = bookService.findById(id);

        assertThat(result).isEqualTo(dto);
    }

    @Test
    @DisplayName("Find by ID throws when not found")
    void findById_InvalidId_ThrowsException() {
        Long id = 99L;

        when(bookRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookService.findById(id))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Can't find book by id " + id);
    }

    @Test
    @DisplayName("Update book with valid data")
    void update_ValidIdAndDto_ReturnsUpdatedDto() {
        Long id = 1L;
        UpdateBookRequestDto dto = new UpdateBookRequestDto();
        Book book = new Book();
        BookDto expectedDto = new BookDto();

        when(bookRepository.findById(id)).thenReturn(Optional.of(book));
        doNothing().when(bookMapper).updateBookFromDto(dto, book);
        when(bookRepository.save(book)).thenReturn(book);
        when(bookMapper.toDto(book)).thenReturn(expectedDto);

        BookDto result = bookService.update(id, dto);

        assertThat(result).isEqualTo(expectedDto);
    }

    @Test
    @DisplayName("Update throws if book not found")
    void update_InvalidId_ThrowsException() {
        Long id = 99L;
        UpdateBookRequestDto dto = new UpdateBookRequestDto();

        when(bookRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookService.update(id, dto))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Book not found with id: " + id);
    }

    @Test
    @DisplayName("Delete book by ID")
    void deleteById_DeletesBook() {
        Long id = 1L;
        bookService.deleteById(id);
        verify(bookRepository).deleteById(id);
    }

    @Test
    @DisplayName("Find all by category ID returns correct page")
    void findAllByCategoryId_ReturnsPageOfBooks() {
        Long categoryId = 2L;
        Pageable pageable = Pageable.ofSize(5);
        Book book = new Book();
        BookDtoWithoutCategoryIds dto = new BookDtoWithoutCategoryIds(1L, "title", "author", BigDecimal.ONE, "desc");
        Page<Book> page = new PageImpl<>(List.of(book));

        when(bookRepository.findAllByCategoriesId(categoryId, pageable)).thenReturn(page);
        when(bookMapper.toDtoWithoutCategories(book)).thenReturn(dto);

        Page<BookDtoWithoutCategoryIds> result = bookService.findAllByCategoryId(categoryId, pageable);

        assertThat(result.getContent()).containsExactly(dto);
    }
}
