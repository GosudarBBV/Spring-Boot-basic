package book.shop.spring.boot.intro.service;

import book.shop.spring.boot.intro.dto.CategoryResponseDto;
import book.shop.spring.boot.intro.dto.CreateCategoryRequestDto;
import book.shop.spring.boot.intro.exception.EntityNotFoundException;
import book.shop.spring.boot.intro.mapper.CategoryMapper;
import book.shop.spring.boot.intro.model.Category;
import book.shop.spring.boot.intro.repository.CategoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class CategoryServiceImplTest {

    private CategoryRepository categoryRepository;
    private CategoryMapper categoryMapper;
    private CategoryServiceImpl categoryService;

    @BeforeEach
    void setUp() {
        categoryRepository = mock(CategoryRepository.class);
        categoryMapper = mock(CategoryMapper.class);
        categoryService = new CategoryServiceImpl(categoryRepository, categoryMapper);
    }

    @Test
    @DisplayName("Get all categories returns list of DTOs")
    void findAll_ReturnsListOfDtos() {
        Category category1 = new Category();
        category1.setId(1L);
        category1.setName("Fiction");

        Category category2 = new Category();
        category2.setId(2L);
        category2.setName("Science");

        when(categoryRepository.findAll()).thenReturn(List.of(category1, category2));

        CategoryResponseDto dto1 = new CategoryResponseDto(1L, "Fiction", null);
        CategoryResponseDto dto2 = new CategoryResponseDto(2L, "Science", null);

        when(categoryMapper.toResponseDto(category1)).thenReturn(dto1);
        when(categoryMapper.toResponseDto(category2)).thenReturn(dto2);

        List<CategoryResponseDto> result = categoryService.findAll();

        assertThat(result).containsExactly(dto1, dto2);
    }

    @Test
    @DisplayName("Get category by ID - returns DTO")
    void getById_ValidId_ReturnsDto() {
        Long id = 1L;
        Category category = new Category();
        category.setId(id);
        category.setName("Fiction");

        when(categoryRepository.findById(id)).thenReturn(Optional.of(category));

        CategoryResponseDto expectedDto = new CategoryResponseDto(id, "Fiction", null);
        when(categoryMapper.toResponseDto(category)).thenReturn(expectedDto);

        CategoryResponseDto result = categoryService.getById(id);

        assertThat(result).isEqualTo(expectedDto);
    }

    @Test
    @DisplayName("Get category by ID - throws if not found")
    void getById_InvalidId_ThrowsException() {
        Long id = 42L;
        when(categoryRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> categoryService.getById(id))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Category not found by id: " + id);
    }

    @Test
    @DisplayName("Save category - returns DTO")
    void save_ValidDto_ReturnsDto() {
        CreateCategoryRequestDto request = new CreateCategoryRequestDto("Fantasy", "Magic books");

        Category category = new Category();
        category.setName("Fantasy");
        category.setDescription("Magic books");

        Category savedCategory = new Category();
        savedCategory.setId(1L);
        savedCategory.setName("Fantasy");

        CategoryResponseDto expectedDto = new CategoryResponseDto(1L, "Fantasy", "Magic books");

        when(categoryMapper.toEntity(request)).thenReturn(category);
        when(categoryRepository.save(category)).thenReturn(savedCategory);
        when(categoryMapper.toResponseDto(savedCategory)).thenReturn(expectedDto);

        CategoryResponseDto result = categoryService.save(request);

        assertThat(result).isEqualTo(expectedDto);
    }

    @Test
    @DisplayName("Update category - success")
    void update_ValidId_ReturnsDto() {
        Long id = 1L;
        CreateCategoryRequestDto request = new CreateCategoryRequestDto("Fantasy", "Updated desc");

        Category existing = new Category();
        existing.setId(id);
        existing.setName("Old name");

        Category saved = new Category();
        saved.setId(id);
        saved.setName("Fantasy");

        CategoryResponseDto expectedDto = new CategoryResponseDto(id, "Fantasy", "Updated desc");

        when(categoryRepository.findById(id)).thenReturn(Optional.of(existing));
        when(categoryRepository.save(existing)).thenReturn(saved);
        when(categoryMapper.toResponseDto(saved)).thenReturn(expectedDto);

        CategoryResponseDto result = categoryService.update(id, request);

        assertThat(result).isEqualTo(expectedDto);
    }

    @Test
    @DisplayName("Update category - throws if not found")
    void update_InvalidId_ThrowsException() {
        Long id = 1L;
        CreateCategoryRequestDto request = new CreateCategoryRequestDto("Any", "Desc");

        when(categoryRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> categoryService.update(id, request))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Category not found with id: " + id);
    }

    @Test
    @DisplayName("Delete category - success")
    void deleteById_ValidId_Deletes() {
        Long id = 1L;
        when(categoryRepository.existsById(id)).thenReturn(true);

        categoryService.deleteById(id);

        verify(categoryRepository).deleteById(id);
    }

    @Test
    @DisplayName("Delete category - throws if not found")
    void deleteById_InvalidId_ThrowsException() {
        Long id = 42L;
        when(categoryRepository.existsById(id)).thenReturn(false);

        assertThatThrownBy(() -> categoryService.deleteById(id))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Category not found with id: " + id);
    }
}
