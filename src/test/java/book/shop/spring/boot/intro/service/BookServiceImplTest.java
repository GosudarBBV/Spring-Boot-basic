package book.shop.spring.boot.intro.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

@ExtendWith(org.mockito.junit.jupiter.MockitoExtension.class)
class BookServiceImplTest {

    @Mock
    private BookRepository bookRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Spy
    private BookMapper bookMapper;

    @InjectMocks
    private BookServiceImpl bookService;

    @Test
    @DisplayName("Save book with valid data and only non-deleted categories")
    void save_WithValidDataAndNonDeletedCategories_ReturnsSavedBookDto() {
        CreateBookRequestDto requestDto = new CreateBookRequestDto(
                "Effective Java", "Joshua Bloch", "1234567890123",
                new BigDecimal("49.99"), "Best practices",
                "java.jpg", List.of(1L, 2L)
        );

        Book unsavedBook = new Book();
        Book savedBook = new Book();
        BookDto expectedBookDto = new BookDto();

        Category fictionCategory = new Category();
        fictionCategory.setId(1L);
        fictionCategory.setName("Fiction");
        fictionCategory.setDeleted(false);

        Category deletedCategory = new Category();
        deletedCategory.setId(2L);
        deletedCategory.setName("Archived");
        deletedCategory.setDeleted(true);

        when(bookMapper.toModel(requestDto)).thenReturn(unsavedBook);
        when(categoryRepository.findAllById(List.of(1L, 2L)))
                .thenReturn(List.of(fictionCategory, deletedCategory));
        when(bookRepository.save(any(Book.class))).thenReturn(savedBook);
        when(bookMapper.toDto(savedBook)).thenReturn(expectedBookDto);

        BookDto result = bookService.save(requestDto);

        assertThat(result).isEqualTo(expectedBookDto);
        verify(bookRepository).save(any(Book.class));
        verify(categoryRepository).findAllById(List.of(1L, 2L));
        verify(bookMapper).toDto(savedBook);
    }

    @Test
    @DisplayName("Find all books returns paginated result")
    void findAll_ReturnsPaginatedBookDtos() {
        Pageable pageable = Pageable.ofSize(10);
        Book bookEntity = new Book();
        BookDto mappedBookDto = new BookDto();
        Page<Book> bookPage = new PageImpl<>(List.of(bookEntity));

        when(bookRepository.findAll(pageable)).thenReturn(bookPage);
        when(bookMapper.toDto(bookEntity)).thenReturn(mappedBookDto);

        Page<BookDto> result = bookService.findAll(pageable);

        assertThat(result.getContent()).containsExactly(mappedBookDto);
        verify(bookRepository).findAll(pageable);
        verify(bookMapper).toDto(bookEntity);
    }

    @Test
    @DisplayName("Find book by ID returns correct BookDto")
    void findById_ValidId_ReturnsBookDto() {
        Long bookId = 1L;
        Book bookEntity = new Book();
        BookDto expectedDto = new BookDto();

        when(bookRepository.findById(bookId)).thenReturn(Optional.of(bookEntity));
        when(bookMapper.toDto(bookEntity)).thenReturn(expectedDto);

        BookDto result = bookService.findById(bookId);

        assertThat(result).isEqualTo(expectedDto);
        verify(bookRepository).findById(bookId);
        verify(bookMapper).toDto(bookEntity);
    }

    @Test
    @DisplayName("Find book by invalid ID throws EntityNotFoundException")
    void findById_InvalidId_ThrowsEntityNotFoundException() {
        Long nonExistentId = 99L;

        when(bookRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookService.findById(nonExistentId))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Can't find book by id " + nonExistentId);

        verify(bookRepository).findById(nonExistentId);
    }

    @Test
    @DisplayName("Update existing book with valid data")
    void update_ExistingBookIdAndValidDto_UpdatesAndReturnsBookDto() {
        Long bookId = 1L;
        UpdateBookRequestDto updateDto = new UpdateBookRequestDto();
        Book existingBook = new Book();
        BookDto updatedBookDto = new BookDto();

        when(bookRepository.findById(bookId)).thenReturn(Optional.of(existingBook));
        doNothing().when(bookMapper).updateBookFromDto(updateDto, existingBook);
        when(bookRepository.save(existingBook)).thenReturn(existingBook);
        when(bookMapper.toDto(existingBook)).thenReturn(updatedBookDto);

        BookDto result = bookService.update(bookId, updateDto);

        assertThat(result).isEqualTo(updatedBookDto);
        verify(bookRepository).findById(bookId);
        verify(bookMapper).updateBookFromDto(updateDto, existingBook);
        verify(bookRepository).save(existingBook);
        verify(bookMapper).toDto(existingBook);
    }

    @Test
    @DisplayName("Update throws if book with ID not found")
    void update_NonExistentBookId_ThrowsEntityNotFoundException() {
        Long invalidId = 99L;
        UpdateBookRequestDto updateDto = new UpdateBookRequestDto();

        when(bookRepository.findById(invalidId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookService.update(invalidId, updateDto))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Book not found with id: " + invalidId);

        verify(bookRepository).findById(invalidId);
    }

    @Test
    @DisplayName("Delete book by ID")
    void deleteById_ValidId_DeletesBook() {
        Long bookId = 1L;

        bookService.deleteById(bookId);

        verify(bookRepository).deleteById(bookId);
    }

    @Test
    @DisplayName("Find all books by category ID returns paginated DTOs")
    void findAllByCategoryId_ValidCategoryId_ReturnsPaginatedBooks() {
        Long categoryId = 2L;
        Pageable pageable = Pageable.ofSize(5);
        Book bookEntity = new Book();
        BookDtoWithoutCategoryIds bookDto = new BookDtoWithoutCategoryIds(
                1L, "Clean Code", "Robert C. Martin",
                BigDecimal.valueOf(30), "Great book"
        );
        Page<Book> bookPage = new PageImpl<>(List.of(bookEntity));

        when(bookRepository.findAllByCategoriesId(categoryId, pageable)).thenReturn(bookPage);
        when(bookMapper.toDtoWithoutCategories(bookEntity)).thenReturn(bookDto);

        Page<BookDtoWithoutCategoryIds> result = bookService
                .findAllByCategoryId(categoryId, pageable);

        assertThat(result.getContent()).containsExactly(bookDto);
        verify(bookRepository).findAllByCategoriesId(categoryId, pageable);
        verify(bookMapper).toDtoWithoutCategories(bookEntity);
    }
}
