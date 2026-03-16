package book.shop.spring.boot.intro.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import book.shop.spring.boot.intro.dto.CategoryResponseDto;
import book.shop.spring.boot.intro.dto.CreateCategoryRequestDto;
import book.shop.spring.boot.intro.exception.EntityNotFoundException;
import book.shop.spring.boot.intro.mapper.CategoryMapper;
import book.shop.spring.boot.intro.model.Category;
import book.shop.spring.boot.intro.repository.CategoryRepository;
import book.shop.spring.boot.intro.util.TestUtil;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
class CategoryServiceImplTest {

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private CategoryMapper categoryMapper;

    @InjectMocks
    private CategoryServiceImpl categoryService;

    @Test
    @DisplayName("Get all categories returns list of DTOs")
    void findAll_ReturnsListOfDtos() {
        Category fictionCategory = TestUtil.createCategory(1L,
                "Fiction", "Books about imagination");
        Category scienceCategory = TestUtil.createCategory(2L,
                "Science", "Scientific topics");

        when(categoryRepository.findAll()).thenReturn(List.of(fictionCategory, scienceCategory));

        CategoryResponseDto fictionDto = new CategoryResponseDto(1L,
                "Fiction", "Books about imagination");
        CategoryResponseDto scienceDto = new CategoryResponseDto(2L,
                "Science", "Scientific topics");

        when(categoryMapper.toResponseDto(fictionCategory)).thenReturn(fictionDto);
        when(categoryMapper.toResponseDto(scienceCategory)).thenReturn(scienceDto);

        List<CategoryResponseDto> result = categoryService.findAll();

        assertThat(result).containsExactly(fictionDto, scienceDto);
        verify(categoryRepository).findAll();
        verify(categoryMapper).toResponseDto(fictionCategory);
        verify(categoryMapper).toResponseDto(scienceCategory);
    }

    @Test
    @DisplayName("Get category by ID - returns DTO")
    void getById_ValidId_ReturnsDto() {
        Long categoryId = 1L;
        Category category = TestUtil.createCategory(categoryId,
                "Fiction",
                "Books about imagination");
        CategoryResponseDto expectedDto = new CategoryResponseDto(categoryId,
                "Fiction",
                "Books about imagination");

        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(category));
        when(categoryMapper.toResponseDto(category)).thenReturn(expectedDto);

        CategoryResponseDto result = categoryService.getById(categoryId);

        assertThat(result).isEqualTo(expectedDto);
        verify(categoryRepository).findById(categoryId);
        verify(categoryMapper).toResponseDto(category);
    }

    @Test
    @DisplayName("Get category by ID - throws if not found")
    void getById_InvalidId_ThrowsException() {
        Long nonExistentId = 42L;
        when(categoryRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> categoryService.getById(nonExistentId))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Category not found by id: " + nonExistentId);

        verify(categoryRepository).findById(nonExistentId);
    }

    @Test
    @DisplayName("Save category - returns DTO")
    void save_ValidDto_ReturnsDto() {
        CreateCategoryRequestDto requestDto = new CreateCategoryRequestDto("Fantasy",
                "Magic books");
        Category unsavedCategory = new Category();
        unsavedCategory.setName("Fantasy");
        unsavedCategory.setDescription("Magic books");

        Category savedCategory = TestUtil.createCategory(1L, "Fantasy",
                "Magic books");
        CategoryResponseDto expectedDto = new CategoryResponseDto(1L, "Fantasy",
                "Magic books");

        when(categoryMapper.toEntity(requestDto)).thenReturn(unsavedCategory);
        when(categoryRepository.save(unsavedCategory)).thenReturn(savedCategory);
        when(categoryMapper.toResponseDto(savedCategory)).thenReturn(expectedDto);

        CategoryResponseDto result = categoryService.save(requestDto);

        assertThat(result).isEqualTo(expectedDto);
        verify(categoryMapper).toEntity(requestDto);
        verify(categoryRepository).save(unsavedCategory);
        verify(categoryMapper).toResponseDto(savedCategory);
    }

    @Test
    @DisplayName("Update category - success")
    void update_ValidId_ReturnsDto() {
        Long categoryId = 1L;
        CreateCategoryRequestDto updateRequest = new CreateCategoryRequestDto("Fantasy",
                "Updated description");

        Category existingCategory = TestUtil
                .createCategory(categoryId, "Old name",
                "Old description");
        Category updatedCategory = TestUtil
                .createCategory(categoryId, "Fantasy",
                "Updated description");
        CategoryResponseDto expectedDto = new CategoryResponseDto(categoryId,
                "Fantasy",
                "Updated description");

        when(categoryRepository.findById(categoryId))
                .thenReturn(Optional.of(existingCategory));
        when(categoryRepository.save(existingCategory)).thenReturn(updatedCategory);
        when(categoryMapper.toResponseDto(updatedCategory)).thenReturn(expectedDto);

        CategoryResponseDto result = categoryService.update(categoryId, updateRequest);

        assertThat(result).isEqualTo(expectedDto);
        verify(categoryRepository).findById(categoryId);
        verify(categoryRepository).save(existingCategory);
        verify(categoryMapper).toResponseDto(updatedCategory);
    }

    @Test
    @DisplayName("Update category - throws if not found")
    void update_InvalidId_ThrowsException() {
        Long nonExistentId = 1L;
        CreateCategoryRequestDto updateRequest = new CreateCategoryRequestDto("Any",
                "Desc");

        when(categoryRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> categoryService.update(nonExistentId, updateRequest))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Category not found with id: " + nonExistentId);

        verify(categoryRepository).findById(nonExistentId);
    }

    @Test
    @DisplayName("Delete category - success")
    void deleteById_ValidId_Deletes() {
        Long categoryId = 1L;
        when(categoryRepository.existsById(categoryId)).thenReturn(true);

        categoryService.deleteById(categoryId);

        verify(categoryRepository).existsById(categoryId);
        verify(categoryRepository).deleteById(categoryId);
    }

    @Test
    @DisplayName("Delete category - throws if not found")
    void deleteById_InvalidId_ThrowsException() {
        Long nonExistentId = 42L;
        when(categoryRepository.existsById(nonExistentId)).thenReturn(false);

        assertThatThrownBy(() -> categoryService.deleteById(nonExistentId))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Category not found with id: " + nonExistentId);

        verify(categoryRepository).existsById(nonExistentId);
    }
}
