package book.shop.spring.boot.intro.service;

import book.shop.spring.boot.intro.dto.CategoryResponseDto;
import book.shop.spring.boot.intro.dto.CreateCategoryRequestDto;
import book.shop.spring.boot.intro.exception.EntityNotFoundException;
import book.shop.spring.boot.intro.mapper.CategoryMapper;
import book.shop.spring.boot.intro.model.Category;
import book.shop.spring.boot.intro.repository.CategoryRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {
    private final CategoryRepository repository;
    private final CategoryMapper mapper;

    @Override
    public List<CategoryResponseDto> findAll() {
        return repository.findAll()
                .stream()
                .map(mapper::toResponseDto)
                .toList();
    }

    @Override
    public CategoryResponseDto getById(Long id) {
        Category category = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Category not found by id: " + id));
        return mapper.toResponseDto(category);
    }

    @Override
    public CategoryResponseDto save(CreateCategoryRequestDto dto) {
        Category category = mapper.toEntity(dto);
        return mapper.toResponseDto(repository.save(category));
    }

    @Override
    public CategoryResponseDto update(Long id, CreateCategoryRequestDto dto) {
        Category category = repository.findById(id)
                .orElseThrow(
                        () -> new EntityNotFoundException("Category not found with id: " + id));
        return mapper.toResponseDto(repository.save(category));
    }

    @Override
    public void deleteById(Long id) {
        if (!repository.existsById(id)) {
            throw new EntityNotFoundException("Category not found with id: " + id);
        }
        repository.deleteById(id);
    }
}
